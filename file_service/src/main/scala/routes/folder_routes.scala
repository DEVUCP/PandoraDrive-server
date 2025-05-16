package routes

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
import io.circe.parser._
import io.circe.syntax._
import model.get_folder_metadata_by_folder_id
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.ember.server._
import org.http4s.implicits._
import org.http4s.server.Router
import services.folder_service
import types.ErrorResponse

val folder_routes = HttpRoutes.of[IO] {
  case GET -> Root :? FolderIdQueryParamMatcher(id) =>
    folder_service.get_folder_files_metadata(id)

  case GET -> Root / "root" :? UserIdQueryParamMatcher(id) =>
    folder_service.get_user_root_folder(id)

  case GET -> Root :? ParentFolderIdQueryParamMatcher(id) =>
    folder_service.get_children_folders(id)

  case req @ POST -> Root / "upload" =>
    req.as[FolderCreationBody].attempt.flatMap {
      case Left(err)   => BadRequest(ErrorResponse("Invalid body").asJson)
      case Right(body) => folder_service.create_folder_metadata(body)
    }

  case req @ PUT -> Root / "rename" =>
    req.as[FolderRenameBody].attempt.flatMap {
      case Left(err)   => BadRequest(ErrorResponse("Invalid body").asJson)
      case Right(body) => folder_service.rename_folder(body)
    }

  case req @ PUT -> Root / "move" =>
    req.as[FolderMoveBody].attempt.flatMap {
      case Left(err)   => BadRequest(ErrorResponse("Invalid body").asJson)
      case Right(body) => folder_service.move_folder(body)
    }

  case req @ DELETE -> Root / "delete" =>
    req.as[FolderDeletionBody].attempt.flatMap {
      case Left(err)   => BadRequest(ErrorResponse("Invalid body").asJson)
      case Right(body) => folder_service.delete_folder(body)
    }
}
