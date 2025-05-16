package types

case class ErrorResponse(error: String)
case class SuccessResponse(message: String)
case class FileUploadMetadataInserted(
    token: String,
    upload_link: String,
    complete_link: String,
    chunk_size: Int
)
