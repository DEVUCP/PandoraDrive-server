package backend.core
object ChatbotCore {
  def generateResponse(query: String): String = {
    val lowerQuery = query.toLowerCase
    if (lowerQuery.contains("hello") || lowerQuery.contains("hi") || lowerQuery.contains("hey"))
      "Hello! How can I assist you today?"
    else if (lowerQuery.contains("quiz"))
      "Would you like to start a quiz? I can help with that!"
    else if (lowerQuery.contains("analytics") || lowerQuery.contains("statistics"))
      "I can provide you with analytics data. What would you like to analyze?"
    else if (lowerQuery.contains("help") || lowerQuery.contains("support"))
      "I'm here to help. Please tell me what you need assistance with."
    else
      "I'm not sure how to respond to that. Could you please clarify?"
  }
}