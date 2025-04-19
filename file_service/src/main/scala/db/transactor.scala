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
  url = {
    val res = config("DB_URL")
    res match {
      case None      => ""
      case Some(url) => url
    }
  }, // connect URL
  logHandler = None
)
