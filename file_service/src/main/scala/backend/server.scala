package backend

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.*
import com.comcast.ip4s.*
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.*
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io.*
import org.http4s.dsl.io._
import org.http4s.ember.server.*
import org.http4s.ember.server._
import org.http4s.server.Router
import routes.chunk_routes
import routes.file_routes
import routes.folder_routes
import schema.initialize_schemas
import utils.config

object server extends IOApp:

  private val router = Router(
    "/folder" -> folder_routes,
    "/chunk" -> chunk_routes,
    "/file" -> file_routes,
    "/ping" -> HttpRoutes.of[IO] { case GET -> Root =>
      Ok("""{ "pong" : "from file_service" }""")
    }
  ).orNotFound

  def run(args: List[String]): IO[ExitCode] =
    val servicePort =
      Port.fromString(config.SERVICE_PORT).getOrElse(port"55555")
    initialize_schemas() *>
      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(servicePort)
        .withHttpApp(router)
        .build
        .use(_ => IO.never)
        .as(ExitCode.Success)
