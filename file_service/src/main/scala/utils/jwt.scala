package utils

import java.time.Clock

import io.circe.KeyDecoder.decodeKeyString
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import utils.config

object jwt {
  private val JWT_ALGORITHM = JwtAlgorithm.HS256

  def encode_token[A: io.circe.Encoder](payload: A): Either[String, String] = {
    if (config.JWT_SECRET.isEmpty()) return Left("Empty JWT_SECRET");

    implicit val clock: Clock = Clock.systemUTC()
    val json = payload.asJson.noSpaces
    val claim = JwtClaim(content = json).issuedNow
      .expiresIn(config.JWT_EXPIRY_IN_SECONDS)

    Right(Jwt.encode(claim, config.JWT_SECRET, JWT_ALGORITHM))
  }

  def decode_token[A: io.circe.Decoder](token: String): Either[String, A] = {
    if (config.JWT_SECRET.isEmpty()) return Left("Invalid Secret")

    val decoded = Jwt.decode(token, config.JWT_SECRET, Seq(JWT_ALGORITHM))
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
