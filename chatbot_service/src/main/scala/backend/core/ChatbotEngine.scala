package backend.core

import backend.utils.{InputParser, PatternUtils, QuizUtils, AnalyticsUtils, UserPreferences}

object ChatbotEngine {
  def greetUser(): String = 
    "Welcome to PandoraDrive! Ask me about your files or start a quiz by typing 'start quiz'."

  def handleUserInput(input: String): String = {
    val cleaned = InputParser.cleanInput(input)

    cleaned match {
      case x if PatternUtils.isGreeting(x)       => greetUser()
      case x if PatternUtils.isQuizRequest(x)    => QuizEngine.startQuiz("scala")
      case x if PatternUtils.isAnalyticsRequest(x) =>
        val log = AnalyticsUtils.getInteractionLog()
        AnalyticsEngine.analyzeInteractions(log) + "\n" + AnalyticsEngine.analyzeQuizPerformance(log)
      case "set dark mode"                       =>
        UserPreferences.storeUserPreferences("dark mode")
        "Got it! Preference saved: dark mode"
      case "my preference" =>
        UserPreferences.getUserPreferences().getOrElse("No preference set.")
      case _ =>
        s"I'm not sure how to help with that. Try asking for a quiz or analytics!"
    }
  }
}
