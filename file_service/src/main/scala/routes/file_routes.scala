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
import types.{ErrorResponse, FileUploadMetadataInserted}
import org.http4s.server.Router
import model.{get_file_metadata_by_file_id, create_file_metadata}
import org.http4s.circe.CirceEntityCodec._
import io.circe.generic.auto._
import dto.FileCreationBody
import utils.jwt.create_token
import dto.FileInitToken

val file_routes = HttpRoutes
  .of[IO] {
    case GET -> Root :? IdQueryParamMatcher(id) =>
      get_file_metadata_by_file_id(id) match {
        case (Some(file), _) => Ok(file.asJson)
        case (_, Some(e)) =>
          BadRequest(ErrorResponse(e).asJson)
        case (None, None) =>
          NotFound(ErrorResponse("File not found").asJson)
      }
    case req @ POST -> Root / "upload" / "init" =>
      req.as[FileCreationBody].attempt.flatMap {
        case Left(error) =>
          BadRequest(ErrorResponse(s"Invalid body: ${error.getMessage}").asJson)
        case Right(body) =>
          val err: Either[String, Long] = create_file_metadata(body)
          err match {
            case Left(err) =>
              BadRequest(ErrorResponse(s"Error Ocurred: $err"))
            case Right(file_id) => {
              val token = create_token(FileInitToken(file_id))
              token match {
                case None =>
                  BadRequest(
                    ErrorResponse(
                      "Failed to create token. If you are a developer, check console output"
                    )
                  )
                case Some(valid_token) =>
                  Ok(
                    FileUploadMetadataInserted(
                      "File Metadata Inserted",
                      valid_token
                    ).asJson
                  )
              }
            }
          }
      }
  }
