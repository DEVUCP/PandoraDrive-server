package dto

import types.FileId

case class FileMetadata(
    file_id: FileId,
    folder_id: Int,
    file_name: String,
    created_at: String,
    modified_at: String,
    size_bytes: Int,
    mime_type: String,
    status: String
)
