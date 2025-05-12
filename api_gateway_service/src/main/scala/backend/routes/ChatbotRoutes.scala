package backend.routes

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.AuthedRoutes
import model.User.AuthUser
import backend.middlewares.AuthMiddleware
import org.http4s.dsl.io._

object ChatbotRoutes {
  val routesWithAuth: AuthedRoutes[AuthUser, IO] = AuthedRoutes.of {
    case GET -> Root as _ =>
      Ok("List of chatbot")
    case POST -> Root / "chat" as _ =>
      Ok(s"Chat with chatbot")
    case DELETE -> Root / "chat" / chatbotId as _ =>
      Ok(s"Chatbot $chatbotId deleted")
  }

  val routes = AuthMiddleware(routesWithAuth)
}
