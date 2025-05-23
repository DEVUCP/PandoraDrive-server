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
    original_req
      .as[Json]
      .flatMap { json =>
        val enhanced =
          json.asObject.map(_.add("user_id", Json.fromInt(user_id)))

        enhanced match {
          case Some(obj) =>
            val newReq = original_req.withEntity(obj.asJson)
            IO.pure(newReq)

          case None =>
            IO.pure(original_req) // Return the original request unmodified
        }
      }
      .handleErrorWith(error =>
        IO.println(error.getMessage()) *> IO.pure(original_req)
      )

  def routeRequestImpl[T](req: Request[IO], uriString: String, method: Method)(
      implicit
      decoder: EntityDecoder[IO, T],
      encoder: EntityEncoder[IO, T]
  ): IO[Response[IO]] = {
    EmberClientBuilder.default[IO].build.use { client =>
      Uri.fromString(uriString) match {
        case Right(uri) =>
          val updatedUri = uri.withQueryParams(req.uri.query.params)
          val modifiedReq = {
            val baseReq = req.withUri(updatedUri).withMethod(method)
            if (uri.host.exists(_.value.contains('_'))) {
              val filteredHeaders =
                baseReq.headers.headers.filterNot(_.name == CIString("Host"))
              baseReq.withHeaders(filteredHeaders)
            } else {
              baseReq
            }
          }

          client
            .run(modifiedReq)
            .use(resp => IO.pure(resp))
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
