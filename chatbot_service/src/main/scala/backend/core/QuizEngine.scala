package backend.core
import backend.utils.AnalyticsUtils

// import backend.utils.QuizUtils
object QuizEngine {
  private val sampleQuizzes = Map(
    "scala" -> List(
      ("What is a case class in Scala?", "A special class for immutable data"),
      ("What does 'val' define?", "An immutable variable"),
      ("What collection is immutable by default?", "List")
    ),
    "general" -> List(
      ("What is the capital of France?", "Paris"),
      ("How many continents are there?", "7"),
      ("What gas do plants absorb?", "Carbon dioxide")
    )
  )
  def startQuiz(topic: String): String = {
    val quiz = sampleQuizzes.getOrElse(topic.toLowerCase, sampleQuizzes("general"))
    var correct = 0
    var incorrect = 0
    var logs = List.empty[(Int, String, String)]
    var id = 1
    println(s"Starting quiz on '$topic'...")
    quiz.foreach {
      case (question, correctAnswer) => println(s"Q: $question")
      val userAnswer = "dummy"
      if (userAnswer.trim.toLowerCase == correctAnswer.toLowerCase) {
        correct += 1
        logs = logs :+ (id, "quiz", "correct")
      }
      else {
        incorrect += 1
        logs = logs :+ (id, "quiz", "incorrect")
      }
      id += 1
    }
    val total = correct + incorrect
    s"Quiz completed! Total: $total, Correct: $correct, Incorrect: $incorrect"
  }
}