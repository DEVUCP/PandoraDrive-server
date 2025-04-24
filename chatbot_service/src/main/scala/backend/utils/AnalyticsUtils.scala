package backend.utils
object AnalyticsUtils {
  private var log: List[(Int, String, String)] = List()
  private var counter: Int = 0
  def logInteraction(userInput: String, chatbotResponse: String): Unit = {
    counter += 1
    log = log :+ (counter, userInput, chatbotResponse)
  }
  def getInteractionLog(): List[(Int, String, String)] = log
}