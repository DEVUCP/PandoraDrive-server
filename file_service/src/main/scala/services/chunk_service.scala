package services
import cats.effect.unsafe.implicits.global
import java.security.MessageDigest // Used for hashing
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

def process_upload(metadata: Part[IO], chunk: Part[IO]): IO[Response[IO]] = {
  for {
    metadataBytes <- metadata.body.compile.to(Array)
    metadataStr = new String(metadataBytes, "UTF-8")
    parsed = parse(metadataStr).flatMap(_.as[ChunkMetadataMultipartUpload])
    response <- parsed match {
      case Left(_) =>
        BadRequest(ErrorResponse("Invalid request: Invalid Metadata body"))
      case Right(ChunkMetadataMultipartUpload(token, chunkSeq, chunkSize)) =>
        jwt.decode_token[FileInitToken](token) match {
          case Left(_) =>
            BadRequest(ErrorResponse("Invalid or expired token"))
          case Right(FileInitToken(file_id)) =>
            for {
              chunkBytes <- chunk.body.compile.to(Array)
              digest = MessageDigest.getInstance("SHA-256")
              hashBytes = digest.digest(chunkBytes)
              chunkId = hashBytes.map("%02x".format(_)).mkString

              exists <- chunk_exists(chunkId)
              response <- exists match {
                case true => // Let's add the reference count
                  println("It does exist")
                  for {
                    _ <- chunk_reference_add(chunkId, 1)
                    resp <- Ok()
                  } yield resp
                case false =>
                  for {
                    result <- handle_new_chunk(
                      chunkId,
                      chunkSize,
                      chunkBytes
                    ).attempt
                    resp <- result match {
                      case Left(err) =>
                        InternalServerError(
                          ErrorResponse(
                            s"Internal server error"
                          )
                        )
                      case Right(_) => Ok()
                    }
                  } yield resp
              }
            } yield response
        }
    }
  } yield response
}

def handle_new_chunk(
    chunk_id: ChunkId,
    chunkSize: Int,
    chunkBytes: Array[Byte]
): IO[Unit] =
  create_chunk_metadata(chunk_id, chunkSize).attempt
    .flatMap {
      case Right(_) => IO(true) // Success case
      case Left(err) => {
        IO.println(err) *>
          IO(false)
      }
    } *>
    files.store_file(
      chunkBytes,
      s"${chunk_id.slice(0, 2)}/${chunk_id.slice(2, 4)}/${chunk_id.slice(4, chunk_id.length)}"
    )
