package services

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.*
import cats.implicits._
import com.comcast.ip4s.*
import io.circe.generic.auto._
import io.circe.syntax._
import model.get_folder_metadata_by_folder_id
import org.http4s.*
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io.*
import org.http4s.dsl.io._
import org.http4s.ember.server.*
import org.http4s.ember.server._
import org.http4s.implicits._
import types.ErrorResponse
import types.FolderId

object folder_service {
  def get_folder_files_metadata(folder_id: FolderId): IO[Response[IO]] =
    get_folder_metadata_by_folder_id(folder_id).flatMap {
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
}
