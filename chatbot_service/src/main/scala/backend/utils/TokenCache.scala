package backend.utils

import cats.effect.IO
import cats.effect.Ref
import backend.models.ChatbotTokens

class TokenCache private ( private val ref: Ref[IO, Option[ChatbotTokens]], loader: IO[ChatbotTokens]) {
  def get: IO[ChatbotTokens] = ref.get.flatMap {
    case Some(tokens) => IO.pure(tokens)
    case None => loader.flatTap(tokens => ref.set(Some(tokens)))
  }

  def refresh: IO[Unit] = loader.flatMap(tokens => ref.set(Some(tokens)))
}

object TokenCache {
  def create(loader: IO[ChatbotTokens]): IO[TokenCache] = {
    Ref.of[IO, Option[ChatbotTokens]](None).map { ref =>
      new TokenCache(ref, loader)
    }
  }
}