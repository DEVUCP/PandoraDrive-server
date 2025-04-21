package utils

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import utils.config
import io.circe.syntax._
import io.circe.Encoder
import io.circe.Decoder
import io.circe.parser.decode
import io.circe.KeyDecoder.decodeKeyString
import java.time.Clock
import io.circe.Decoder

object jwt {
  private val JWT_ALGORITHM = JwtAlgorithm.HS256
  private val secret: String = {
    val secret = config.get("JWT_SECRET").flatten
    secret match {
      case None =>
        println("Warning: JWT_SECRET is missing")
        ""
      case Some(jwt_secret) => jwt_secret
    }
  }
  private val expirySeconds: Int = {
    val expirySecondsStr: Option[String] =
      config.get("JWT_EXPIRY_IN_SECONDS").flatten
    expirySecondsStr
      .flatMap(s => scala.util.Try(s.toInt).toOption)
      .getOrElse {
        println(
          "Warning: JWT_EXPIRY_IN_SECONDS not set or invalid, using default 3600 seconds"
        )
        3600
      }
  }

  def create_token[A: io.circe.Encoder](payload: A): Option[String] = {
    if (secret.isEmpty()) return None

    implicit val clock: Clock = Clock.systemUTC()
    val json = payload.asJson.noSpaces
    val claim = JwtClaim(content = json).issuedNow
      .expiresIn(expirySeconds)

    Some(Jwt.encode(claim, secret, JWT_ALGORITHM))
  }

  def decode_token[A: io.circe.Decoder](token: String): Either[String, A] = {
    if (secret.isEmpty()) return Left("Invalid Secret")

    val decoded = Jwt.decode(token, secret, Seq(JWT_ALGORITHM))
    decoded match {
      case scala.util.Success(claim) =>
        decode[A](claim.content) match {
          case Right(value) => Right(value)
          case Left(error) =>
            Left(s"Failed to decode payload: ${error.getMessage}")
        }
      case scala.util.Failure(exception) =>
        Left(s"Invalid token: ${exception.getMessage}")
    }
  }
}
