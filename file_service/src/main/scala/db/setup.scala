package db

import cats._
import cats.data._
import cats.implicits._

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, _}

import db.transactor
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.circe.Encoder
import types.{ChunkId, FileId}

def database_setup(): IO[Unit] = {
  val actions = for {
    _ <- IO.println("Enabling foreign keys...")
    _ <- sql"""PRAGMA foreign_keys = ON;""".update.run.void
      .transact(transactor)

    _ <- IO.println("Creating folder_metadata table...")
    _ <- sql"""create table if not exists folder_metadata (
      folder_id INTEGER PRIMARY KEY AUTOINCREMENT,
      parent_folder_id int NULL,
      folder_name TEXT NOT NULL check (length(folder_name) > 0),
      created_at TEXT NOT NULL default (datetime('now')),
      user_id int NOT NULL,
      FOREIGN KEY(parent_folder_id) REFERENCES folder_metadata(folder_id) on delete cascade
      UNIQUE (parent_folder_id, folder_name)
    );""".update.run.void
      .transact(transactor)

    _ <- IO.println("Creating chunk_metadata table...")
    _ <- sql"""create table if not exists chunk_metadata (
      chunk_id VARCHAR(64) PRIMARY KEY,
      byte_size INT NOT NULL,
      ref_count INT DEFAULT 1
    );""".update.run.void
      .transact(transactor)

    _ <- IO.println("Creating file_metadata table...")
    _ <- sql"""create table if not exists file_metadata (
      file_id INTEGER PRIMARY KEY AUTOINCREMENT,
      folder_id int NOT NULL,
      file_name TEXT NOT NULL check (length(file_name) > 0),
      created_at TEXT NOT NULL DEFAULT (datetime('now')),
      modified_at TEXT NOT NULL DEFAULT (datetime('now')),
      size_bytes bigint NOT NULL,
      mime_type TEXT NOT NULL,
      user_id int NOT NULL,
      status TEXT NOT NULL CHECK (status in ('Uploading', 'Uploaded', 'Flawed')),
      FOREIGN KEY(folder_id) REFERENCES folder_metadata(folder_id) on delete cascade
      UNIQUE (folder_id, file_name)
    );""".update.run.void
      .transact(transactor)
    _ <- sql"""
      CREATE TRIGGER IF NOT EXISTS update_modified_at 
      AFTER UPDATE ON file_metadata
      FOR EACH ROW
      WHEN NEW.modified_at = OLD.modified_at
      BEGIN
        UPDATE file_metadata SET modified_at = datetime('now') WHERE file_id = OLD.file_id;
      END;
    """.update.run.void.transact(transactor)
    _ <- IO.println("Creating file_chunk table...")
    _ <- sql"""create table if not exists file_chunk (
      file_id INTEGER,
      chunk_id VARCHAR(64),
      chunk_seq INTEGER,
      primary key(file_id, chunk_id, chunk_seq)
      foreign key (file_id) references file_metadata(file_id) on delete cascade
      foreign key (chunk_id) references chunk_metadata(chunk_id) on delete cascade
    )""".update.run.void.transact(transactor)
  } yield ()

  IO.println("Running database setup...") *>
    actions
      .handleErrorWith(err => IO.println(s"Error during setup: $err"))
}
