package utils
import java.io.IOException
import java.nio.file.StandardOpenOption._

import cats.effect.IO

import fs2.Stream
import fs2.io.file.{Files => FS2Files, Flags, Path}

object files {
  def store_file(file: Array[Byte], path: String): IO[Unit] = {
    val fullPath = Path("storage") / path
    val byteStream = Stream.emits(file).covary[IO]

    FS2Files[IO].createDirectories(
      fullPath.parent.getOrElse(Path("storage"))
    ) *>
      byteStream.through(FS2Files[IO].writeAll(fullPath)).compile.drain
  }

  def read_file(path: String): IO[Either[String, Stream[IO, Byte]]] =
    IO.blocking {
      val fullPath = Path("storage") / path
      try {
        Right(FS2Files[IO].readAll(fullPath, 4096, Flags.Read))
      } catch { IOException =>
        Left(s"Failed to read the file: $path")
      }
    }
  def remove_file(path: String): IO[Either[String, Unit]] = {
    val fullPath = Path("storage") / path
    FS2Files[IO]
      .delete(fullPath)
      .attempt
      .map {
        case Right(_) => Right(())
        case Left(e: IOException) =>
          Left(s"Failed to delete file: $path. Error: ${e.getMessage}")
        case Left(e) =>
          Left(s"Unexpected error deleting file: $path. Error: ${e.getMessage}")
      }
  }
}
