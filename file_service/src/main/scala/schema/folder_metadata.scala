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
import types.FolderId

case class FolderMetadata(
    folder_id: FolderId,
    parent_folder_id: Option[Int],
    folder_name: String,
    created_at: String,
    user_id: Int
)

def create_folder_metadata_table(): IO[Unit] =
  sql"""
     create table if not exists folder_metadata (
      folder_id INTEGER PRIMARY KEY AUTOINCREMENT,
      parent_folder_id int NULL,
      folder_name TEXT NOT NULL,
      created_at TEXT NOT NULL,
      user_id int NOT NULL,
      FOREIGN KEY(parent_folder_id) REFERENCES folder_metadata(folder_id)
      UNIQUE (parent_folder_id, folder_name) 
     );""".update.run.void.transact(transactor)
