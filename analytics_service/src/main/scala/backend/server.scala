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
import io.circe.syntax.*  // Added for Json handling
import java.time.LocalDate

def parseDate(str: String): Option[LocalDate] =
  try Some(LocalDate.parse(str.take(10)))
  catch { case _: Throwable => None }

case class FileMetadata(
    file_id: Int,
    folder_id: Int,
    file_name: String,
    size_bytes: Int,
    mime_type: String,
    user_id: Int,
    status: String,
    uploaded_at: String,
    created_at: String,
    modified_at: String
)

implicit val fileDecoder: EntityDecoder[IO, List[FileMetadata]] = jsonOf
implicit val analyticsEncoder: EntityEncoder[IO, Json] = jsonEncoderOf

def routeRequestImpl[T](uriString: String, method: Method)(implicit
    decoder: EntityDecoder[IO, T]
): IO[T] = {
  EmberClientBuilder.default[IO].build.use { client =>
    Uri.fromString(uriString) match {
      case Right(uri) =>
        val req = Request[IO](method = method, uri = uri)
        client.expect[T](req)

      case Left(parseFailure) =>
        IO.raiseError(
          new RuntimeException(s"Invalid URI: ${parseFailure.details}")
        )
    }
  }
}

// Constants
val maxDriveSpace: Int = 536870912 // 512 MB

// Helpers
private def getCreatedDates(files: List[FileMetadata]): List[String] =
  files.map(_.created_at).filter(_.nonEmpty).sorted

private def isPhoto(mime: String): Boolean = mime.startsWith("image/")
private def isVideo(mime: String): Boolean = mime.startsWith("video/")

def getAnalytics(folderId: String): IO[Response[IO]] = {
  val fileServiceUrl = s"http://localhost:55555/file?folder_id=$folderId"

  routeRequestImpl[List[FileMetadata]](fileServiceUrl, Method.GET)
    .flatMap { files =>
      val numFiles = files.size
      val sortedBySize = files.sortBy(_.size_bytes)
      val smallest = sortedBySize.headOption
      val largest = sortedBySize.lastOption
      val totalSize = files.map(_.size_bytes).sum
      val spaceLeft = maxDriveSpace - totalSize

      Ok(
        Json.obj(
          "The largest file in your drive" -> Json.fromString(
            largest.map(_.file_name).getOrElse("N/A")
          ),
          "The smallest file in your drive" -> Json.fromString(
            smallest.map(_.file_name).getOrElse("N/A")
          ),
          "The total size of your uploaded media is" -> Json.fromInt(totalSize),
          "The space you have left in your drive" -> Json.fromInt(spaceLeft),
          "The number of files you uploaded to your drive" -> Json.fromInt(numFiles),
          "Your most recently uploaded photo/video was taken at" -> Json.fromString(
            files.flatMap(f => f.taken_at).sorted.lastOption.getOrElse("N/A")
          ),
          "Your last photo/video uploaded was uploaded at" -> Json.fromString(
            files.flatMap(f => f.created_at).sorted.lastOption.getOrElse("N/A")
          ),
          "The number of photos/videos you uploaded this day" -> Json.fromInt(
            files.count(f => f.created_at.exists(_.startsWith(currentDate("day"))) && (isPhoto(f.mime_type) || isVideo(f.mime_type)))
          ),
          "The number of photos/videos you uploaded this week" -> Json.fromInt(
            files.count(f => f.created_at.exists(_.startsWith(currentDate("week"))) && (isPhoto(f.mime_type) || isVideo(f.mime_type)))
          ),
          "The number of photos/videos you uploaded this month" -> Json.fromInt(
            files.count(f => f.created_at.exists(_.startsWith(currentDate("month"))) && (isPhoto(f.mime_type) || isVideo(f.mime_type)))
          ),
          "The number of photos/videos you uploaded this year" -> Json.fromInt(
            files.count(f => f.created_at.exists(_.startsWith(currentDate("year"))) && (isPhoto(f.mime_type) || isVideo(f.mime_type)))
          ),
          "The length of your longest uploaded video" -> Json.fromInt(
            files.filter(f => isVideo(f.mime_type)).flatMap(_.duration_seconds).maxOption.getOrElse(0)
          ),
          "The length of your shortest uploaded video" -> Json.fromInt(
            files.filter(f => isVideo(f.mime_type)).flatMap(_.duration_seconds).minOption.getOrElse(0)
          ),
          "The number of videos you have in your drive" -> Json.fromInt(
            files.count(f => isVideo(f.mime_type))
          ),
          "The number of photos you have in your drive" -> Json.fromInt(
            files.count(f => isPhoto(f.mime_type))
          ),
          "The number of folders you have created in your drive" -> Json.fromInt(
            files.map(_.folder_id).distinct.length
          ),
          "The folder that has the biggest number of uploaded media" -> Json.fromString(
            files.groupBy(_.folder_id).maxByOption(_._2.length).map(_._1).getOrElse("N/A")
          )
        )
      )
  }
}

object server extends IOApp:
  object FolderIdQueryParameter extends QueryParamDecoderMatcher[String]("folder_id")

  private val helloWorldService = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name!")
    case GET -> Root / "hello" =>
      Ok("Hello, World!")
    case GET -> Root / "ping" =>
      Ok("pong")
    case GET -> Root / "analytics" :? FolderIdQueryParameter(id) =>
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
