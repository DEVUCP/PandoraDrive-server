package backend.routes

import cats.effect.IO
import cats.implicits._
import org.http4s._
import org.http4s.dsl.io._
import com.comcast.ip4s.*
import org.typelevel.ci.CIString
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.multipart.Multipart

import io.circe.Json
import io.circe.generic.auto._
import org.http4s.implicits._

import org.http4s.HttpRoutes
import org.http4s.AuthedRoutes
import model.User.AuthUser
import backend.middlewares.AuthMiddleware
import org.http4s.dsl.io._
import utils.config
import utils.routing.{routeRequestJson, addUserIdToReq}
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


object ChatbotRoutes {

  val chatbot_service_port = config.CHATBOT_SERVICE_PORT

  case class ChatRequest(message: String)

  val routesWithAuth: AuthedRoutes[AuthUser, IO] = AuthedRoutes.of {
    case req @ POST -> Root / "chat" as user =>
      addUserIdToReq(req.req, user.id).flatMap { req =>
        routeRequestJson(
          req,
          s"http://localhost:$chatbot_service_port/chat",
          Method.POST
        )
      }

  }

  val routes = AuthMiddleware(routesWithAuth)
}
