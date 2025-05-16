package backend.models

import io.circe.generic.auto._

case class ChatbotResponse(
  text: Option[String],
  html_object: Option[String]
)

case class ChatbotOutput(
  text: Option[List[String]],
  html_object: Option[String]
)

case class ChatbotToken(
 input_tokens: List[String],
 output: ChatbotOutput
)

case class ChatbotTokens(
  Greeting: ChatbotToken,
  Analytics: ChatbotToken,
  LargestFileStat: ChatbotToken,
  SmallestFileStat: ChatbotToken,
  FileCountStat: ChatbotToken,
  RecentFileStat: ChatbotToken,
  RecentUploadDateStat: ChatbotToken,
  TotalSizeStat: ChatbotToken,
  DailyUploadsStat: ChatbotToken,
  WeeklyUploadsStat: ChatbotToken,
  MonthlyUploadsStat: ChatbotToken,
  YearlyUploadsStat: ChatbotToken,
  LongestVideoStat: ChatbotToken,
  ShortestVideoStat: ChatbotToken,
  VideoCountStat: ChatbotToken,
  PhotoCountStat: ChatbotToken,
  FolderCountStat: ChatbotToken,
  FreeSpaceStat: ChatbotToken,
  BiggestFolderStat: ChatbotToken,
  UploadFile: ChatbotToken,
  DisplayFile: ChatbotToken,
  FilterFiles: ChatbotToken,
  Help: ChatbotToken,
  Default: ChatbotToken
)