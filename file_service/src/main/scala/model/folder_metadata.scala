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
import java.sql.SQLException

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
      case None => create_folder(DTOFolderCreationBody("root", None, user_id))
    }

def create_folder(
    body: DTOFolderCreationBody
): IO[Either[String, FolderMetadata]] =
  val created_at = java.time.Instant.now().toString
  sql"""insert into folder_metadata(folder_name, parent_folder_id, user_id, created_at) values(${body.folder_name}, ${body.parent_folder_id}, ${body.user_id}, $created_at)""".update
    .withUniqueGeneratedKeys[Int]("folder_id")
    .transact(transactor)
    .attempt
    .flatMap {
      case Right(folder_id) =>
        sql"""select folder_id, parent_folder_id, folder_name,created_at, user_id from folder_metadata where folder_id=$folder_id"""
          .query[FolderMetadata]
          .unique
          .transact(transactor)
          .flatMap(folder => IO.pure(Right(folder)))
      case Left(e: SQLException)
          if e.getMessage.contains("UNIQUE constraint failed") =>
        IO.pure(
          Left(
            "Folder with the same name already exists in the target directory."
          )
        )

      case Left(e) =>
        IO.println(s"Unexpected DB error: ${e.getMessage}") *>
          IO.pure(Left("Unexpected database error"))
    }
