package model

import doobie._
import doobie.implicits._
import cats._
import cats.data._
import cats.effect.IO
import cats.implicits._
import cats.effect.unsafe.implicits.global
import io.circe.Encoder
import types.{ChunkId, FileId}

import dto.ChunkMetadataMultipartUpload
import types.ChunkId
import db.transactor

def chunk_exists(chunkId: ChunkId): IO[Boolean] = {
  sql"""
    SELECT 1 FROM chunk_metadata WHERE chunk_id = $chunkId
  """
    .query[Int]
    .option
    .map(_.isDefined)
    .transact(transactor)
}

def chunk_reference_add(chunkId: ChunkId, diff: Int): IO[Unit] =
  sql"""
   update chunk_metadata
   set ref_count = ref_count + $diff
   where chunk_id = $chunkId
  """.update.run.void.transact(transactor)

def create_chunk_metadata(chunkId: ChunkId, chunkSize: Int): IO[Unit] =
  sql"""
  insert into chunk_metadata(chunk_id, byte_size) values($chunkId, $chunkSize)
  """.update.run.void.transact(transactor)

def remove_file_chunks(file_id: FileId): IO[Unit] =
  sql"""update chunk_metadata set ref_count = ref_count - 1 where chunk_id in (select chunk_id from file_chunk where file_id=$file_id)""".update.run.void
    .transact(transactor)
// TODO: Create a background job in scala to remove unused chunks
