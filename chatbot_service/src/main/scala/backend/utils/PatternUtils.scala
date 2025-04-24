package backend.utils
object PatternUtils {
  def isGreeting(input: String): Boolean = {
    val words = input.toLowerCase.split("\\s+").toSet
    greetings.exists(words.contains)
  }
  def isQuizRequest(input: String): Boolean = {
    input.toLowerCase.contains("quiz")
  }
  // TODO: Check if the input contains the word "analytics"
  def isAnalyticsRequest(input: String): Boolean = ???
}