package backend.core
object ChatbotCore {
  def generateResponse(query: String): String = {
    query.toLowerCase match {
      case q if q.contains("hello") || q.contains("hi") || q.contains("hey") =>
        "Hello! How can I assist you today?"

      case q if q.contains("quiz") =>
        "Would you like to start a quiz? I can help with that!"

      case q if q.contains("analytics") || q.contains("statistics") =>
        "I can provide you with analytics data. What would you like to analyze?"

      case q if q.contains("help") || q.contains("support") =>
        "I'm here to help. Please tell me what you need assistance with."

      case _ =>
        "I'm not sure how to respond to that. Could you please clarify?"
    }
  }
}
