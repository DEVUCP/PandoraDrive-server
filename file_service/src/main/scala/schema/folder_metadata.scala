package schema

import doobie._
import doobie.implicits._
import cats._
import cats.data._
import cats.effect.IO
import cats.implicits._
import cats.effect.unsafe.implicits.global
import doobie.util.transactor.Transactor
import db.transactor
import types.FolderId

case class FolderMetadata(
    folder_id: FolderId,
    parent_folder_id: Option[Int],
    folder_name: String,
    created_at: String,
    modified_at: String,
    uploaded_at: String,
    owner_id: Int,
    status: String
)

def create_folder_metadata_table(): IO[Unit] =
  sql"""
     create table if not exists folder_metadata (
      folder_id INTEGER PRIMARY KEY AUTOINCREMENT,
      parent_folder_id int NULL,
      folder_name TEXT NOT NULL,
      created_at TEXT NOT NULL,
      modified_at TEXT NOT NULL,
      uploaded_at TEXT NOT NULL,
      owner_id int NOT NULL,
      status TEXT NOT NULL CHECK (status in ('UploadStart', 'Uploaded', 'Flawed')),
      FOREIGN KEY(parent_folder_id) REFERENCES folder_metadata(folder_id)
     );""".update.run.void.transact(transactor)
