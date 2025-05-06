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
import dto.{FileCreationBody, validate_file_creation_body}
import types.{FileId, FolderId}

def get_file_metadata_by_file_id(
    id: FileId
): IO[Either[String, FileMetadata]] =
  sql"""
      select file_id, folder_id, file_name, size_bytes, mime_type, owner_id, status, uploaded_at, created_at, modified_at
      from file_metadata
      where file_id = $id;
    """
    .query[FileMetadata]
    .to[List]
    .transact(transactor)
    .attempt
    .map {
      case Right(Nil)      => Left(s"No file found with ID $id")
      case Right(h :: Nil) => Right(h)
      case Right(_)        => Left(s"Multiple files with same file_id?")
      case Left(e)         => Left(s"Database error: ${e.getMessage}")
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
    body: FileCreationBody,
    file_id: FileId
): IO[Either[String, Long]] = {
  implicit val bigIntPut: Put[BigInt] =
    Put[BigDecimal].contramap(BigDecimal(_))

  for {
    validation <- validate_file_creation_body(body)
    resp <- {
      val (valid, err) = validation
      if (!valid) IO.pure(Left(err))
      else {
        val modified_at = java.time.Instant.now().toString
        val updateQuery =
          sql"""update file_metadata set status='Uploading', modified_at=${modified_at} where file_id=$file_id"""
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
  } yield resp
}
def create_file_metadata(body: FileCreationBody): IO[Either[String, Long]] = {
  implicit val bigIntPut: Put[BigInt] =
    Put[BigDecimal].contramap(BigDecimal(_))

  for {
    validation <- validate_file_creation_body(body)
    result <- {
      val (valid, err) = validation
      if (!valid) IO.pure(Left(err))
      else {
        val uploaded_at = java.time.Instant.now().toString

        val insertQuery =
          sql"""
            insert into file_metadata (
              file_name, folder_id, size_bytes, mime_type,
              owner_id, status, created_at, uploaded_at, modified_at
            )
            values (
              ${body.file_name}, ${body.folder_id}, ${body.size_bytes},
              ${body.mime_type}, ${body.owner_id}, 'Uploading',
              ${body.created_at}, $uploaded_at, ${body.modified_at}
            )
          """

        val insertAction: ConnectionIO[Option[Long]] =
          insertQuery.update
            .withGeneratedKeys[Long]("id")
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
  } yield result
}

def file_complete_status(file_id: FileId): IO[Unit] = {
  sql"""
  update file_metadata set status='Uploaded' where file_id=$file_id
  """.update.run.void.transact(transactor)
}

def get_files_by_folder_id(folder_id: FolderId): IO[
  List[FileMetadata]
] =
  implicit val bigIntMeta: Meta[BigInt] = Meta[Long].timap(BigInt(_))(_.toLong)

  sql"""select file_id, folder_id, file_name, created_at, modified_at, uploaded_at, size_bytes, mime_type, owner_id, status from file_metadata where folder_id = $folder_id"""
    .query[FileMetadata]
    .to[List]
    .transact(transactor)
    .handleErrorWith { e =>
      IO.println(
        s"Error fetching files for folder $folder_id: ${e.getMessage}"
      ) >>
        IO.pure(Nil)
    }

def delete_file(file_id: FileId): IO[Unit] =
  remove_file_chunks(file_id) *>
    sql"""delete from file_metadata where file_id = $file_id""".update.run.void
      .transact(transactor)
