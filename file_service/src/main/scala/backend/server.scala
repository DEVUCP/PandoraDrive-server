package backend

import cats.syntax.all._

import cats.effect.{ExitCode, IO, IOApp, Resource}

import com.comcast.ip4s._
import db.database_setup
import jobs.ChunkCleanup
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.ember.server._
import org.http4s.server.Router
import routes.{chunk_routes, file_routes, folder_routes}
import utils.config

object server extends IOApp {

  private val router = Router(
    "/folder" -> folder_routes,
    "/chunk" -> chunk_routes,
    "/file" -> file_routes,
    "/ping" -> HttpRoutes.of[IO] { case GET -> Root =>
      Ok("""{ "pong" : "from file_service" }""")
    }
  ).orNotFound

  def run(args: List[String]): IO[ExitCode] = {
    val servicePort =
      Port.fromString(config.SERVICE_PORT).getOrElse(port"55555")

    val serverResource: Resource[IO, Unit] =
      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(servicePort)
        .withHttpApp(router)
        .build
        .as(())

    // Explicitly specifying IO for the cleanup job
    val cleanupJobResource: Resource[IO, Unit] =
      Resource.make(ChunkCleanup.runJob().start)(_.cancel).void

    val resources: Resource[IO, Unit] = for {
      _ <- Resource.eval(
        IO.println("Starting database setup...") *> db.database_setup()
      )
      _ <- cleanupJobResource
      _ <- serverResource
    } yield ()

    // Use the resources and handle lifecycle properly
    resources.use(_ => IO.never).as(ExitCode.Success)
  }
}
