package dto

case class FileCreationBody(
    file_name: String,
    folder_id: Int,
    size_bytes: BigInt,
    mime_type: String,
    owner_id: Int,
    status: String,
    created_at: String,
    modified_at: String
)
