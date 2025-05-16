package dto

import types.FileId

case class FileDeletionBody(file_id: FileId, user_id: Int)
