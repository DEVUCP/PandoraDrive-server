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
import java.time.LocalDate

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

// Constants
val maxDriveSpace: Int = 536870912 // 512 MB

// Helpers
private def getCreatedDates(files: List[FileMetadata]): List[String] =
  files.map(_.created_at).filter(_.nonEmpty).sorted

private def isPhoto(mime: String): Boolean = mime.startsWith("image/")
private def isVideo(mime: String): Boolean = mime.startsWith("video/")

object server extends IOApp:
  object FolderIdQueryParameter extends QueryParamDecoderMatcher[String]("folder_id")

  private val helloWorldService = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name!")
    case GET -> Root / "hello" =>
      Ok("Hello, World!")
    case GET -> Root / "ping" =>
      Ok("pong")
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
