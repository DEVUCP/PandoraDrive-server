package types

case class ErrorResponse(error: String)
case class SuccessResponse(message: String)
case class FileUploadMetadataInserted(message: String, file_id: FileId)
