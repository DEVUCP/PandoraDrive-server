package routes

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.*
import cats.implicits._
import com.comcast.ip4s.*
import dto.ChunkMetadataMultipartUpload
import dto.DTOFileDownloadBody
import dto.FileCompletionBody
import dto.FileCreationBody
import dto.UploadBody
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.*
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe._
import org.http4s.dsl.io.*
import org.http4s.implicits._
import org.http4s.multipart.Multipart
import org.http4s.multipart.*
import org.http4s.server.Router
import services.file_service
import types.ErrorResponse
import types.FileUploadMetadataInserted

val file_routes = HttpRoutes
  .of[IO] {
    case GET -> Root :? FolderIdQueryParamMatcher(id) =>
      file_service.folder_files(id)

    case GET -> Root :? FileIdQueryParamMatcher(id) =>
      file_service.file_by_id(id)

    case GET -> Root / "download" :? FileIdQueryParamMatcher(id) =>
      file_service.download_file_metadata(id)

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

    case DELETE -> Root / "delete" :? FileIdQueryParamMatcher(id) =>
      file_service.delete_file(id)
  }
