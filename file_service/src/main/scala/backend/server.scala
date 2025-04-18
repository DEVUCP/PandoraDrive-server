package backend

import cats.effect.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import com.comcast.ip4s.*

import cats._
import cats.data._
import cats.effect.IO
import cats.implicits._
import cats.effect.unsafe.implicits.global
import doobie._
import doobie.implicits._

import schema.initialize_schemas
import model.get_file_metadata_by_file_id
import model.get_folder_metadata_by_folder_id

import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.circe._
import scala.util.Try

object server extends IOApp:

  // simple HTTP service/app
  private val fileService = HttpRoutes
    .of[IO] {
      case req @ GET -> Root / "folder" =>
        val id = req.params.get("id")
        id match {
          // There's no ID
          case None =>
            BadRequest(Map("error" -> "Missing id parameter").asJson)

          case Some(idStr) =>
            // Try to convert to int
            Try(idStr.toInt).toOption match {
              case None =>
                BadRequest(Map("error" -> s"Invalid id: $idStr").asJson)

              case Some(id) =>
                get_folder_metadata_by_folder_id(id) match {
                  case (Some(folder), _) => Ok(folder.asJson)
                  case (None, e) if e.nonEmpty =>
                    BadRequest(Map("error" -> e).asJson)
                  case (None, _) =>
                    NotFound(Map("error" -> "Folder not found").asJson)
                }
            }
        }

      case GET -> Root / "hello" / name =>
        Ok(s"Hello, $name!")
      case GET -> Root / "hello" =>
        Ok("Hello, World!")
      case GET -> Root / "ping" =>
        Ok("pong")
    }
    .orNotFound

  // server run func
  def run(args: List[String]): IO[ExitCode] =
    initialize_schemas()
    println("HELLO 1")
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"55551")
      .withHttpApp(fileService)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
