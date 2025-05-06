package services
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.*
import cats.effect.unsafe.implicits.global
import cats.implicits._
import com.comcast.ip4s.*
import dto.ChunkDownloadBody
import dto.ChunkMetadataMultipartUpload
import dto.FileCompletionBody
import dto.UploadBody
import io.circe.generic.auto._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import model.are_file_chunks_uploaded
import model.chunk_exists
import model.chunk_reference_add
import model.create_chunk_metadata
import model.create_file_chunk_link
import model.get_chunk_metadata
import org.http4s.MediaType
import org.http4s.*
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe._
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._
import org.http4s.multipart.Multipart
import org.http4s.multipart.*
import org.http4s.server.Router
import types.ChunkId
import types.ErrorResponse
import types.FileId
import utils.files
import utils.files.read_file
import utils.hash_chunk
import utils.jwt

object chunk_service {
  class InvalidMetadata extends Throwable
  def construct_file_name(chunk_id: ChunkId): String =
    s"${chunk_id.slice(0, 2)}/${chunk_id.slice(2, 4)}/${chunk_id.slice(4, chunk_id.length)}"

  def process_upload_curried(
      chunk_exists: ChunkId => IO[Boolean],
      chunk_reference_add: ChunkId => IO[Unit],
      create_new_chunk: (ChunkId, Int) => IO[Unit],
      hash_chunk: Array[Byte] => IO[ChunkId],
      store_file: (ChunkId, Array[Byte]) => IO[Unit],
      create_file_chunk_link: (FileId, ChunkId, Int) => IO[Unit]
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
          jwt.decode_token[UploadBody](token) match {
            case Left(err) => BadRequest(ErrorResponse("Invalid Body"))
            case Right(UploadBody(file_id)) =>
              for {
                chunk_bytes <- chunk.body.compile.to(Array)
                chunk_id <- hash_chunk(chunk_bytes)
                exists <- chunk_exists(chunk_id)
                response <-
                  if (exists) {
                    for {
                      _ <- chunk_reference_add(chunk_id)
                      _ <- create_file_chunk_link(
                        file_id,
                        chunk_id,
                        chunk_seq
                      )
                      resp <- Ok()
                    } yield resp
                  } else {
                    val result = for {
                      _ <- store_file(
                        chunk_id,
                        chunk_bytes
                      ) // NOTE: Storing files comes before saving them in the database
                      _ <- create_new_chunk(chunk_id, chunk_size)
                      _ <- create_file_chunk_link(file_id, chunk_id, chunk_seq)
                    } yield ()

                    result.attempt.flatMap {
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
    (chunk_id, bytes) => {
      files.store_file(
        bytes,
        construct_file_name(chunk_id)
      )
    },
    create_file_chunk_link
  )

  def download_chunk(
      chunk_id: ChunkId
  ): IO[Response[IO]] =
    get_chunk_metadata(chunk_id).flatMap {
      case Some(data) =>
        read_file(construct_file_name(chunk_id)).flatMap {
          case Right(stream) =>
            Ok(stream)
              .map(
                _.withContentType(
                  `Content-Type`(MediaType.application.`octet-stream`)
                )
              )
          case Left(msg) => BadRequest(ErrorResponse(msg))
        }
      case None =>
        BadRequest(ErrorResponse("Chunk with this ID doesn't exist"))
    }

}
