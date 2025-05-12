package db

import cats._
import cats.data._
import cats.implicits._

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import utils.config

val transactor: Transactor[IO] = Transactor.fromDriverManager[IO](
  driver = "org.sqlite.JDBC",
  // url = config.DB_URL,
  url = config.DB_URL,
  logHandler = None
)
