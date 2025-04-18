package db

import doobie._
import doobie.implicits._
import cats._
import cats.data._
import cats.effect.IO
import cats.implicits._
import cats.effect.unsafe.implicits.global
import doobie.util.transactor.Transactor

val transactor: Transactor[IO] = Transactor.fromDriverManager[IO](
  driver = "org.sqlite.JDBC", // driver classname
  url = "jdbc:sqlite:db.db", // connect URL
  logHandler = None
)
