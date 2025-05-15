package backend

import cats.effect.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.ember.client.EmberClientBuilder
import com.comcast.ip4s.*
import io.circe.generic.auto.*
import org.http4s.circe.*
import io.circe.Json
import io.circe.syntax.*
import java.time.LocalDate

implicit val fileDecoder: EntityDecoder[IO, List[FileMetadata]] = jsonOf
implicit val analyticsEncoder: EntityEncoder[IO, Json] = jsonEncoderOf

def routeRequestImpl[T](uriString: String, method: Method)(
  implicit decoder: EntityDecoder[IO, T]
): IO[T] = {
  EmberClientBuilder.default[IO].build.use { client =>
    Uri.fromString(uriString) match {
      case Right(uri) =>
        val req = Request[IO](method = method, uri = uri)
        client.expect[T](req)

      case Left(parseFailure) =>
        IO.raiseError(new RuntimeException(s"Invalid URI: ${parseFailure.details}"))
    }
  }
}

case class FileMetadata(
  file_id: Int,
  folder_id: Int,
  file_name: String,
  created_at: String,
  modified_at: String,
  size_bytes: Int,
  mime_type: String,
  status: String,
)

// Placeholder values
val maxDriveSpace: Int = 536870912 // 512 MB

private def getCreatedDates(files: List[FileMetadata]): List[String] =
  files.map(_.created_at).filter(_.nonEmpty).sorted

private def isPhoto(mime: String): Boolean = mime.startsWith("image/")
private def isVideo(mime: String): Boolean = mime.startsWith("video/")

def getAnalytics(folderId: String): IO[Response[IO]] = {
  val fileServiceUrl = s"http://localhost:55555/file?folder_id=$folderId"

  def parseDate(str: String): Option[LocalDate] =
    try Some(LocalDate.parse(str.take(10)))
    catch { case _: Throwable => None }

  routeRequestImpl[List[FileMetadata]](fileServiceUrl, Method.GET)
    .flatMap { files =>
      val numFiles = files.size
      val sortedBySize = files.sortBy(_.size_bytes)
      val smallest = sortedBySize.headOption
      val largest = sortedBySize.lastOption
      val totalSize = files.map(_.size_bytes).sum
      val spaceLeft = maxDriveSpace - totalSize

      val createdDates = files.flatMap(f => parseDate(f.created_at))
      val mostRecentCreated =
        createdDates.sorted.lastOption.map(_.toString).getOrElse("N/A")

      val uploadedDates = files.flatMap(f => parseDate(f.created_at))
      val mostRecentUploaded =
        uploadedDates.sorted.lastOption.map(_.toString).getOrElse("N/A")

      val photos = files.count(f => isPhoto(f.mime_type))
      val videos = files.count(f => isVideo(f.mime_type))

      val currentDate = LocalDate.now()
      def uploadedThis(pred: LocalDate => Boolean): Int =
        uploadedDates.count(pred)

      val uploadedToday = uploadedThis(_.isEqual(currentDate))
      val uploadedWeek = uploadedThis(_.isAfter(currentDate.minusDays(7)))
      val uploadedMonth = uploadedThis(_.isAfter(currentDate.minusDays(30)))
      val uploadedYear = uploadedThis(_.isAfter(currentDate.minusDays(365)))

      val largestVideo = sortedBySize.reverse.find(f => isVideo(f.mime_type))
      val smallestVideo = sortedBySize.find(f => isVideo(f.mime_type))

      val responseJson = Json.obj(
        "LargestFile" -> Json.fromString(
          largest.map(_.file_name).getOrElse("N/A")
        ),
        "SmallestFile" -> Json.fromString(
          smallest.map(_.file_name).getOrElse("N/A")
        ),
        "NumFiles" -> Json.fromInt(
          numFiles
        ),
        "MostRecentFile" -> Json
          .fromString(mostRecentCreated),
        "MostRecentFileUploadDate" -> Json.fromString(
          mostRecentUploaded
        ),
        "TotalSize" -> Json.fromInt(totalSize),
        "NumFilesToday" -> Json.fromInt(
          uploadedToday
        ),
        "NumFilesThisWeek" -> Json.fromInt(
          uploadedWeek
        ),
        "NumFilesThisMonth" -> Json.fromInt(
          uploadedMonth
        ),
        "NumFilesThisYear" -> Json.fromInt(
          uploadedYear
        ),
        "LongestVideoLength" -> Json.fromString(
          largestVideo.map(v => v.size_bytes + " bytes").getOrElse("N/A")
        ),
        "ShortestVideoLength" -> Json.fromString(
          smallestVideo.map(v => v.size_bytes + " bytes").getOrElse("N/A")
        ),
        "NumVideos" -> Json.fromInt(videos),
        "NumPhotos" -> Json.fromInt(photos),
        "NumFolders" -> Json.fromInt(
          files.map(_.folder_id).distinct.size
        ),
        "SizeLeft" -> Json.fromInt(spaceLeft),
        "BiggestFile" -> Json
          .fromInt(
            files
              .groupBy(_.folder_id)
              .maxByOption(_._2.size)
              .map(_._1)
              .getOrElse(-1)
          )
      )

      Ok(responseJson)
    }
    .handleErrorWith { e =>
      println(s"Error during analytics generation: ${e.getMessage}")
      InternalServerError(
        Json.obj("error" -> Json.fromString("Failed to generate analytics"))
      )
    }
}

object server extends IOApp:
  object UserIdQueryParameter extends QueryParamDecoderMatcher[String]("user_id")

  private val helloWorldService = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name!")
    case GET -> Root / "hello" =>
      Ok("Hello, World!")
    case GET -> Root / "ping" =>
      Ok("pong")
    case GET -> Root / "analytics" :? UserIdQueryParameter(id) =>
      getAnalytics(id)
  }.orNotFound

  var port = sys.env.get("ANALYTICS_SERVICE_PORT") match {
    case Some(port) => Port.fromString(port).getOrElse(port"55552")
    case None => port"55552"
  }

  def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port)
      .withHttpApp(helloWorldService)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
