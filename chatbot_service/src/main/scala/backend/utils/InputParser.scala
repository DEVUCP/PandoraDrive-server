package backend.utils
object InputParser {
  def cleanInput(input: String): String = {
    input.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase.trim
  }
  def parseInput(input: String): List[String] = {
    cleanInput(input).split("\\s+").toList
  }
}