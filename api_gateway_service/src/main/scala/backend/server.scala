package backend

import cats.effect.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.implicits._
import org.http4s.server.Router
import com.comcast.ip4s.*
import backend.routes.{AdminRoutes, FileRoutes, ChatbotRoutes, AuthRoutes}
import schema.{initialize_schema}
import utils.config

import org.http4s.server.middleware._
import org.http4s.server.middleware.Throttle
import scala.concurrent.duration.DurationInt

object server extends IOApp:

  private val corsPolicy = CORS.policy.withAllowOriginAll

  //simple HTTP service/app
  private val httpApp = Router(
    "/api/v1/admin" -> AdminRoutes.routes,
    "/api/v1/files" -> FileRoutes.routes,
    "/api/v1/chatbot" -> ChatbotRoutes.routes,
    "/api/v1/auth" -> AuthRoutes.routes,

    "/" -> HttpRoutes.of[IO] {
      case GET -> Root / "hello" / name =>
        Ok(s"Hello, $name!")
      case GET -> Root / "hello" =>
        Ok("Hello, World!")
      case GET -> Root / "ping" ~ json =>
        Ok("pong from gateway to gateway")
      }  ).orNotFound

  private val finalHttpApp = Throttle.httpApp[IO](
    amount = 100,
    per = 1.seconds
  )(corsPolicy(httpApp))

  // server run func
  def run(args: List[String]): IO[ExitCode] =
    val service_port = config.SERVICE_PORT
    
    initialize_schema() *>
    finalHttpApp.flatMap{app =>
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(service_port)
      .withHttpApp(app)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
    }
