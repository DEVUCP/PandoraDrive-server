package dto

import types.FolderId

case class FolderMetadata(
    folder_id: FolderId,
    parent_folder_id: Option[Int],
    folder_name: String,
    created_at: String
)
