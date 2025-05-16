package dto

import types.FolderId

case class FolderMoveBody(
    folder_id: FolderId,
    user_id: Int,
    new_folder_id: FolderId
)
