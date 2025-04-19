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

  // simple HTTP service/app

  private val router = Router(
    "/folder" -> folder_routes,
    "/file" -> file_routes
  ).orNotFound

  // server run func
  def run(args: List[String]): IO[ExitCode] =
    initialize_schemas()
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"55551")
      .withHttpApp(router)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
