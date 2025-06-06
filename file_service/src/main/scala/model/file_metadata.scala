package model

import cats._
import cats.data._
import cats.implicits._

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import db.transactor
import doobie._
import doobie.implicits._
import dto.{FileUpsertionBody}
import types.{FileId, FolderId}

def get_file_metadata_by_file_id(
    id: FileId
): IO[Either[String, dto.FileMetadata]] =
  sql"""
      select file_id, folder_id, file_name, created_at, modified_at, size_bytes, mime_type, status
      from file_metadata
      where file_id = $id;
    """
    .query[dto.FileMetadata]
    .unique
    .transact(transactor)
    .attempt
    .map {
      case Right(h) => Right(h)
      case Left(e)  => Left(s"Database error: ${e.getMessage}")
    }

def get_file_id_by_file_name_and_folder(
    file_name: String,
    folder_id: FolderId
): IO[Option[FileId]] =
  sql"""
    SELECT file_id FROM file_metadata WHERE file_name=$file_name and folder_id=$folder_id
  """
    .query[FileId]
    .option
    .transact(transactor)

def update_file_metadata(
    body: FileUpsertionBody,
    file_id: FileId
): IO[Either[String, Long]] = {
  implicit val bigIntPut: Put[BigInt] =
    Put[BigDecimal].contramap(BigDecimal(_))

  if (body.file_name.isEmpty) IO.pure(Left("Empty File Name"))
  else {
    val updateQuery =
      sql"""update file_metadata set status='Uploading' where file_id=$file_id"""
    updateQuery.update
      .withGeneratedKeys[FileId]("id")
      .compile
      .last
      .transact(transactor)
      .attempt
      .map {
        case Right(_) => Right(file_id)
        case Left(e) =>
          Left(
            s"Database error: ${e.getMessage}"
          )
      }
  }
}
def create_file_metadata(body: FileUpsertionBody): IO[Either[String, Long]] = {
  implicit val bigIntPut: Put[BigInt] =
    Put[BigDecimal].contramap(BigDecimal(_))

  if (body.file_name.isEmpty) IO.pure(Left("Empty file name"))
  else {
    val insertQuery =
      sql"""
            insert into file_metadata (
              file_name, folder_id, size_bytes, mime_type,
              user_id, status
            )
            values (
              ${body.file_name}, ${body.folder_id}, ${body.size_bytes},
              ${body.mime_type}, ${body.user_id}, 'Uploading'
            )
          """

    val insertAction: ConnectionIO[Option[Long]] =
      insertQuery.update
        .withGeneratedKeys[FileId]("id")
        .compile
        .last

    insertAction
      .transact(transactor)
      .attempt
      .map {
        case Right(Some(id)) => Right(id)
        case Right(None)     => Left("Insert failed: no ID returned")
        case Left(e) =>
          Left(
            s"Database error: ${e.getMessage}"
          )
      }
  }
}

def set_file_status_uploaded(file_id: FileId): IO[Unit] = {
  sql"""
  update file_metadata set status='Uploaded' where file_id=$file_id
  """.update.run.void.transact(transactor)
}

def get_files_by_folder_id(folder_id: FolderId): IO[
  List[dto.FileMetadata]
] =
  implicit val bigIntMeta: Meta[BigInt] = Meta[Long].timap(BigInt(_))(_.toLong)

  sql"""select file_id, folder_id, file_name, created_at, modified_at, size_bytes, mime_type, status
  from file_metadata where folder_id = $folder_id"""
    .query[dto.FileMetadata]
    .to[List]
    .transact(transactor)
    .handleErrorWith { e =>
      IO.println(
        s"Error fetching files for folder $folder_id: ${e.getMessage}"
      ) >>
        IO.pure(Nil)
    }

def delete_file_metadata(file_id: FileId, user_id: Int): IO[Boolean] =
  file_exists(file_id, user_id).flatMap {
    case false => IO.pure(false)
    case true =>
      sql"""delete from file_metadata where file_id = $file_id""".update.run.void
        .transact(transactor)
        .flatMap(_ => IO.pure(true))
        .handleErrorWith(err => IO.println(err) *> IO.pure(false))
  }

def file_exists(file_id: FileId, user_id: Int): IO[Boolean] =
  sql"""SELECT EXISTS (SELECT 1 FROM file_metadata WHERE file_id = $file_id and user_id = $user_id)"""
    .query[Boolean]
    .unique
    .transact(transactor)

def rename_file(
    file_id: FileId,
    user_id: Int,
    new_file_name: String
): IO[Boolean] =
  file_exists(file_id, user_id).flatMap {
    case false => IO.pure(false)
    case true =>
      sql"""update file_metadata set file_name = $new_file_name where file_id = $file_id""".update.run
        .transact(transactor)
        .as(true)
        .handleErrorWith(err => IO.println(err) *> IO.pure(false))
  }

def move_file(
    file_id: FileId,
    user_id: Int,
    new_parent_folder: FolderId
): IO[Boolean] =
  file_exists(file_id, user_id).flatMap {
    case false => IO.pure(false)
    case true =>
      folder_exists(new_parent_folder, user_id).flatMap {
        case false => IO.pure(false)
        case true =>
          sql"""update file_metadata set parent_folder_id = $new_parent_folder where file_id = $file_id""".update.run
            .transact(transactor)
            .as(true)
            .handleErrorWith(err => IO.println(err) *> IO.pure(false))
      }
  }
