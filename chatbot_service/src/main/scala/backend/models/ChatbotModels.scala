package backend.models

import io.circe.generic.auto._

case class ChatbotResponse(
  text: Option[String],
  html_object: Option[List[String]]
)

case class ChatbotOutput(
  text: Option[List[String]],
  html_object: Option[List[String]]
)

case class ChatbotToken(
 input_tokens: List[String],
 output: ChatbotOutput
)

case class ChatbotTokens(
  Greeting: ChatbotToken,
  Analytics: ChatbotToken,
  UploadFile: ChatbotToken,
  DisplayFile: ChatbotToken,
  FilterFiles: ChatbotToken,
  Help: ChatbotToken,
  Default: ChatbotToken
)
