package backend.core

import cats.effect.IO
import cats.effect.std.Random
import backend.models._
import backend.utils._

class ChatbotEngine( tokenCache: TokenCache, random: Random[IO]) {
  def handleUserInput(input: String, user_id: Int): IO[ChatbotResponse] = {
    for {
      tokens <- tokenCache.get
      parsed = InputParser.parseInput(input)
      response <- RuleEngine.process(parsed, tokens, user_id)(random)
    } yield response
  }
}

object ChatbotEngine {
  def create(tokenLoader: IO[ChatbotTokens]): IO[ChatbotEngine] = {
    for {
      random <- Random.scalaUtilRandom[IO]
      cache <- TokenCache.create(tokenLoader)
    } yield new ChatbotEngine(cache, random)
  }
}