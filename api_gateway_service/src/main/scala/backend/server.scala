package backend

import cats.effect.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.implicits._
import org.http4s.server.Router
import com.comcast.ip4s.*
import backend.routes.{AdminRoutes, FileRoutes, ChatbotRoutes}

object server extends IOApp:

  //simple HTTP service/app
  private val httpApp = Router(
    "/api/v1/admin" -> AdminRoutes.routes,
    "/api/v1/files" -> FileRoutes.routes,
    "/api/v1/chatbot" -> ChatbotRoutes.routes,

    "/" -> HttpRoutes.of[IO] {
      case GET -> Root / "hello" / name =>
        Ok(s"Hello, $name!")
      case GET -> Root / "hello" =>
        Ok("Hello, World!")
      case GET -> Root / "ping" =>
        Ok("pong")
    }
  ).orNotFound

  // server run func
  def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"55551")
      .withHttpApp(httpApp)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)