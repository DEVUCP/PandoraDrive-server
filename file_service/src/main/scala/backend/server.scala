package backend

import cats.effect.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import com.comcast.ip4s.*

import cats.effect.{IO, IOApp, ExitCode}
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.ember.server._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.server.Router

import routes.{folder_routes, file_routes}
import schema.{initialize_schemas}
import utils.config

object server extends IOApp:

  private val router = Router(
    "/folder" -> folder_routes,
    "/file" -> file_routes,
    "/ping" -> HttpRoutes.of[IO] { case GET -> Root => Ok("""{ "pong" : "from file_service" }""") }
  ).orNotFound
  var port = sys.env.get("FILE_SERVICE_PORT") match {
    case Some(port) => Port.fromString(port).getOrElse(port"55555")
    case None => port"55555"
  }

  def run(args: List[String]): IO[ExitCode] =
    initialize_schemas() *>
      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port)
        .withHttpApp(router)
        .build
        .use(_ => IO.never)
        .as(ExitCode.Success)
