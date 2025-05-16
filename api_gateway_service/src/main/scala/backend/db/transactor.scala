package db

import doobie._
import doobie.implicits._
import cats._
import cats.data._
import cats.effect.IO
import cats.implicits._
import cats.effect.unsafe.implicits.global
import doobie.util.transactor.Transactor
import utils.config

val transactor: Transactor[IO] = Transactor.fromDriverManager[IO](
  driver = "org.sqlite.JDBC", // driver classname
  url =config.DB_URL,
  logHandler = None
)

