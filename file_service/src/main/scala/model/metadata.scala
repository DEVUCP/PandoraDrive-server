package model

import cats._
import cats.data._
import cats.effect.IO
import cats.implicits._
import cats.effect.unsafe.implicits.global
import doobie._
import doobie.implicits._

import schema.{FileMetadataDB, FolderMetadata}
import db.transactor

def get_file_metadata_by_file_id(id: Int): Unit =
  sql"""select file_id, file_name, uploaded_at, modified_at, created_at, size_bytes, mime_type, owner_id, status from file_metadata"""
    .query[FileMetadataDB]
    .to[List]
    .transact(transactor)
    .unsafeRunSync()
    .foreach(println)

// TODO:  Create indices
/** CREATE INDEX idx_folders_parent ON folders(parent_folder_id); CREATE INDEX
  * idx_files_folder ON files(folder_id); CREATE INDEX idx_files_status ON
  * files(upload_status);
  */

def get_folder_metadata_by_folder_id(
    id: Int
): (Option[FolderMetadata], String) =
  val res =
    sql"""select folder_id, parent_folder_id, folder_name, created_at, modified_at, uploaded_at, owner_id, status from folder_metadata where folder_id = $id"""
      .query[FolderMetadata]
      .to[List]
      .transact(transactor)
      .unsafeRunSync()

  res match {
    case Nil      => (None, f"No folder exists with id=$id")
    case h :: Nil => (Some(h), "")
    case _        => (None, "There canot be more than one element")
  }
