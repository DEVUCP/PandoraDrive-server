package backend.core

import cats.effect.IO
import cats.effect.std.Random
import backend.utils.InputParser.matchesToken
import backend.utils.AnalyticsServiceClient.fetchAnalytics
import backend.models._
import cats.implicits._
import org.http4s.dsl.io._
import cats.effect.unsafe.implicits.global

case class ChatRule(
 name: String,
 priority: Int,
 matches: (List[String], ChatbotTokens) => Boolean,
 execute: (ChatbotTokens, Random[IO], Int) => IO[ChatbotResponse]
)

object RuleEngine {
  private def randomResponse(texts: Option[List[String]], html: Option[List[String]], format_response : String => String) (implicit r: Random[IO]): IO[ChatbotResponse] = {
    texts match {
      case Some(list) if list.nonEmpty =>
        r.nextIntBounded(list.size).map(idx =>
          ChatbotResponse(Some(format_response(list(idx))), html)
        )
      case _ => IO.pure(ChatbotResponse(None, html))
    }
  }

  val allRules: List[ChatRule] = List(
    ChatRule(
      name = "greeting",
      priority = 100,
      matches = (input, tokens) => matchesToken(input, tokens.Greeting.input_tokens),
      execute = (tokens, r, _) => randomResponse(tokens.Greeting.output.text, tokens.Greeting.output.html_object, response => response)(r)
    ),

    ChatRule(
      name = "analytics",
      priority = 80,
      matches = (input, tokens) => matchesToken(input, tokens.Analytics.input_tokens),
      execute = (tokens, r, _) => randomResponse(tokens.Analytics.output.text, tokens.Analytics.output.html_object, response => response)(r)
    ),

    ChatRule(
      name = "largest_file_stat",
      priority = 80,
      matches = (input, tokens) => matchesToken(input, tokens.LargestFileStat.input_tokens),
      execute = (tokens, r, user_id) => fetchAnalytics(user_id).flatMap { data =>
        randomResponse(tokens.LargestFileStat.output.text, tokens.LargestFileStat.output.html_object, response => response.replace("{}", data.LargestFile))(r)
      }
    ),

    ChatRule(
      name = "smallest_file_stat",
      priority = 80,
      matches = (input, tokens) => matchesToken(input, tokens.SmallestFileStat.input_tokens),
      execute = (tokens, r, user_id) => fetchAnalytics(user_id).flatMap { data =>
        randomResponse(tokens.SmallestFileStat.output.text, tokens.SmallestFileStat.output.html_object, response => response.replace("{}", data.SmallestFile))(r)
      }
    ),

    ChatRule(
      name = "file_count_stat",
      priority = 80,
      matches = (input, tokens) => matchesToken(input, tokens.FileCountStat.input_tokens),
      execute = (tokens, r, user_id) => fetchAnalytics(user_id).flatMap { data =>
        randomResponse(tokens.FileCountStat.output.text, tokens.FileCountStat.output.html_object, response => response.replace("{}", data.NumFiles.toString))(r)
      }
    ),

    ChatRule(
      name = "recent_file_stat",
      priority = 80,
      matches = (input, tokens) => matchesToken(input, tokens.RecentFileStat.input_tokens),
      execute = (tokens, r, user_id) => fetchAnalytics(user_id).flatMap { data =>
        randomResponse(tokens.RecentFileStat.output.text, tokens.RecentFileStat.output.html_object, response => response.replace("{}", data.MostRecentFile))(r)
      }
    ),

    ChatRule(
      name = "recent_upload_date_stat",
      priority = 80,
      matches = (input, tokens) => matchesToken(input, tokens.RecentUploadDateStat.input_tokens),
      execute = (tokens, r, user_id) => fetchAnalytics(user_id).flatMap { data =>
        randomResponse(tokens.RecentUploadDateStat.output.text, tokens.RecentUploadDateStat.output.html_object, response => response.replace("{}", data.MostRecentFileUploadDate))(r)
      }
    ),

    ChatRule(
      name = "total_size_stat",
      priority = 80,
      matches = (input, tokens) => matchesToken(input, tokens.TotalSizeStat.input_tokens),
      execute = (tokens, r, user_id) => fetchAnalytics(user_id).flatMap { data =>
        randomResponse(tokens.TotalSizeStat.output.text, tokens.TotalSizeStat.output.html_object, response => response.replace("{}", data.TotalSize.toString))(r)
      }
    ),

    ChatRule(
      name = "daily_uploads_stat",
      priority = 80,
      matches = (input, tokens) => matchesToken(input, tokens.DailyUploadsStat.input_tokens),
      execute = (tokens, r, user_id) => fetchAnalytics(user_id).flatMap { data =>
        randomResponse(tokens.DailyUploadsStat.output.text, tokens.DailyUploadsStat.output.html_object, response => response.replace("{}", data.NumFilesToday.toString))(r)
      }
    ),

    ChatRule(
      name = "weekly_uploads_stat",
      priority = 80,
      matches = (input, tokens) => matchesToken(input, tokens.WeeklyUploadsStat.input_tokens),
      execute = (tokens, r, user_id) => fetchAnalytics(user_id).flatMap { data =>
        randomResponse(tokens.WeeklyUploadsStat.output.text, tokens.WeeklyUploadsStat.output.html_object, response => response.replace("{}", data.NumFilesThisWeek.toString))(r)
      }
    ),

    ChatRule(
      name = "monthly_uploads_stat",
      priority = 80,
      matches = (input, tokens) => matchesToken(input, tokens.MonthlyUploadsStat.input_tokens),
      execute = (tokens, r, user_id) => fetchAnalytics(user_id).flatMap { data =>
        randomResponse(tokens.MonthlyUploadsStat.output.text, tokens.MonthlyUploadsStat.output.html_object, response => response.replace("{}", data.NumFilesThisMonth.toString))(r)
      }
    ),

    ChatRule(
      name = "yearly_uploads_stat",
      priority = 80,
      matches = (input, tokens) => matchesToken(input, tokens.YearlyUploadsStat.input_tokens),
      execute = (tokens, r, user_id) => fetchAnalytics(user_id).flatMap { data =>
        randomResponse(tokens.YearlyUploadsStat.output.text, tokens.YearlyUploadsStat.output.html_object, response => response.replace("{}", data.NumFilesThisYear.toString))(r)
      }
    ),

    ChatRule(
      name = "longest_video_stat",
      priority = 80,
      matches = (input, tokens) => matchesToken(input, tokens.LongestVideoStat.input_tokens),
      execute = (tokens, r, user_id) => fetchAnalytics(user_id).flatMap { data =>
        randomResponse(tokens.LongestVideoStat.output.text, tokens.LongestVideoStat.output.html_object, response => response.replace("{}", data.LongestVideoLength))(r)
      }
    ),

    ChatRule(
      name = "shortest_video_stat",
      priority = 80,
      matches = (input, tokens) => matchesToken(input, tokens.ShortestVideoStat.input_tokens),
      execute = (tokens, r, user_id) => fetchAnalytics(user_id).flatMap { data =>
        randomResponse(tokens.ShortestVideoStat.output.text, tokens.ShortestVideoStat.output.html_object, response => response.replace("{}", data.ShortestVideoLength))(r)
      }
    ),

    ChatRule(
      name = "video_count_stat",
      priority = 80,
      matches = (input, tokens) => matchesToken(input, tokens.VideoCountStat.input_tokens),
      execute = (tokens, r, user_id) => fetchAnalytics(user_id).flatMap { data =>
        randomResponse(tokens.VideoCountStat.output.text, tokens.VideoCountStat.output.html_object, response => response.replace("{}", data.NumVideos.toString))(r)
      }
    ),

    ChatRule(
      name = "photo_count_stat",
      priority = 80,
      matches = (input, tokens) => matchesToken(input, tokens.PhotoCountStat.input_tokens),
      execute = (tokens, r, user_id) => fetchAnalytics(user_id).flatMap { data =>
        randomResponse(tokens.PhotoCountStat.output.text, tokens.PhotoCountStat.output.html_object, response => response.replace("{}", data.NumPhotos.toString))(r)
      }
    ),

    ChatRule(
      name = "folder_count_stat",
      priority = 80,
      matches = (input, tokens) => matchesToken(input, tokens.FolderCountStat.input_tokens),
      execute = (tokens, r, user_id) => fetchAnalytics(user_id).flatMap { data =>
        randomResponse(tokens.FolderCountStat.output.text, tokens.FolderCountStat.output.html_object, response => response.replace("{}", data.NumFolders.toString))(r)
      }
    ),

    ChatRule(
      name = "free_space_stat",
      priority = 80,
      matches = (input, tokens) => matchesToken(input, tokens.FreeSpaceStat.input_tokens),
      execute = (tokens, r, user_id) => fetchAnalytics(user_id).flatMap { data =>
        randomResponse(tokens.FreeSpaceStat.output.text, tokens.FreeSpaceStat.output.html_object, response => response.replace("{}", data.SizeLeft.toString))(r)
      }
    ),

    ChatRule(
      name = "biggest_folder_stat",
      priority = 80,
      matches = (input, tokens) => matchesToken(input, tokens.BiggestFolderStat.input_tokens),
      execute = (tokens, r, user_id) => fetchAnalytics(user_id).flatMap { data =>
        randomResponse(tokens.BiggestFolderStat.output.text, tokens.BiggestFolderStat.output.html_object, response => response.replace("{}", data.BiggestFile.toString))(r)
      }
    ),

    ChatRule(
      name = "upload",
      priority = 71,
      matches = (input, tokens) => matchesToken(input, tokens.UploadFile.input_tokens),
      execute = (tokens, _, _) => IO.pure(ChatbotResponse(
        text = None,
        html_object = tokens.UploadFile.output.html_object
      ))
    ),

    ChatRule(
      name = "display",
      priority = 71,
      matches = (input, tokens) => matchesToken(input, tokens.DisplayFile.input_tokens),
      execute = (tokens, r, _) => randomResponse(tokens.DisplayFile.output.text, tokens.DisplayFile.output.html_object, response => response)(r)
    ),

    ChatRule(
      name = "filter",
      priority = 70,
      matches = (input, tokens) => matchesToken(input, tokens.FilterFiles.input_tokens),
      execute = (tokens, r, _) => randomResponse(tokens.FilterFiles.output.text, tokens.FilterFiles.output.html_object, response => response)(r)
    ),

    ChatRule(
      name = "help",
      priority = 50,
      matches = (input, tokens) => matchesToken(input, tokens.Help.input_tokens),
      execute = (tokens, r, _) => randomResponse(tokens.Help.output.text, tokens.Help.output.html_object, response => response)(r)
    ),

    ChatRule(
      name = "default",
      priority = 0,
      matches = (_, _) => true,
      execute = (tokens, r, _) => randomResponse(tokens.Default.output.text, None, response => response)(r)
    )
  )

  def process(input: List[String], tokens: ChatbotTokens, user_id: Int)(implicit r: Random[IO]): IO[ChatbotResponse] = {
    allRules
      .sortBy(-_.priority)
      .collectFirstSome { rule =>
        if (rule.matches(input, tokens)) Some(rule.execute(tokens, r, user_id))
        else None
      }
      .getOrElse(IO.pure(ChatbotResponse(Some("System error"), None)))
  }
}