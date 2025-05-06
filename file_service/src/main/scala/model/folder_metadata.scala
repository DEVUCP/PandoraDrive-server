package model

import cats._
import cats.data._
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import db.transactor
import doobie._
import doobie.implicits._
import dto.DTOFolderCreationBody
import schema.FileMetadata
import schema.FolderMetadata
import types.FolderId

def get_folder_metadata_by_folder_id(
    id: FolderId
): IO[Either[String, FolderMetadata]] =
  sql"""select folder_id, parent_folder_id, folder_name, created_at, user_id from folder_metadata where folder_id = $id"""
    .query[FolderMetadata]
    .unique
    .transact(transactor)
    .attempt
    .map {
      case Right(h) => Right(h)
      case Left(e)  => Left(s"Database error: ${e.getMessage}")
    }

def get_root_folder_by_user_id(
    user_id: Int
): IO[Either[String, FolderMetadata]] =
  sql"""select folder_id, parent_folder_id, folder_name, created_at, user_id from folder_metadata where user_id = $user_id and parent_folder_id is null"""
    .query[FolderMetadata]
    .option
    .transact(transactor)
    .flatMap {
      case Some(folder) => IO.pure(Right(folder))
      case None =>
        val created_at = java.time.Instant.now().toString
        sql"""insert into folder_metadata(folder_name, created_at, user_id) values('root', $created_at, $user_id)""".update.run
          .transact(transactor)
          .flatMap { _ => get_root_folder_by_user_id(user_id) }
          .handleError(err => Left(s"Database Error: ${err.getMessage}"))
    }

def create_folder(body: DTOFolderCreationBody): IO[FolderMetadata] =
  sql"""insert into folder_metadata(folder_name, parent_folder_id, user_id, created_at) values(${body.folder_name}, ${body.parent_folder_id}, ${body.user_id})""".update
    .withUniqueGeneratedKeys[Int]("folder_id")
    .transact(transactor)
    .flatMap { folder_id =>
      sql"""select folder_id, parent_folder_id, folder_name,created_at, user_id from folder_metadata where folder_id=$folder_id"""
        .query[FolderMetadata]
        .unique
        .transact(transactor)
    }
