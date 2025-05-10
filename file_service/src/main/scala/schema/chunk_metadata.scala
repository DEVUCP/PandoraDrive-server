package schema

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
import types.ChunkId

case class ChunkMetadata(
    chunk_id: ChunkId,
    ref_count: Int, // The reference count of the chunk
    chunk_size: Int
)
