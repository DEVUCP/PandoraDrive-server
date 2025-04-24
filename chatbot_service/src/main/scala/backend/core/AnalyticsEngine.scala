package backend.core

// import backend.utils.AnalyticsUtils

object AnalyticsEngine {
  def analyzeInteractions(log: List[(Int, String, String)]): String = {
    val total = log.length
    val quizCount = log.count(_._2 == "quiz")
    val analyticsCount = log.count(_._2 == "analytics")
    val greetingsCount = log.count(_._2 == "greeting")
    s"Total: $total, Quiz: $quizCount, Analytics: $analyticsCount, Greetings: $greetingsCount"
  }

  // TODO: Analyze quiz responses and return counts for total, correct, and incorrect answers
  def analyzeQuizPerformance(log: List[(Int, String, String)]): String = ???
}
