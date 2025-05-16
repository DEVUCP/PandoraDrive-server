package backend.routes

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import model.User.{add_user, get_all_users, remove_user}

object AdminRoutes {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root / "users" =>
      for {
        users <- get_all_users() // Extract the List[User] from IO
        response <- Ok(users.mkString("\n")) // Now we can use mkString
      } yield response

    case POST -> Root / "user" / username / pass =>
      add_user(username, pass).attempt.flatMap {
        case Right(_) => Ok(s"Successfully added user $username")
        case Left(e) =>
          InternalServerError(s"Failed to add user $username: ${e.getMessage}")
      }

    case DELETE -> Root / "user" / userId =>
      remove_user(userId.toInt).attempt.flatMap {
        case Right(_) => Ok(s"User removed successfully")
        case Left(e) =>
          InternalServerError(s"Failed to remove user: ${e.getMessage}")
      }
  }
}
