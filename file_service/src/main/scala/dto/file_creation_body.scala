package dto

case class FileCreationBody(
    file_name: String,
    folder_id: Int,
    size_bytes: BigInt,
    mime_type: String,
    owner_id: Int,
    created_at: String,
    modified_at: String
)

def validate_file_creation_body(
    body: FileCreationBody
): Option[String] = {
  val dateRegex = """^\d{4}-\d{2}-\d{2}$""".r

  if (!dateRegex.matches(body.created_at))
    return Some("created_at has invalid format (expected YYYY-MM-DD)")

  if (!dateRegex.matches(body.modified_at))
    return Some("modified_at has invalid format (expected YYYY-MM-DD)")

  None
}
