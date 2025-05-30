package model

import cats._
import cats.data._
import cats.implicits._

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import db.transactor
import doobie._
import doobie.implicits._
import dto.{ChunkMetadataMultipartUpload, DTOChunkMetadata}
import io.circe.Encoder
import schema.ChunkMetadata
import types.{ChunkId, FileId}

def get_chunk_metadata(chunkId: ChunkId): IO[Option[ChunkMetadata]] = {
  sql"""
    SELECT chunk_id, ref_count, byte_size FROM chunk_metadata WHERE chunk_id = $chunkId
  """
    .query[ChunkMetadata]
    .option
    .transact(transactor)
    .handleErrorWith(err => IO.println(err) *> IO.pure(None))
}
def chunk_exists(chunkId: ChunkId): IO[Boolean] = {
  sql"""
    SELECT 1 FROM chunk_metadata WHERE chunk_id = $chunkId
  """
    .query[Int]
    .option
    .map(_.isDefined)
    .transact(transactor)
    .handleErrorWith(err => IO.println(err) *> IO.pure(false))
}

def chunk_reference_add(chunkId: ChunkId, diff: Int): IO[Unit] =
  sql"""
   update chunk_metadata
   set ref_count = ref_count + $diff
   where chunk_id = $chunkId
  """.update.run.void
    .transact(transactor)
    .handleErrorWith(err => IO.println(err))

def create_chunk_metadata(chunkId: ChunkId, chunkSize: Int): IO[Unit] =
  sql"""
  insert into chunk_metadata(chunk_id, byte_size) values($chunkId, $chunkSize)
  """.update.run.void.transact(transactor)

def remove_file_chunks(file_id: FileId, user_id: Int): IO[Unit] =
  sql"""
    update chunk_metadata set ref_count = ref_count - 1 
    where chunk_id in 
      (select chunk_id from file_chunk where file_id=(select file_id from file_metadata where file_id=$file_id and user_id = $user_id))""".update.run.void
    .transact(transactor)
    .handleErrorWith(err => IO.println(err))

def get_file_chunks_metadata(
    file_id: FileId
): IO[Either[String, List[DTOChunkMetadata]]] =
  implicit val bigIntMeta: Meta[BigInt] = Meta[Long].timap(BigInt(_))(_.toLong)
  sql"""select cm.chunk_id, fc.chunk_seq, cm.byte_size from file_chunk fc inner join chunk_metadata cm on fc.chunk_id = cm.chunk_id where file_id = $file_id order by chunk_seq"""
    .query[DTOChunkMetadata]
    .to[List]
    .transact(transactor)
    .attempt
    .map {
      case Right(lst) => Right(lst)
      case Left(error) =>
        Left(s"Database Internal Error: ${error.getMessage}")
    }
