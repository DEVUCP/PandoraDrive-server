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
import types.FolderId

def get_folder_metadata_by_folder_id(
    id: FolderId
): IO[Either[String, FolderMetadata]] =
  sql"""select folder_id, parent_folder_id, folder_name, created_at, modified_at, uploaded_at, owner_id, status from folder_metadata where folder_id = $id"""
    .query[FolderMetadata]
    .to[List]
    .transact(transactor)
    .attempt
    .map {
      case Right(Nil)      => Left(f"No folder exists with id=$id")
      case Right(h :: Nil) => Right(h)
      case Right(_)        => Left("There cannot be more than one element")
      case Left(e)         => Left(s"Database error: ${e.getMessage}")
    }
