package utils
import cats.effect.IO
import java.nio.file.StandardOpenOption._
import java.io.IOException
import fs2.Stream
import fs2.io.file.{Files => FS2Files, Path, Flags}
import cats.effect.IO

object files {
  def store_file(file: Array[Byte], path: String): IO[Unit] = {
    val fullPath = Path("storage") / path
    val byteStream = Stream.emits(file).covary[IO]

    FS2Files[IO].createDirectories(
      fullPath.parent.getOrElse(Path("storage"))
    ) *>
      byteStream.through(FS2Files[IO].writeAll(fullPath)).compile.drain
  }

  def read_file(path: String): IO[Either[String, Stream[IO, Byte]]] = IO {
    val fullPath = Path("storage") / path
    try {
      Right(FS2Files[IO].readAll(fullPath, 4096, Flags.Read))
    } catch {
      case _: IOException => Left(s"Failed to read the file: $path")
    }
  }
}
