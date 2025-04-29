package backend.routes

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

object ChatbotRoutes {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      Ok("List of chatbot")
    case POST -> Root / "chat" =>
      Ok(s"Chat with chatbot")
    case DELETE -> Root / "chat" / chatbotId =>
      Ok(s"Chatbot $chatbotId deleted")
  

  } 
}
