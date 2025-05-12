package jobs

import scala.concurrent.duration._
import scala.util.control.NonFatal

import cats.Id
import cats.implicits._

import cats.effect._
import cats.effect.std.Queue

import db.transactor
import doobie._
import doobie.implicits._
import fs2.Stream
import services.chunk_service
import types.ChunkId
import utils.files

object ChunkCleanup {
  def runJob(): IO[Unit] =
    def job(): IO[Unit] =
      IO.println("Job has started") *>
        sql"""select chunk_id from chunk_metadata where ref_count <= 0"""
          .query[ChunkId]
          .to[List]
          .transact(transactor)
          .attempt
          .flatMap {
            case Left(err) => IO.println(err)
            case Right(chunks) =>
              chunks
                .traverse { chunk_id =>
                  files
                    .remove_file(chunk_service.construct_file_name(chunk_id))
                    .attempt
                }
                .flatMap { results =>
                  if (!results.forall(_.isRight))
                    IO.println(s"Chunk Cleanup job failed")
                  else
                    sql"""delete from chunk_metadata where ref_count <= 0""".update.run.void
                      .transact(transactor)
                }
          }
    def scheduleCleanup(interval: FiniteDuration): Stream[IO, Unit] =
      Stream.awakeEvery[IO](interval) >> Stream.eval(job())

    scheduleCleanup(6.hour).compile.drain
}
