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
import types.FileId
def get_file_metadata_by_file_id(
    id: Int
): (Option[FileMetadata], Option[String]) = {

  val result: Either[Throwable, List[FileMetadata]] =
    sql"""
      select file_id, folder_id, file_name, size_bytes, mime_type, owner_id, status, uploaded_at, created_at, modified_at
      from file_metadata
      where file_id = $id;
    """
      .query[FileMetadata]
      .to[List]
      .transact(transactor)
      .attempt
      .unsafeRunSync()

  result match {
    case Right(Nil)      => (None, None)
    case Right(h :: Nil) => (Some(h), None)
    case Right(_)        => (None, Some("There cannot be more than one file"))
    case Left(e)         => (None, Some(s"Database error: ${e.getMessage}"))
  }
}

def create_file_metadata(body: FileCreationBody): Either[String, FileId] = {
  val valid = validate_file_creation_body(body)
  valid match {
    case Some(err) => return Left(err)
    case _ => {
      implicit val bigIntPut: Put[BigInt] =
        Put[BigDecimal].contramap(BigDecimal(_))
      val uploaded_at = java.time.Instant.now().toString

      val insertQuery =
        sql"""
      insert into file_metadata (
        file_name, folder_id, size_bytes, mime_type,
        owner_id, status, created_at, uploaded_at, modified_at
      )
      values (
        ${body.file_name}, ${body.folder_id}, ${body.size_bytes},
        ${body.mime_type}, ${body.owner_id}, 'UploadStart',
        ${body.created_at}, $uploaded_at, ${body.modified_at}
      )
    """

      val create: ConnectionIO[Option[Long]] =
        insertQuery.update
          .withGeneratedKeys[Long]("id")
          .compile
          .last

      val result: Either[Throwable, Option[Long]] =
        create.transact(transactor).attempt.unsafeRunSync()

      result match {
        case Right(Some(id)) => Right(id)
        case Right(None)     => Left("Insert failed: no ID returned")
        case Left(e) =>
          println(s"-- ERROR: $e")
          Left(
            "An error occurred. If you are a developer, check console log output"
          )
      }
    }
  }
}

// TODO:  Create indices
/** CREATE INDEX idx_folders_parent ON folders(parent_folder_id); CREATE INDEX
  * idx_files_folder ON files(folder_id); CREATE INDEX idx_files_status ON
  * files(upload_status);
  */
