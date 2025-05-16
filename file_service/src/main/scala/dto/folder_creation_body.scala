package dto

case class FolderCreationBody(
    folder_name: String,
    parent_folder_id: Int,
    user_id: Int
)
