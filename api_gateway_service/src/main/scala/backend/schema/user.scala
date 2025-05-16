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

case class ChunkMetadata(
    user_id: String,
    username: String,
    password: String
)

def create_user_table(): IO[Unit] =
  sql"""
    CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username   VARCHAR(64) NOT NULL,
    password   VARCHAR(64) NOT NULL
    );
    """.update.run.void.transact(transactor)
