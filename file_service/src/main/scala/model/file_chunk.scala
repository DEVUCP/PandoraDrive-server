package model

import cats._
import cats.data._
import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._

import types.{FileId, ChunkId}
import db.transactor

def create_file_chunk_link(
    file_id: FileId,
    chunk_id: ChunkId,
    chunk_seq: Int
): IO[Unit] =
  sql"""
  insert into file_chunk(file_id, chunk_id, chunk_seq) values($file_id, $chunk_id, $chunk_seq)
  """.update.run.void.transact(transactor)
