package model

import cats._
import cats.data._
import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._

import types.{FileId, ChunkId}
import db.transactor
import utils.config

def create_file_chunk_link(
    file_id: FileId,
    chunk_id: ChunkId,
    chunk_seq: Int
): IO[Unit] =
  IO.println("Creating file chunk") *>
    sql"""
  insert into file_chunk(file_id, chunk_id, chunk_seq) values($file_id, $chunk_id, $chunk_seq)
  """.update.run.void.transact(transactor)

def is_file_chunks_uploaded(file_id: FileId): IO[Boolean] =
  for {
    // Get the actual chunk count and validity of the chunk sequence
    chunkData <- sql"""
      SELECT COUNT(*) as count, 
             coalesce(MIN(chunk_seq), 0) as min,
             coalesce(MAX(chunk_seq), 0) as max
      FROM file_chunk
      WHERE file_id = $file_id
    """.query[(Int, Int, Int)].unique.transact(transactor)

    (count, min, max) = chunkData
    // Get the size_bytes from file_metadata table
    sizeBytesOption <- sql"""
      SELECT size_bytes FROM file_metadata WHERE file_id = $file_id
    """.query[Option[Int]].unique.transact(transactor)

    // Calculate the expected chunk count based on size_bytes
    result <- sizeBytesOption match {
      case Some(sizeBytes) =>
        val chunkSize =
          config.CHUNK_SIZE // Assuming config.CHUNK_SIZE is the configured chunk size
        val expectedChunkCount = math.ceil(sizeBytes.toDouble / chunkSize).toInt

        // Compare the chunk count from file_chunk table with the expected chunk count
        if (
          (count, min, max) == (expectedChunkCount, 0, expectedChunkCount - 1)
        ) {
          IO.pure(true) // Chunks match and sequence is valid
        } else {
          IO.pure(false) // Either chunk count or sequence is invalid
        }

      case None =>
        // Handle the case where the file metadata doesn't exist or the size_bytes is missing
        IO.pure(false) // Return false if no size_bytes found for the file
    }
  } yield result
