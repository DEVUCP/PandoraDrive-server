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
import model.get_file_metadata_by_file_id

val file_routes = HttpRoutes
  .of[IO] { case GET -> Root :? IdQueryParamMatcher(id) =>
    get_file_metadata_by_file_id(id) match {
      case (Some(file), _) => Ok(file.asJson)
      case (_, Some(e)) =>
        BadRequest(ErrorResponse(e).asJson)
      case (None, None) =>
        NotFound(ErrorResponse("File not found").asJson)
    }
  }
