package backend.utils

object AnalyticsUtils {
  private var log: List[(Int, String, String)] = List()
  private var counter: Int = 0

  // TODO: Log a user input and corresponding chatbot response, incrementing the interaction counter
  def logInteraction(userInput: String, chatbotResponse: String): Unit = ???

  // TODO: Return the full list of logged interactions with sequence numbers
  def getInteractionLog(): List[(Int, String, String)] = ???
}
