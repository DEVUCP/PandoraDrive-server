package dto

import types.FileId
import types.FolderId

case class FileMoveBody(
    file_id: FileId,
    user_id: Int,
    new_parent_folder_id: FolderId
)
