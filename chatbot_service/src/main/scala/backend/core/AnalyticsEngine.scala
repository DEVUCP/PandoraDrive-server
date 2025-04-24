package backend.core
object AnalyticsEngine {
  def analyzeInteractions(log: List[(Int, String, String)]): String = {
    val total = log.length
    val quizCount = log.count(_._2 == "quiz")
    val analyticsCount = log.count(_._2 == "analytics")
    val greetingsCount = log.count(_._2 == "greeting")
    s"Total: $total, Quiz: $quizCount, Analytics: $analyticsCount, Greetings: $greetingsCount"
  }
  def analyzeQuizPerformance(log: List[(Int, String, String)]): String = {
    val quizResponses = log.filter(_._2 == "quiz")
    val (correct, incorrect) = quizResponses.map(_._3.toLowerCase).partition(_ == "correct")
    val total = quizResponses.length
    val correctCount = correct.length
    val incorrectCount = incorrect.length
    s"Total Quiz Responses: $total, Correct: $correctCount, Incorrect: $incorrectCount"
  }
}