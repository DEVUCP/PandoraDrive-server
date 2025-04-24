package backend.utils
object InputParser {
  def cleanInput(input: String): String = {
    input.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase.trim
  }
  // TODO: Tokenizes input string into a list of words
  def parseInput(input: String): List[String] = ???
}