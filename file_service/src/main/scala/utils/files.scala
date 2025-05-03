package utils
import java.nio.file.{Files, Paths, StandardOpenOption}
import cats.effect.IO
import java.nio.file.StandardOpenOption._
import java.io.IOException
import fs2._

object files {
  def store_file(file: Array[Byte], path: String): IO[Unit] = IO {
    val fullPath = Paths.get(
      "storage",
      path
    ) // "storage" is the root directory, can be customized
    Files.createDirectories(fullPath.getParent) // Ensure directories exist
    Files.write(fullPath, file, CREATE, WRITE, TRUNCATE_EXISTING)
  }
  def read_file(path: String): IO[Either[String, Stream[IO, Byte]]] =
    IO {
      val fullPath = Paths.get("storage", path)
      try {
        // Stream the file's bytes instead of reading it into memory all at once
        Right(
          fs2.io.file.readAll[IO](fullPath, 4096)
        ) // 4096 is the buffer size for reading in chunks
      } catch {
        case e: IOException => Left(s"Failed to read the file: ${path}")
      }
    }
}
