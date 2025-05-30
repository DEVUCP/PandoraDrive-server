package model

import java.sql.SQLException

import cats._
import cats.data._
import cats.implicits._

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import db.transactor
import doobie._
import doobie.implicits._
import dto.FolderCreationBody
import schema.{FileMetadata, FolderMetadata}
import services.folder_service.get_folder_files_metadata
import types.FolderId

def get_folder_metadata_by_folder_id(
    id: FolderId
): IO[Either[String, dto.FolderMetadata]] =
  sql"""select folder_id, parent_folder_id, folder_name, created_at, user_id from folder_metadata where folder_id = $id"""
    .query[dto.FolderMetadata]
    .unique
    .transact(transactor)
    .attempt
    .map {
      case Right(h) => Right(h)
      case Left(e)  => Left(s"Database error: ${e.getMessage}")
    }

def get_root_folder_by_user_id(
    user_id: Int
): IO[Either[String, dto.FolderMetadata]] =
  sql"""select folder_id, parent_folder_id, folder_name, created_at from folder_metadata where user_id = $user_id and parent_folder_id is null"""
    .query[dto.FolderMetadata]
    .option
    .transact(transactor)
    .flatMap {
      case Some(folder) => IO.pure(Right(folder))
      case None =>
        create_folder("root", None, user_id).flatMap {
          case Right(folder_id) =>
            get_folder_metadata_by_folder_id(folder_id)
          case Left(_) => IO.pure(Left("Failed to get root folder"))
        }
    }

def create_folder(
    folder_name: String,
    parent_folder_id: Option[FolderId],
    user_id: Int
): IO[Either[String, FolderId]] =
  val valid: IO[Boolean] = parent_folder_id match {
    case None         => IO.pure(true)
    case Some(folder) => folder_exists(folder, user_id)
  }

  valid.flatMap {
    case false => IO.pure(Left("Invalid parent_folder_id"))
    case true =>
      sql"""insert into folder_metadata(folder_name, parent_folder_id, user_id) values(${folder_name}, ${parent_folder_id}, ${user_id})""".update
        .withUniqueGeneratedKeys[FolderId]("folder_id")
        .transact(transactor)
        .attempt
        .flatMap {
          case Right(folder_id) => IO.pure(Right(folder_id))
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
  }

def folder_exists(folder_id: FolderId, user_id: Int): IO[Boolean] =
  sql"""select 1 from folder_metadata where user_id = $user_id and folder_id = $folder_id"""
    .query[Int]
    .option
    .map(_.isDefined)
    .transact(transactor)

def get_folder_by_parent_id(
    parent_folder_id: FolderId
): IO[List[dto.FolderMetadata]] =
  sql"""select folder_id, parent_folder_id, folder_name,created_at user_id from folder_metadata where parent_folder_id=$parent_folder_id"""
    .query[dto.FolderMetadata]
    .to[List]
    .attempt
    .transact(transactor)
    .flatMap {
      case Right(folders) => IO.pure(folders)
      case Left(err)      => IO.println(err) *> IO.pure(List())
    }

def delete_folder_by_id(folder_id: FolderId, user_id: Int): IO[Boolean] =
  folder_exists(folder_id, user_id).flatMap {
    case false => IO.pure(false)
    case true =>
      sql"""delete from folder_metadata where folder_id=$folder_id and user_id=$user_id""".update.run.void
        .transact(transactor)
        .flatMap { _ => IO.pure(true) }
        .handleErrorWith(err => IO.println(err) *> IO.pure(false))
  }

def rename_folder(
    folder_id: FolderId,
    user_id: Int,
    new_folder_name: String
): IO[Boolean] =
  folder_exists(folder_id, user_id).flatMap {
    case false => IO.pure(false)
    case true =>
      sql"""update folder_metadata set folder_name=$new_folder_name where folder_id=$folder_id""".update.run.void
        .transact(transactor)
        .flatMap { _ => IO.pure(true) }
        .handleErrorWith(err => IO.println(err) *> IO.pure(false))
  }

def move_folder(
    folder_id: FolderId,
    user_id: Int,
    new_folder_id: FolderId
): IO[Boolean] = {
  // check if the new folder is a descendant of the current folder
  val checkDescendantQuery =
    sql"""
      WITH RECURSIVE descendants AS (
        SELECT id FROM folder_metadata WHERE parent_folder_id = $folder_id
        UNION
        SELECT f.id FROM folder_metadata f
        INNER JOIN descendants d ON f.parent_folder_id = d.id
      )
      SELECT 1 FROM descendants WHERE id = $new_folder_id LIMIT 1
    """.query[Int].option.transact(transactor)

  checkDescendantQuery.flatMap {
    case Some(_) =>
      IO.pure(false)
    case None =>
      sql"UPDATE folder_metadata SET parent_folder_id = $new_folder_id WHERE id = $folder_id AND user_id = $user_id".update.run
        .transact(transactor)
        .flatMap {
          case 0 =>
            IO.pure(false)
          case _ =>
            IO.pure(true)
        }
  }
}
