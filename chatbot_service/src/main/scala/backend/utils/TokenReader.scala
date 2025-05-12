package backend.utils

import io.circe.parser
import cats.effect.IO
import backend.models.ChatbotTokens
import java.nio.file.{Files, Paths, NoSuchFileException}
import scala.util.{Try, Success, Failure}
import io.circe.generic.auto.deriveDecoder
import backend.utils.config

object TokenReader {

  def ParseTokens(filePath: String): Either[String, ChatbotTokens] = {
    val fileContent = Try(Files.readString(Paths.get(filePath))) match {
      case Success(content) => Right(content)
      case Failure(_: NoSuchFileException) => Left(s"File not found: $filePath")
      case Failure(ex) => Left(s"File read error: ${ex.getMessage}")
    }

    fileContent.flatMap { jsonString =>
      parser.decode[ChatbotTokens](jsonString)
        .left.map(err => s"JSON parse error: ${err.getMessage}")
    }
  }

  def GetAllTokens(): Either[RuntimeException, ChatbotTokens] = {
    ParseTokens(config.TOKENS_FILE_PATH) match {
      case Right(tokens) => Right(tokens)
      case Left(error) => Left(RuntimeException(s"Failed to load tokens: $error"))
    }
  }

}