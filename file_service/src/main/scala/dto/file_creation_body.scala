package dto

case class FileCreationBody(
    file_name: String,
    folder_id: Int,
    size_bytes: BigInt,
    mime_type: String,
    owner_id: Int,
    status: String,
    created_at: String,
    modified_at: String
)

def validate_file_creation_body(
    body: FileCreationBody
): Option[String] = {
  val allowedStatus = Set("UploadStart", "Uploaded", "Flawed")
  val dateRegex = """^\d{4}-\d{2}-\d{2}$""".r

  if (!allowedStatus.contains(body.status))
    return Some(
      "Invalid Status; Valid Status is one of UploadStart, Uploaded, Flawed"
    )

  if (!dateRegex.matches(body.created_at))
    return Some("created_at has invalid format (expected YYYY-MM-DD)")

  if (!dateRegex.matches(body.modified_at))
    return Some("modified_at has invalid format (expected YYYY-MM-DD)")

  None
}
