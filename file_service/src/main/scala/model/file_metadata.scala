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
import dto.FileCreationBody
import java.text.SimpleDateFormat
import java.text.DateFormat
import java.util.Calendar;
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

def create_file_metadata(body: FileCreationBody): Option[String] =
  // TODO: Validate body
  val df: DateFormat = new SimpleDateFormat("YYYY-MM-DD");
  val uploaded_at: String = df.format(Calendar.getInstance().getTime())
  val create: ConnectionIO[Int] =
    sql"""
      insert into file_metadata(file_name, folder_id, size_bytes, mime_type, owner_id, status, created_at, uploaded_at, modified_at) values(${body.file_name}, ${body.folder_id}, ${body.size_bytes.toString}, ${body.mime_type}, ${body.owner_id}, ${body.status}, ${body.created_at}, "${uploaded_at}", ${body.modified_at});
     """.update.run

  val result: Either[Throwable, Int] =
    create.transact(transactor).attempt.unsafeRunSync()

  result match {
    case Right(1) => None
    case Right(_) => Some("Insert failed: no row inserted")
    case Left(e) =>
      println(f"-- ERROR: $e")
      Some(s"Developer: Check Console Output")
  }

// TODO:  Create indices
/** CREATE INDEX idx_folders_parent ON folders(parent_folder_id); CREATE INDEX
  * idx_files_folder ON files(folder_id); CREATE INDEX idx_files_status ON
  * files(upload_status);
  */
