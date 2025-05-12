package backend.core

import cats.effect.IO
import cats.effect.std.Random
import backend.models._
import cats.implicits._

case class ChatRule(
 name: String,
 priority: Int,
 matches: (List[String], ChatbotTokens) => Boolean,
 execute: (ChatbotTokens, Random[IO]) => IO[ChatbotResponse]
)

object RuleEngine {
  private def randomResponse(texts: Option[List[String]], html: Option[List[String]]) (implicit r: Random[IO]): IO[ChatbotResponse] = {
    texts match {
      case Some(list) if list.nonEmpty =>
        r.nextIntBounded(list.size).map(idx =>
          ChatbotResponse(Some(list(idx)), html)
        )
      case _ => IO.pure(ChatbotResponse(None, html))
    }
  }

  val allRules: List[ChatRule] = List(
    ChatRule(
      name = "greeting",
      priority = 100,
      matches = (input, tokens) => input.exists(tokens.Greeting.input_tokens.contains),
      execute = (tokens, r) => randomResponse(tokens.Greeting.output.text, tokens.Greeting.output.html_object)(r)
    ),

    ChatRule(
      name = "analytics",
      priority = 80,
      matches = (input, tokens) => input.exists(tokens.Analytics.input_tokens.contains),
      execute = (tokens, r) => randomResponse(tokens.Analytics.output.text, tokens.Analytics.output.html_object)(r)
    ),

    ChatRule(
      name = "upload",
      priority = 85,
      matches = (input, tokens) => input.exists(tokens.UploadFile.input_tokens.contains),
      execute = (tokens, _) => IO.pure(ChatbotResponse(
        text = None,
        html_object = tokens.UploadFile.output.html_object
      ))
    ),

    ChatRule(
      name = "display",
      priority = 75,
      matches = (input, tokens) => input.exists(tokens.DisplayFile.input_tokens.contains),
      execute = (tokens, r) => randomResponse(tokens.DisplayFile.output.text, tokens.DisplayFile.output.html_object)(r)
    ),

    ChatRule(
      name = "filter",
      priority = 70,
      matches = (input, tokens) => input.exists(tokens.FilterFiles.input_tokens.contains),
      execute = (tokens, r) => randomResponse(tokens.FilterFiles.output.text, tokens.FilterFiles.output.html_object)(r)
    ),

    ChatRule(
      name = "help",
      priority = 50,
      matches = (input, tokens) => input.exists(tokens.Help.input_tokens.contains),
      execute = (tokens, r) => randomResponse(tokens.Help.output.text, tokens.Help.output.html_object)(r)
    ),

    ChatRule(
      name = "default",
      priority = 0,
      matches = (_, _) => true,
      execute = (tokens, r) => randomResponse(tokens.Default.output.text, None)(r)
    )
  )

  def process(input: List[String], tokens: ChatbotTokens)(implicit r: Random[IO]): IO[ChatbotResponse] = {
    allRules
      .sortBy(-_.priority)
      .collectFirstSome { rule =>
        if (rule.matches(input, tokens)) Some(rule.execute(tokens, r))
        else None
      }
      .getOrElse(IO.pure(ChatbotResponse(Some("System error"), None)))
  }
}