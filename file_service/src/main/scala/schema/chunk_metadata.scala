package schema

import cats._
import cats.data._
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import db.transactor
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.circe.Encoder
import types.ChunkId

case class ChunkMetadata(
    chunk_id: ChunkId,
    ref_count: Int, // The reference count of the chunk
    chunk_size: Int
)

def create_chunk_metadata_table(): IO[Unit] =
  sql"""
    CREATE TABLE IF NOT EXISTS chunk_metadata (
        chunk_id     VARCHAR(64) PRIMARY KEY,
        byte_size    INT NOT NULL,
        ref_count    INT DEFAULT 1
    );
  """.update.run.void.transact(transactor)
