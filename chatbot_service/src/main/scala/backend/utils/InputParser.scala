package backend.utils
object InputParser {
  def cleanInput(input: String): String =
    input.replaceAll("""[\p{Punct}]""", "").toLowerCase.trim
  def parseInput(input: String): List[String] =
    cleanInput(input).split("\\s+").toList
}
