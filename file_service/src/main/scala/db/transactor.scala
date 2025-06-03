package db

import cats._
import cats.data._
import cats.implicits._

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.log.{
  LogHandler,
  LogEvent,
  Success,
  ProcessingFailure,
  ExecFailure
}

import utils.config

val handler: LogHandler[IO] = new LogHandler[IO] {
  def run(event: LogEvent) = event match {
    case Success(sql, args, _, _, processing_time) =>
      IO.println(
        s"Executed: $sql | args: $args | Procesing Time: ${processing_time.toMillis} ms"
      )
    case ProcessingFailure(sql, args, _, _, processing_time, failure) =>
      IO.println(
        s"Processing failure: $sql | args: $args | Processing Time: ${processing_time.toMillis} ms",
        failure
      )
    case ExecFailure(sql, args, _, _, failure) =>
      IO.println(s"Execution failure: $sql | args: $args", failure)
  }
}

val transactor: Transactor[IO] = Transactor.fromDriverManager[IO](
  driver = "org.sqlite.JDBC",
  url = config.DB_URL,
  logHandler = Some(handler)
)
