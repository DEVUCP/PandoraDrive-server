package backend.routes

import cats.effect.IO
import cats.implicits._
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
import org.http4s.server.Router
import utils.config
import utils.routing.{routeRequestJson, addUserIdToReq}

import org.http4s.AuthedRoutes
import model.User.AuthUser
import backend.middlewares.AuthMiddleware

object FileRoutes {

  val file_service_port = config.FILE_SERVICE_PORT

  val routesWithAuth: AuthedRoutes[AuthUser, IO] = AuthedRoutes.of {
    case req @ GET -> Root as user =>
      routeRequestJson(
        req.req,
        s"http://localhost:$file_service_port/file",
        Method.GET
      )

    case req @ GET -> Root / "ping" as user =>
      addUserIdToReq(req.req, user.id).flatMap { req =>
        routeRequestJson(
          req,
          s"http://localhost:$file_service_port/ping",
          Method.GET
        )
      }

    case req @ POST -> Root / "upload" as user =>
      addUserIdToReq(req.req, user.id).flatMap { req =>
        routeRequestJson(
          req,
          s"http://localhost:$file_service_port/file/upload",
          Method.POST
        )
      }

    case req @ GET -> Root / "download" / "init" as user =>
      addUserIdToReq(req.req, user.id).flatMap { req =>
        routeRequestJson(
          req,
          s"http://localhost:$file_service_port/file/download",
          Method.GET
        )
      }

    case req @ DELETE -> Root / "delete" as user =>
      addUserIdToReq(req.req, user.id).flatMap { req =>
        routeRequestJson(
          req,
          s"http://localhost:$file_service_port/file/delete",
          Method.DELETE
        )
      }

    case req @ GET -> Root / "folder" / "root" as user =>
      routeRequestJson(
        req.req,
        s"http://localhost:$file_service_port/folder?user_id=${user.id}",
        Method.GET
      )

    case req @ GET -> Root / "folder" as user =>
      routeRequestJson(
        req.req,
        s"http://localhost:$file_service_port/folder",
        Method.GET
      )
  }

  val routes = AuthMiddleware(routesWithAuth)
}
