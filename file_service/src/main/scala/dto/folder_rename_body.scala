package dto

import types.FolderId

case class FolderRenameBody(
    folder_id: FolderId,
    user_id: Int,
    new_folder_name: String
)
