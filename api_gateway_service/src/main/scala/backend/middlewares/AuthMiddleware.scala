package backend.middlewares

import cats.effect.IO
import cats.implicits._
import org.http4s._
import org.http4s.server._
import model.User
import model.User.{get_user_by_id, AuthUser}
import utils.jwt.{decode_token}
import io.circe.generic.auto._
import io.circe.parser._
import cats.data.OptionT
import org.http4s.headers.Cookie
import org.http4s.dsl.io._

object AuthMiddleware {

  private def extractSessionCookie(request: Request[IO]): IO[Option[String]] = IO {
    request.headers.get[Cookie]
      .flatMap(_.values.find(_.name == "session"))
      .map(_.content)
  }

  private def verifyUser(token: String): IO[Option[AuthUser]] = {
    decode_token[AuthUser](token) match {
      case Right(claim) => get_user_by_id(claim.id).map {
          case Some(user) => Some(AuthUser(user.userId, user.username))
          case None => None
        }
      case Left(_) => IO.pure(None)
    }
  }

  def apply(routes: AuthedRoutes[AuthUser, IO]): HttpRoutes[IO] = {
    HttpRoutes.of[IO] { request =>
      OptionT(extractSessionCookie(request))
        .flatMapF(verifyUser)
        .flatMap(user => routes(AuthedRequest(user, request)))
        .getOrElseF(Forbidden("Authentication required"))
    }
  }
}