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
      case "file" | "files" | "storage" => fileQuizBank
      case _ => fileQuizBank.take(3)
    }
  }
  // TODO: Format and present quiz question with options
  def presentQuizQuestion(q: (String, List[String], String)): String = ???
  // TODO: Evaluate user's answer against the correct answer
  def evaluateQuizAnswer(userAnswer: String, correctAnswer: String): Boolean = ???
  // TODO: Summarize the results of the quiz and calculate accuracy
  def summarizeQuizResults(answers: List[Boolean]): String = ???
}