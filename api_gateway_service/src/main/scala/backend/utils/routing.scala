package utils

import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.ember.client.EmberClientBuilder
import com.comcast.ip4s.*
import org.typelevel.ci.CIString

import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.multipart.Multipart

import io.circe.Json
import io.circe.generic.auto._
import org.http4s.implicits._
import utils.config
import io.circe.syntax.EncoderOps

object routing {

  def addUserIdToReq(original_req: Request[IO], user_id: Int): IO[Request[IO]] =
    original_req.as[Json].flatMap { json =>
      val enhanced = json.asObject match {
        case Some(obj) => obj.add("user_id", Json.fromInt(user_id))
      }

      val newReq = original_req
        .withEntity(enhanced.asJson) // Convert JsonObject back to Json
      IO.pure(newReq)
    }

  def routeRequestImpl[T](req: Request[IO], uriString: String, method: Method)(
      implicit
      decoder: EntityDecoder[IO, T],
      encoder: EntityEncoder[IO, T]
  ): IO[Response[IO]] = {
    EmberClientBuilder.default[IO].build.use { client =>
      Uri.fromString(uriString) match {
        case Right(uri) =>
          // modify request for file service
          val modifiedReq = {
            val baseReq = req.withUri(uri).withMethod(method)
            if (uri.host.exists(_.value.contains('_'))) {
              val filteredHeaders =
                baseReq.headers.headers.filterNot(_.name == CIString("Host"))
              baseReq.withHeaders(filteredHeaders)
            } else {
              baseReq
            }
          }

          client
            .expect[T](modifiedReq)
            .flatMap(Ok(_))
            .handleErrorWith { case e =>
              println(s"Error during request to $uriString: $e")
              InternalServerError("Internal Server Error")
            }

        case Left(parseFailure) =>
          println(s"Invalid URI '$uriString': ${parseFailure.details}")
          InternalServerError("Invalid URI")
      }
    }
  }

  def routeRequestJson(
      req: Request[IO],
      URI: String,
      method: Method
  ): cats.effect.IO[org.http4s.Response[cats.effect.IO]] = {
    routeRequestImpl[Json](req, URI, method)
  }

  def routeRequestString(
      req: Request[IO],
      URI: String,
      method: Method
  ): cats.effect.IO[org.http4s.Response[cats.effect.IO]] = {
    routeRequestImpl[String](req, URI, method)
  }
}

