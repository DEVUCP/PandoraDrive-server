package schema
import doobie._
import doobie.implicits._
import cats._
import cats.data._
import cats.effect.IO
import cats.implicits._
import cats.effect.unsafe.implicits.global
import doobie.util.transactor.Transactor
import io.circe.Encoder

import db.transactor
import types.{ChunkId, FileId}

case class FileChunkRelation(
    chunk_id: ChunkId,
    file_id: FileId,
    chunk_seq: Int
)

def create_file_chunk_relationship_table(): IO[Unit] =
  sql"""
    create table if not exists file_chunk (
      file_id INTEGER,
      chunk_id VARCHAR(64),
      chunk_seq INTEGER,
      primary key(file_id, chunk_id, chunk_seq)
      foreign key (file_id) references file_metadata(chunk_id) on delete cascade
      foreign key (chunk_id) references chunk_metadata(chunk_id) on delete cascade
    )""".update.run.void
    .transact(transactor)
