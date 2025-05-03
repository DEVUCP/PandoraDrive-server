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
import model.{
  get_file_metadata_by_file_id,
  create_file_metadata,
  update_file_metadata,
  get_file_id_by_file_name_and_folder,
  get_file_chunks_metadata
}
import dto.FileCreationBody
import dto.{UploadBody, ChunkMetadataMultipartUpload}
import services.chunk_service
import dto.FileCompletionBody
import model.{get_files_by_folder_id, delete_file}

val file_routes = HttpRoutes
  .of[IO] {
    case GET -> Root :? FolderIdQueryParamMatcher(id) =>
      for {
        list <- get_files_by_folder_id(id)
        resp <- Ok(list.asJson)
      } yield resp

    case GET -> Root :? FileIdQueryParamMatcher(id) =>
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

    case GET -> Root / "download" :? FileIdQueryParamMatcher(id) =>
      get_file_chunks_metadata(id).flatMap {
        case Right(lst) => Ok(lst)
        case Left(errorMsg) =>
          IO.println(errorMsg) *>
            InternalServerError("Internal Server Error")
      }

    case req @ POST -> Root / "upload" / "init" =>
      req.as[FileCreationBody].attempt.flatMap {
        case Left(error) =>
          BadRequest(ErrorResponse(s"Invalid body: ${error.getMessage}").asJson)

        case Right(body) =>
          get_file_id_by_file_name_and_folder(body.file_name, body.folder_id)
            .flatMap {
              case Some(id) =>
                update_file_metadata(body, id).flatMap {
                  case Left(err) =>
                    BadRequest(ErrorResponse(s"Error occurred: $err").asJson)
                  case Right(file_id) =>
                    Ok(
                      FileUploadMetadataInserted(
                        "File Metadata Inserted",
                        file_id
                      ).asJson
                    )
                }

              case None =>
                create_file_metadata(body).flatMap {
                  case Left(err) =>
                    BadRequest(ErrorResponse(s"Error occurred: $err").asJson)

                  case Right(file_id) =>
                    Ok(
                      FileUploadMetadataInserted(
                        "File Metadata Inserted",
                        file_id
                      ).asJson
                    )
                }
            }
      }

    case req @ POST -> Root / "upload" / "complete" =>
      req.as[FileCompletionBody].attempt.flatMap {
        case Left(_)     => BadRequest(ErrorResponse("Invalid Body").asJson)
        case Right(body) => chunk_service.upload_complete(body)
      }

    case DELETE -> Root / "delete" :? FileIdQueryParamMatcher(id) => {
      delete_file(id) *>
        Ok()
    }
  }
