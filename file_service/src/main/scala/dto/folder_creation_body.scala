package dto

case class DTOFolderCreationBody(
    folder_name: String,
    parent_folder_id: Option[Int],
    owner_id: Int
)
