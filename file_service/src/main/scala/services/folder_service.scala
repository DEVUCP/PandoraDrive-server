package services

import cats.data.EitherT
import cats.implicits._

import cats.effect.{ExitCode, IO, IOApp, _}

import com.comcast.ip4s.*
import dto.{
  FolderCreationBody,
  FolderDeletionBody,
  FolderMoveBody,
  FolderRenameBody
}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.ember.server._
import org.http4s.implicits._
import types.{ErrorResponse, FolderId}

object folder_service {
  def get_folder_files_metadata(folder_id: FolderId): IO[Response[IO]] =
    model.get_folder_metadata_by_folder_id(folder_id).flatMap {
      case Right(folder) =>
        Ok(folder.asJson)

      case Left(errMsg) =>
        errMsg match {
          case msg if msg.startsWith("No folder exists") =>
            NotFound(ErrorResponse(msg).asJson)
          case other =>
            BadRequest(ErrorResponse(other).asJson)
        }
    }

  def create_folder_metadata(body: FolderCreationBody): IO[Response[IO]] =
    model
      .create_folder(
        body.folder_name,
        Some(body.parent_folder_id),
        body.user_id
      )
      .flatMap {
        case Right(folder_id) =>
          Ok(folder_id.asJson)

        case Left(errMsg) if errMsg.contains("already exists") =>
          Conflict(ErrorResponse(errMsg).asJson)

        case Left(errMsg) =>
          InternalServerError(ErrorResponse(errMsg).asJson)
      }

  def get_user_root_folder(user_id: Int): IO[Response[IO]] =
    model
      .get_root_folder_by_user_id(user_id)
      .flatMap {
        case Right(folder) => Ok(folder.asJson)
        case Left(err) =>
          IO.println(err) *> InternalServerError(
            ErrorResponse("Internal Server Error").asJson
          )
      }

  def get_children_folders(folder_id: FolderId): IO[Response[IO]] =
    model.get_folder_by_parent_id(folder_id).flatMap { folders =>
      Ok(folders.asJson)
    }

  def delete_folder(body: FolderDeletionBody): IO[Response[IO]] =
    model
      .delete_folder_by_id(body.folder_id, body.user_id)
      .flatMap {
        case true => Ok()
        case false =>
          NotFound(
            ErrorResponse(
              "Failed to delete folder. Maybe folder doesn't exist or already deleted"
            ).asJson
          )
      }
      .handleErrorWith(err =>
        IO.println(err) *> InternalServerError(
          ErrorResponse("Internal Server Error").asJson
        )
      )

  def rename_folder(body: FolderRenameBody): IO[Response[IO]] =
    model
      .rename_folder(body.folder_id, body.user_id, body.new_folder_name)
      .flatMap {
        case true => Ok()
        case false =>
          NotFound(
            ErrorResponse(
              "Failed to delete folder. Maybe folder doesn't exist or already deleted"
            ).asJson
          )
      }
      .handleErrorWith(err =>
        IO.println(err) *> InternalServerError(
          ErrorResponse("Internal Server Error").asJson
        )
      )

  def move_folder(body: FolderMoveBody): IO[Response[IO]] =
    model
      .move_folder(body.folder_id, body.user_id, body.new_folder_id)
      .flatMap {
        case true => NoContent()
        case false =>
          NotFound(
            ErrorResponse(
              "Failed to delete folder. Maybe folder doesn't exist or already deleted"
            ).asJson
          )
      }
      .handleErrorWith(err =>
        IO.println(err) *> InternalServerError(
          ErrorResponse("Internal Server Error").asJson
        )
      )
}
