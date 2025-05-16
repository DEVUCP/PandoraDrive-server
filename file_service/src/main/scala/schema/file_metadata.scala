package schema

import types.FileId

case class FileMetadata(
    file_id: FileId,
    folder_id: Int,
    file_name: String,
    created_at: String,
    modified_at: String,
    size_bytes: Int,
    mime_type: String,
    user_id: Int,
    status: String
)
