package backend.core
import backend.utils.AnalyticsUtils
import backend.utils.QuizUtils
object QuizEngine {
  def startQuiz(topic: String): String = {
    val questions = QuizUtils.selectQuizQuestions(topic)
    if (questions.isEmpty) return "No quiz available for that topic. Try 'scala'."

    val results = questions.map { case (q, options, correct) =>
      val formattedQ = s"$q\nOptions: ${options.mkString(", ")}"
      println(formattedQ) // Or pass to front-end
      val userAnswer = scala.io.StdIn.readLine("> ") // Replace with real input hook in prod
      val isCorrect = QuizUtils.evaluateQuizAnswer(userAnswer, correct)
      AnalyticsUtils.logInteraction(q, s"User answered: $userAnswer | Correct: $correct")
      isCorrect
    }

    QuizUtils.summarizeQuizResults(results)
  }
}
