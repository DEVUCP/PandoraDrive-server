package backend.utils
object QuizUtils {
  val fileQuizBank: List[(String, List[String], String)] = List(
    (
      "Which file type usually contains compressed data?",
      List(".txt", ".zip", ".csv", ".md"),
      ".zip"
    ),
    (
      "What metadata is typically used to sort files by creation date?",
      List("File name", "Size", "Timestamp", "Extension"),
      "Timestamp"
    ),
    (
      "Which of these file types is likely to contain images?",
      List(".docx", ".png", ".pdf", ".exe"),
      ".png"
    ),
    (
      "Which file extension is most likely used for plain text files?",
      List(".mp4", ".txt", ".exe", ".zip"),
      ".txt"
    ),
    (
      "What kind of file would most likely have a large size?",
      List(".txt", ".mp3", ".md", ".csv"),
      ".mp3"
    )
  )
  def selectQuizQuestions(topic: String): List[(String, List[String], String)] = {
    topic.toLowerCase match {
      case "quiz" => fileQuizBank
      case _ => List()
    }
  }
  def presentQuizQuestion(q: (String, List[String], String)): String =
    s"${q._1}\nOptions: ${q._2.mkString(", ")}"
  def evaluateQuizAnswer(userAnswer: String, correctAnswer: String): Boolean = {
    userAnswer.trim.equalsIgnoreCase(correctAnswer.trim)
  }
  def summarizeQuizResults(answers: List[Boolean]): String = {
    val total = answers.length
    val correct = answers.count(identity)
    val incorrect = total - correct
    val accuracy = if (total > 0) (correct.toDouble / total) * 100 else 0.0
    f"Quiz Completed! Total: $total, Correct: $correct, Incorrect: $incorrect, Accuracy: $accuracy%.2f%%"
  }
}
