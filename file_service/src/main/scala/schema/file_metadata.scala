package schema

import cats._
import cats.data._
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import db.transactor
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.circe.Encoder
import types.FileId

case class FileMetadata(
    file_id: FileId,
    folder_id: Int,
    file_name: String,
    size_bytes: Int,
    mime_type: String,
    user_id: Int,
    status: String,
    uploaded_at: String,
    created_at: String,
    modified_at: String
)

def create_file_metadata_table(): IO[Unit] =
  sql"""
     create table if not exists file_metadata (
      file_id INTEGER PRIMARY KEY AUTOINCREMENT,
      folder_id int NOT NULL,
      file_name TEXT NOT NULL,
      created_at TEXT NOT NULL,
      modified_at TEXT NOT NULL,
      uploaded_at TEXT NOT NULL,
      size_bytes bigint NOT NULL,
      mime_type TEXT NOT NULL,
      user_id int NOT NULL,
      status TEXT NOT NULL CHECK (status in ('Uploading', 'Uploaded', 'Flawed')),
      FOREIGN KEY(folder_id) REFERENCES folder_metadata(folder_id)
      UNIQUE (folder_id, file_name)
     );""".update.run.void.transact(transactor)
