package model

import cats._
import cats.data._
import cats.effect.IO
import cats.implicits._
import cats.effect.unsafe.implicits.global
import doobie._
import doobie.implicits._

import schema.{FileMetadata, FolderMetadata}
import db.transactor

def get_file_metadata_by_file_id(
    id: Int
): (Option[FileMetadata], Option[String]) =
  val res =
    sql"""select file_id, file_name, uploaded_at, modified_at, created_at, size_bytes, mime_type, owner_id, status from file_metadata"""
      .query[FileMetadata]
      .to[List]
      .transact(transactor)
      .unsafeRunSync()

  res match {
    case Nil      => (None, None)
    case h :: Nil => (Some(h), None)
    case _        => (None, Some("There cannot be more than one file"))
  }

// TODO:  Create indices
/** CREATE INDEX idx_folders_parent ON folders(parent_folder_id); CREATE INDEX
  * idx_files_folder ON files(folder_id); CREATE INDEX idx_files_status ON
  * files(upload_status);
  */
