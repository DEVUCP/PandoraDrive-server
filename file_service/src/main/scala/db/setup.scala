package db

import cats._
import cats.data._
import cats.implicits._

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import db.transactor
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.circe.Encoder
import types.{ChunkId, FileId}

def database_setup(): IO[Unit] = {
  val actions = for {
    // Enable Foreign Keys
    _ <- sql"""PRAGMA foreign_keys = ON;""".update.run.void
    // Folder Metadata
    _ <- sql"""create table if not exists folder_metadata (
      folder_id INTEGER PRIMARY KEY AUTOINCREMENT,
      parent_folder_id int NULL,
      folder_name TEXT NOT NULL,
      created_at TEXT NOT NULL,
      user_id int NOT NULL,
      FOREIGN KEY(parent_folder_id) REFERENCES folder_metadata(folder_id) on delete cascade
      UNIQUE (parent_folder_id, folder_name) 
     );""".update.run.void

    // Recursive Deletion for subfolders because the cascade will only affect direct children
    _ <- sql"""
      CREATE TRIGGER IF NOT EXISTS delete_child_folders
      BEFORE DELETE ON folder_metadata
      FOR EACH ROW
      BEGIN
        -- First delete all subfolders (which will trigger this same trigger recursively)
        DELETE FROM folder_metadata WHERE parent_folder_id = OLD.folder_id;
      END;
    """.update.run.void

    _ <- sql"""CREATE TABLE IF NOT EXISTS chunk_metadata (
        chunk_id     VARCHAR(64) PRIMARY KEY,
        byte_size    INT NOT NULL,
        ref_count    INT DEFAULT 1
    );
  """.update.run.void
    _ <- sql"""create table if not exists file_metadata (
      file_id INTEGER PRIMARY KEY AUTOINCREMENT,
      folder_id int NOT NULL,
      file_name TEXT NOT NULL,
      created_at TEXT NOT NULL,
      modified_at TEXT NOT NULL,
      uploaded_at TEXT NOT NULL,
      size_bytes bigint NOT NULL,
      mime_type TEXT NOT NULL,
      user_id int NOT NULL,
      status TEXT NOT NULL CHECK (status in ('Uploading', 'Uploaded', 'Flawed')),
      FOREIGN KEY(folder_id) REFERENCES folder_metadata(folder_id) on delete cascade
      UNIQUE (folder_id, file_name)
     );""".update.run.void

    _ <- sql"""create table if not exists file_chunk (
      file_id INTEGER,
      chunk_id VARCHAR(64),
      chunk_seq INTEGER,
      primary key(file_id, chunk_id, chunk_seq)
      foreign key (file_id) references file_metadata(file_id) on delete cascade
      foreign key (chunk_id) references chunk_metadata(chunk_id) on delete cascade
    )""".update.run.void

    _ <- sql"""create trigger if not exists decrement_chunk_ref_count
      after delete on file_chunk
      for each row
      begin
        update chunk_metadata
        set ref_count = ref_count - 1
        where chunk_id = old.chunk_id;
      end;
    """.update.run.void
  } yield ();

  actions.transact(transactor).handleErrorWith(err => IO.println(err))
}
