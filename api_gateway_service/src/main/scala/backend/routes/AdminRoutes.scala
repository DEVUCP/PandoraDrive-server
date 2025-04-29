package backend.routes

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

object AdminRoutes {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "users" => 
      Ok("List of users")
    case POST -> Root / "user" / username => 
      Ok(s"User details for: $username")

    case DELETE -> Root / "user" / username =>
      Ok(s"User $username deleted")
  } 
}
