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
import utils.routing.routeRequestJson


object FileRoutes {

  val file_service_port = config.FILE_SERVICE_PORT

  object FileIdQueryParamMatcher extends QueryParamDecoderMatcher[String]("file_id")
  object FolderIdQueryParamMatcher extends QueryParamDecoderMatcher[String]("folder_id")
  object UserIddQueryParamMatcher extends QueryParamDecoderMatcher[String]("user_id")

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

    case req @ DELETE -> Root / "delete" =>
      routeRequestJson(req, s"http://file:$file_service_port/file/delete", Method.DELETE)

    case req @ GET -> Root / "folder" :? UserIddQueryParamMatcher(userId) =>
      routeRequestJson(req, s"http://file:$file_service_port/folder?user_id=$userId", Method.GET)
    
  } 
}