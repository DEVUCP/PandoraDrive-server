package backend.routes

import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.multipart._
import org.http4s.circe._
import io.circe.generic.auto._
import model.User.{get_user_by_username, AuthUser}
import utils.jwt
import cats.syntax.all._
import org.http4s.MediaType
import io.circe.Json
import io.circe.syntax._

object AuthRoutes {

  private def handleLogin(
      username: String,
      password: String
  ): IO[Response[IO]] = {
    get_user_by_username(username).attempt.flatMap {
      case Right(Some(user)) if user.password == password =>
        jwt.encode_token(AuthUser(user.userId, user.username)) match {
          case Right(session) =>
            Ok("User logged in successfully")
              .map(
                _.addCookie(
                  ResponseCookie("session", session, path = Some("/"))
                )
              )
          case Left(e) =>
            InternalServerError(s"Token generation failed: ${e}")
        }
      case Right(_) =>
        NotFound("User not found")
      case Left(e) =>
        InternalServerError(s"Database error: ${e.getMessage}")
    }
  }

  private def processForm(form: UrlForm): IO[Response[IO]] = {
    (
      form.values.get("username").flatMap(_.headOption),
      form.values.get("password").flatMap(_.headOption)
    ) match {
      case (Some(u), Some(p)) => handleLogin(u, p)
      case _ => BadRequest("Both username and password are required")
    }
  }

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "login" =>
      req.contentType match {
        case Some(ct)
            if ct.mediaType == MediaType.application.`x-www-form-urlencoded` =>
          req.as[UrlForm].attempt.flatMap {
            case Right(form) => processForm(form)
            case Left(e) => BadRequest(s"Invalid form data: ${e.getMessage}")
          }
        case _ =>
          BadRequest("Content-Type must be application/x-www-form-urlencoded")
      }
  }
}

