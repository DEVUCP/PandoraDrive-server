package backend

import cats.effect.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.implicits._
import org.http4s.server.Router
import com.comcast.ip4s.*
import backend.routes.{AdminRoutes, FileRoutes, ChatbotRoutes}
import schema.{initialize_schema}
import utils.config

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
      case GET -> Root / "ping" ~ json =>
        Ok("pong from gateway to gateway")
      }  ).orNotFound


  // server run func
  def run(args: List[String]): IO[ExitCode] =
    val service_port = config.SERVICE_PORT
    initialize_schema() *>
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(service_port)
      .withHttpApp(httpApp)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)