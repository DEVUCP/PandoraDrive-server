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

def get_folder_metadata_by_folder_id(
    id: Int
): (Option[FolderMetadata], Option[String]) =
  val res =
    sql"""select folder_id, parent_folder_id, folder_name, created_at, modified_at, uploaded_at, owner_id, status from folder_metadata where folder_id = $id"""
      .query[FolderMetadata]
      .to[List]
      .transact(transactor)
      .unsafeRunSync()

  res match {
    case Nil      => (None, Some(f"No folder exists with id=$id"))
    case h :: Nil => (Some(h), None)
    case _        => (None, Some("There cannot be more than one element"))
  }
