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
  sql"""select folder_id, parent_folder_id, folder_name, created_at, owner_id, status from folder_metadata where folder_id = $id"""
    .query[FolderMetadata]
    .unique
    .transact(transactor)
    .attempt
    .map {
      case Right(h) => Right(h)
      case Left(e)  => Left(s"Database error: ${e.getMessage}")
    }

def get_root_folder_by_user_id(
    id: Int
): IO[Either[String, FolderMetadata]] =
  sql"""select folder_id, parent_folder_id, folder_name, created_at, owner_id, status from folder_metadata where user_id = $id and parent_folder_id = $id"""
    .query[FolderMetadata]
    .unique
    .transact(transactor)
    .attempt
    .map {
      case Right(h) => Right(h)
      case Left(e)  => Left(s"Database error: ${e.getMessage}")
    }

def create_folder(body: DTOFolderCreationBody): IO[FolderMetadata] =
  sql"""insert into folder_metadata(folder_name, parent_folder_id, owner_id, created_at) values(${body.folder_name}, ${body.parent_folder_id}, ${body.owner_id})""".update
    .withUniqueGeneratedKeys[Int]("folder_id")
    .transact(transactor)
    .flatMap { folder_id =>
      sql"""select folder_id, parent_folder_id, folder_name,created_at, owner_id, status from folder_metadata where folder_id=$folder_id"""
        .query[FolderMetadata]
        .unique
        .transact(transactor)
    }
