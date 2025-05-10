package routes

import cats.implicits._

import cats.effect.{ExitCode, IO, IOApp, _}

import com.comcast.ip4s.*
import dto.{
  ChunkMetadataMultipartUpload,
  DTOFileDownloadBody,
  FileCompletionBody,
  FileCreationBody,
  FileDeletionBody,
  UploadBody
}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.*
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe._
import org.http4s.dsl.io.*
import org.http4s.implicits._
import org.http4s.multipart.{Multipart, _}
import org.http4s.server.Router
import services.file_service
import types.{ErrorResponse, FileUploadMetadataInserted}
import dto.FileRenameBody

val file_routes = HttpRoutes
  .of[IO] {
    case GET -> Root :? FolderIdQueryParamMatcher(id) =>
      file_service.folder_files(id)

    case GET -> Root :? FileIdQueryParamMatcher(id) =>
      file_service.file_by_id(id)

    case GET -> Root / "download" :? FileIdQueryParamMatcher(id) =>
      file_service.download_file_metadata(id)

    case req @ POST -> Root / "rename" =>
      req.as[FileRenameBody].attempt.flatMap {
        case Left(_)     => BadRequest(ErrorResponse("Invalid Body").asJson)
        case Right(body) => file_service.rename_file(body)
      }

    // TODO: Make sure somehow that the folder is created first, instead of falling into a db error
    case req @ POST -> Root / "upload" =>
      req.as[FileCreationBody].attempt.flatMap {
        case Left(error) =>
          BadRequest(ErrorResponse(s"Invalid body: ${error.getMessage}").asJson)

        case Right(body) =>
          file_service.upload_file_metadata(body)
      }

    case req @ POST -> Root / "upload" / "complete" =>
      req.as[FileCompletionBody].attempt.flatMap {
        case Left(_)     => BadRequest(ErrorResponse("Invalid Body").asJson)
        case Right(body) => file_service.upload_complete(body)
      }

    case req @ DELETE -> Root / "delete" =>
      req.as[FileDeletionBody].attempt.flatMap {
        case Left(_) => BadRequest(ErrorResponse("Invalid Body").asJson)
        case Right(body) =>
          file_service.delete_file(body)
      }
  }
