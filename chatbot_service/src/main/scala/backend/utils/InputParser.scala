package backend.utils

object InputParser {
  def cleanInput(input: String): String =
    input.replaceAll("""[^\w\s]""", "").toLowerCase.trim

  def parseInput(input: String): List[String] = {
    val cleaned = cleanInput(input)
    val words = cleaned.split("\\s+").toList

    (1 to 3).flatMap(n =>
      words.sliding(n).map(_.mkString(" ")))
        .toList
  }

  def matchesToken(parsed_input: List[String], tokens: List[String]): Boolean = {
    tokens.exists(token => parsed_input.contains(token.toLowerCase))
  }
}