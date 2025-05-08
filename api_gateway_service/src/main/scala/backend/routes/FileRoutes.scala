package backend.routes

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

object FileRoutes {

  val file_service_port = config.FILE_SERVICE_PORT

   def routeRequestImpl[T](req: Request[IO], uriString: String, method: Method)(
    implicit decoder: EntityDecoder[IO, T], 
    encoder: EntityEncoder[IO, T]
  ): IO[Response[IO]] = {
    EmberClientBuilder.default[IO].build.use { client =>
      Uri.fromString(uriString) match {
        case Right(uri) =>
          // modify request for file service
          val modifiedReq = {
            val baseReq = req.withUri(uri).withMethod(method)
            if (uri.host.exists(_.value.contains('_'))) {
              val filteredHeaders = baseReq.headers.headers.filterNot(_.name == CIString("Host"))
              baseReq.withHeaders(filteredHeaders)
            } else {
              baseReq
            }
          }
  
          client.expect[T](modifiedReq)
            .flatMap(Ok(_))
            .handleErrorWith {
              case e =>
                println(s"Error during request to $uriString: $e")
                InternalServerError("Internal Server Error")
            }
        
        case Left(parseFailure) =>
          println(s"Invalid URI '$uriString': ${parseFailure.details}")
          InternalServerError("Invalid URI")
      }
    }
  }
  
  def routeRequestJson(req: Request[IO], URI: String, method : Method): cats.effect.IO[org.http4s.Response[cats.effect.IO]] = {
    routeRequestImpl[Json](req, URI, method)
  }

  def routeRequestString(req: Request[IO], URI: String, method : Method): cats.effect.IO[org.http4s.Response[cats.effect.IO]] = {
    routeRequestImpl[String](req, URI, method)
  }

  object FileIdQueryParamMatcher extends QueryParamDecoderMatcher[String]("file_id")
  object FolderIdQueryParamMatcher extends QueryParamDecoderMatcher[String]("folder_id")

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    
    case req @ GET -> Root / "ping" =>
      routeRequestJson(req, s"http://file:$file_service_port/ping", Method.GET)
    
    case req @ POST -> Root / "upload" / "init" => 
      routeRequestJson(req, s"http://file:$file_service_port/file/upload", Method.POST)

    case req @ GET -> Root / "download" / "init" :? FileIdQueryParamMatcher(fileId) =>
      routeRequestJson(req, s"http://file:$file_service_port/file/download?file_id=$fileId", Method.GET)
    
    case req @ GET -> Root :? FileIdQueryParamMatcher(fileId) =>
      routeRequestJson(req, s"http://file:$file_service_port/file?file_id=$fileId", Method.GET)

    case req @ GET -> Root :? FolderIdQueryParamMatcher(folderId) =>
      routeRequestJson(req, s"http://file:$file_service_port/file/?folder_id=$folderId", Method.GET)
    

    case req @ DELETE -> Root / "delete" :?  FileIdQueryParamMatcher(fileId) =>
      routeRequestJson(req, s"http://file:$file_service_port/file/delete?file_id=$fileId", Method.DELETE)
  } 
}