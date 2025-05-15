package routes

import cats.implicits._

import cats.effect.{ExitCode, IO, IOApp, _}

import com.comcast.ip4s.*
import dto.ChunkDownloadBody
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.*
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe._
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.implicits._
import org.http4s.multipart.{Multipart, _}
import org.http4s.server.Router
import services.chunk_service
import services.chunk_service.{download_chunk, upload_chunk}
import types.ErrorResponse

val chunk_routes = HttpRoutes
  .of[IO] {
    case req @ POST -> Root / "upload" =>
      EntityDecoder
        .mixedMultipartResource[IO]()
        .use(decoder =>
          println(decoder)
          req.decodeWith(decoder, strict = true)(multipart =>
            val chunk_metadata =
              multipart.parts.find(_.name.contains("metadata"))
            val chunk = multipart.parts.find(_.name.contains("chunk"))

            (chunk_metadata, chunk) match {
              case (_, None) =>
                BadRequest(
                  ErrorResponse("Invalid request: Chunk is not uploaded")
                )
              case (None, _) =>
                BadRequest(
                  ErrorResponse("Invalid request: No Metadata Received")
                )
              case (Some(metadata), Some(chunk)) =>
                IO.println("Beginning to upload a new chunk") *>
                  chunk_service.upload_chunk(metadata, chunk)
            }
          )
        )
    case req @ GET -> Root / "download" =>
      req.as[ChunkDownloadBody].attempt.flatMap {
        case Left(err) => BadRequest(ErrorResponse("Invalid Body"))
        case Right(ChunkDownloadBody(chunk_id)) =>
          download_chunk(chunk_id)
      }
  }
