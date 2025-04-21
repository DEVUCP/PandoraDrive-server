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
import io.circe.Encoder
import types.FileId

case class FileMetadata(
    file_id: FileId,
    folder_id: Int,
    file_name: String,
    size_bytes: Int,
    mime_type: String,
    owner_id: Int,
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
      owner_id int NOT NULL,
      status TEXT NOT NULL CHECK (status in ('UploadStart', 'Uploaded', 'Flawed')),
      FOREIGN KEY(folder_id) REFERENCES folder_metadata(file_id)
     );""".update.run.void.transact(transactor)

// def create_root_directory(): Unit =
//   val rs: List[Int] = sql"SELECT 1 FROM folders WHERE parent_folder_id IS NULL"
//     .query[Int]
//     .to[List]
//     .transact(transactor)
//     .unsafeRunSync()
//
//   rs match {
//     case Nil =>
//       sql"INSERT INTO folder_metadata(parent_folder_id, folder_name, uploaded_at, created_at, modified_at) "
//     case _::_ => ???
//   }
