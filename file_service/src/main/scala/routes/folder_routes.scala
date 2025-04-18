package routes

import cats.implicits._
import org.http4s.implicits._
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

import schema.initialize_schemas
import model.get_folder_metadata_by_folder_id
import response.ErrorResponse
import org.http4s.server.Router

object IdQueryParamMatcher extends QueryParamDecoderMatcher[Int]("id")

val folder_routes = HttpRoutes
  .of[IO] {
    case GET -> Root :? IdQueryParamMatcher(id) =>
      get_folder_metadata_by_folder_id(id) match {
        case (Some(folder), _) => Ok(folder.asJson)
        case (None, e) if e.nonEmpty =>
          BadRequest(ErrorResponse(e).asJson)
        case (None, _) =>
          NotFound(ErrorResponse("Folder not found").asJson)
      }

    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name!")
    case GET -> Root / "hello" =>
      Ok("Hello, World!")
    case GET -> Root / "ping" =>
      Ok("pong")
  }
