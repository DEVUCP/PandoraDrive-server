package routes

import cats.implicits._
import org.http4s.implicits._
import cats.effect.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.multipart.*
import com.comcast.ip4s.*

import cats.effect.{IO, IOApp, ExitCode}
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.multipart.Multipart
import org.http4s.circe.CirceEntityCodec._
import org.http4s.server.Router

import schema.initialize_schemas
import model.get_folder_metadata_by_folder_id
import types.{ErrorResponse, FileUploadMetadataInserted}
import model.{get_file_metadata_by_file_id, create_file_metadata}
import dto.FileCreationBody
import utils.jwt.create_token
import dto.{UploadToken, ChunkMetadataMultipartUpload}
import services.chunk_service
import dto.FileCompletionBody

val file_routes = HttpRoutes
  .of[IO] {
    case GET -> Root :? IdQueryParamMatcher(id) =>
      get_file_metadata_by_file_id(id).flatMap {
        case Right(file) =>
          Ok(file.asJson)

        case Left(errorMsg) =>
          errorMsg match {
            case msg if msg.startsWith("No file found") =>
              NotFound(ErrorResponse(msg).asJson)
            case other =>
              BadRequest(ErrorResponse(other).asJson)
          }
      }
    case req @ POST -> Root / "upload" / "init" =>
      req.as[FileCreationBody].attempt.flatMap {
        case Left(error) =>
          BadRequest(ErrorResponse(s"Invalid body: ${error.getMessage}").asJson)

        case Right(body) =>
          create_file_metadata(body).flatMap {
            case Left(err) =>
              BadRequest(ErrorResponse(s"Error occurred: $err").asJson)

            case Right(file_id) =>
              create_token(UploadToken(file_id)) match {
                case None =>
                  BadRequest(
                    ErrorResponse(
                      "Failed to create token. If you are a developer, check console output"
                    ).asJson
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
    case req @ POST -> Root / "upload" / "chunk" =>
      EntityDecoder
        .mixedMultipartResource[IO]()
        .use(decoder =>
          req.decodeWith(decoder, strict = true)(multipart =>
            val chunk_metadata =
              multipart.parts.find(_.name.contains("metadata"))
            val chunk =
              multipart.parts.find(_.name.contains("chunk"))
            (chunk_metadata, chunk) match {
              case (_, None) =>
                BadRequest(
                  ErrorResponse("Invalid request: Chunk is not uploaded")
                )
              case (None, _) =>
                BadRequest(
                  ErrorResponse("Invalid request: No Metadata Received")
                )
              case (Some(metadata), Some(chunk)) =>
                chunk_service.upload_chunk(metadata, chunk)
            }
          )
        )
    case req @ POST -> Root / "upload" / "complete" =>
      req.as[FileCompletionBody].attempt.flatMap {
        case Left(_)     => BadRequest(ErrorResponse("Invalid Body").asJson)
        case Right(body) => chunk_service.upload_complete(body)
      }
  }
