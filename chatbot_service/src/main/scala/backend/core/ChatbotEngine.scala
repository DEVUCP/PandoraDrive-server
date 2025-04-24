package backend.core
import backend.utils.AnalyticsUtils
// import backend.utils.{InputParser, PatternUtils, QuizUtils, AnalyticsUtils, Preferences}
object ChatbotEngine {
  def greetUser(): String = "Welcome, User! How can I help you today?"
  def handleUserInput(input: String): String = {
    val cleanedInput = input.trim.toLowerCase
    cleanedInput match {
      case input if input.contains("hello") || input.contains("hi") || input.contains("hey") => ChatbotCore.generateResponse(input)
      case input if input.contains("quiz") => ChatbotCore.generateResponse(input)
      case input if input.contains("analytics") || input.contains("stats") => ChatbotCore.generateResponse(input)
      case input if input.contains("help") || input.contains("support") => ChatbotCore.generateResponse(input)
      case _ => ChatbotCore.generateResponse(input)
    }
  }
}