package backend.utils
object PatternUtils {
  def isGreeting(input: String): Boolean = {
    val words = input.toLowerCase.split("\\s+").toSet
    greetings.exists(words.contains)
  }
  def isQuizRequest(input: String): Boolean = {
    input.toLowerCase.contains("quiz")
  }
  def isAnalyticsRequest(input: String): Boolean = {
    input.toLowerCase.contains("analytics")
  }
}