package services
import cats.effect.unsafe.implicits.global
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
import io.circe.parser._
import io.circe.generic.auto._
import org.http4s.multipart.Multipart
import org.http4s.circe.CirceEntityCodec._
import org.http4s.server.Router

import types.ErrorResponse
import dto.ChunkMetadataMultipartUpload
import utils.jwt
import types.FileId
import dto.FileInitToken
import types.ChunkId
import model.chunk_exists
import model.{chunk_reference_add, create_chunk_metadata}
import utils.files
import utils.hash_chunk

object chunk_service {
  class InvalidMetadata extends Throwable

  private def process_upload_curried(
      chunk_exists: ChunkId => IO[Boolean],
      chunk_reference_add: ChunkId => IO[Unit],
      create_new_chunk: (ChunkId, Int) => IO[Unit],
      hash_chunk: Array[Byte] => IO[ChunkId],
      store_file: (ChunkId, Array[Byte]) => IO[Unit]
  )(metadata: Part[IO], chunk: Part[IO]): IO[Response[IO]] = {
    for {
      metadataBytes <- metadata.body.compile.to(Array)
      metadataStr = new String(metadataBytes, "UTF-8")
      parsed = parse(metadataStr).flatMap(_.as[ChunkMetadataMultipartUpload])
      response <- parsed match {
        case Left(_) =>
          BadRequest(ErrorResponse("Invalid request: Invalid Metadata body"))

        case Right(
              ChunkMetadataMultipartUpload(token, chunk_seq, chunk_size)
            ) =>
          jwt.decode_token[FileInitToken](token) match {
            case Left(_) =>
              BadRequest(ErrorResponse("Invalid or expired token"))

            case Right(FileInitToken(file_id)) =>
              for {
                chunk_bytes <- chunk.body.compile.to(Array)
                chunk_id <- hash_chunk(chunk_bytes)
                exists <- chunk_exists(chunk_id)
                response <-
                  if (exists) {
                    for {
                      _ <- chunk_reference_add(chunk_id)
                      resp <- Ok()
                    } yield resp
                  } else {
                    (for {
                      _ <- create_new_chunk(chunk_id, chunk_size)
                      _ <- store_file(chunk_id, chunk_bytes)
                    } yield ()).attempt.flatMap {
                      case Left(_) =>
                        InternalServerError(
                          ErrorResponse("Internal server error")
                        )
                      case Right(_) => Ok()
                    }
                  }
              } yield response
          }
      }
    } yield response
  }

  val upload_chunk = process_upload_curried(
    chunk_exists,
    (id) => chunk_reference_add(id, 1),
    create_chunk_metadata,
    hash_chunk,
    (chunk_id, bytes) =>
      files.store_file(
        bytes,
        s"${chunk_id.slice(0, 2)}/${chunk_id.slice(2, 4)}/${chunk_id.slice(4, chunk_id.length)}"
      )
  )
}
