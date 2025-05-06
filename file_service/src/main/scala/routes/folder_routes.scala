package routes

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.*
import cats.implicits._
import com.comcast.ip4s.*
import dto.DTOFolderCreationBody
import io.circe.generic.auto._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe.syntax._
import model.get_folder_metadata_by_folder_id
import org.http4s.*
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe._
import org.http4s.dsl.io.*
import org.http4s.dsl.io._
import org.http4s.ember.server.*
import org.http4s.ember.server._
import org.http4s.implicits._
import org.http4s.server.Router
import schema.initialize_schemas
import services.folder_service
import types.ErrorResponse

val folder_routes = HttpRoutes.of[IO] {
  case GET -> Root :? FolderIdQueryParamMatcher(id) =>
    folder_service.get_folder_files_metadata(id)

  case GET -> Root :? UserIdQueryParamMatcher(id) =>
    folder_service.get_user_root_folder(id)

  case req @ POST -> Root / "upload" =>
    req.as[DTOFolderCreationBody].attempt.flatMap {
      case Left(err)   => BadRequest(ErrorResponse("Invalid body").asJson)
      case Right(body) => folder_service.create_folder_metadata(body)
    }
}
