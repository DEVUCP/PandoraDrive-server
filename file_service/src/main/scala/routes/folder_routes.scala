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
import types.ErrorResponse
import org.http4s.server.Router

val folder_routes = HttpRoutes
  .of[IO] { case req @ GET -> Root :? IdQueryParamMatcher(id) =>
    get_folder_metadata_by_folder_id(id) match {
      case (Some(folder), None) => Ok(folder.asJson)
      case (None, None) =>
        NotFound(ErrorResponse("Folder not found").asJson)
      case (None, Some(e)) =>
        BadRequest(ErrorResponse(e).asJson)
    }
  }
