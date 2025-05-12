package dto

import types.FileId

case class FileRenameBody(file_id: FileId, user_id: Int, new_file_name: String)
