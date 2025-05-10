package services

import cats.data.EitherT

import cats.effect.IO

import dto.{
  DTOFileDownloadBody,
  FileCompletionBody,
  FileCreationBody,
  UploadBody,
  FileDeletionBody
}
import io.circe.generic.auto._
import io.circe.syntax._
import model.{
  are_file_chunks_uploaded,
  create_file_metadata,
  delete_file_metadata,
  get_file_chunks_metadata,
  get_file_id_by_file_name_and_folder,
  get_file_metadata_by_file_id,
  get_files_by_folder_id,
  remove_file_chunks,
  set_file_status_uploaded,
  update_file_metadata,
  validate_folder_user
}
import org.http4s.Response
import org.http4s.circe._
import org.http4s.dsl.io.*
import types.{ErrorResponse, FileId, FileUploadMetadataInserted, FolderId}
import utils.{config, jwt}

object file_service {
  def folder_files(folder_id: FolderId): IO[Response[IO]] =
    for {
      list <- get_files_by_folder_id(folder_id)
      resp <- Ok(list.asJson)
    } yield resp

  def file_by_id(file_id: FileId): IO[Response[IO]] =
    EitherT(get_file_metadata_by_file_id(file_id)).value
      .flatMap {
        case Right(file) =>
          Ok(file.asJson)
        case Left(err) =>
          IO.println(err) *>
            InternalServerError(
              ErrorResponse("Internal Server Error").asJson
            )
      }

  def download_file_metadata(file_id: FileId) =
    get_file_chunks_metadata(file_id).flatMap {
      case Left(errorMsg) =>
        IO.println(errorMsg) *>
          InternalServerError(ErrorResponse("Internal Server Error").asJson)
      case Right(lst) =>
        Ok(
          DTOFileDownloadBody(
            s"${config.SERVICE_URL}:${config.SERVICE_PORT}/chunk/download",
            lst
          ).asJson
        )
    }

  def upload_file_metadata(body: FileCreationBody): IO[Response[IO]] =
    validate_folder_user(body.user_id, body.folder_id).flatMap {
      case false => BadRequest(ErrorResponse("Invalid Folder data").asJson)
      case true =>
        get_file_id_by_file_name_and_folder(body.file_name, body.folder_id)
          .flatMap { data =>
            val db_request = data match {
              case Some(id) => update_file_metadata(body, id)
              case None     => create_file_metadata(body)
            }

            db_request.flatMap {
              case Left(err) =>
                BadRequest(ErrorResponse(s"Error occurred: $err").asJson)
              case Right(file_id) =>
                val token =
                  jwt.encode_token(UploadBody(file_id))
                token match {
                  case Left(err) =>
                    IO.println(err) *> InternalServerError(
                      "Internal Server Error"
                    )
                  case Right(token_data) =>
                    Ok(
                      FileUploadMetadataInserted(
                        "File Metadata Inserted",
                        token_data,
                        s"${config.SERVICE_URL}:${config.SERVICE_PORT}/chunk/upload",
                        s"${config.SERVICE_URL}:${config.SERVICE_PORT}/file/upload/complete",
                        config.CHUNK_SIZE
                      ).asJson
                    )
                }
            }
          }
    }

  def upload_complete_curried(
      is_file_chunks_uploaded: FileId => IO[Boolean],
      file_complete_status: FileId => IO[Unit]
  )(token: String): IO[Response[IO]] =
    jwt.decode_token[UploadBody](token) match {
      case Left(err) => BadRequest(ErrorResponse("Invalid body").asJson)
      case Right(UploadBody(file_id)) =>
        is_file_chunks_uploaded(file_id).flatMap {
          case false =>
            NotFound(ErrorResponse("Chunks are not uploaded").asJson)
          case true =>
            set_file_status_uploaded(file_id) *>
              Ok()
        }
    }

  def upload_complete(body: FileCompletionBody) =
    upload_complete_curried(
      are_file_chunks_uploaded,
      set_file_status_uploaded
    )(body.token)

  def delete_file(body: FileDeletionBody): IO[Response[IO]] =
    remove_file_chunks(body.file_id, body.user_id)
      .flatMap(_ => delete_file_metadata(body.file_id, body.user_id))
      .flatMap {
        case true => Ok()
        case false =>
          NotFound(ErrorResponse("File Not found or already deleted").asJson)
      }
}
