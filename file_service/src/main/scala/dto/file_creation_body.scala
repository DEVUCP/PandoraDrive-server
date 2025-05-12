package dto

import cats.effect.IO

case class FileUpsertionBody(
    file_name: String,
    folder_id: Int,
    size_bytes: BigInt,
    mime_type: String,
    user_id: Int
)
