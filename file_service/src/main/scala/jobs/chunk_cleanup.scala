package jobs

import cats.effect._
import cats.implicits._
import cats.effect.std.Queue
import doobie._
import doobie.implicits._
import scala.concurrent.duration._
import scala.util.control.NonFatal
import types.ChunkId
import services.chunk_service
import db.transactor
import cats.Id
import utils.files
import fs2.Stream

object ChunkCleanup {
  def runJob(cleanup_interval: Interval): IO[Unit] =
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

    scheduleCleanup(cleanup_interval).compile.drain
}
