package utils
import java.nio.file.{Files, Paths, StandardOpenOption}
import cats.effect.IO
import java.nio.file.StandardOpenOption._

object files {
  def store_file(file: Array[Byte], path: String): IO[Unit] = IO {
    val fullPath = Paths.get(
      "storage",
      path
    ) // "storage" is the root directory, can be customized
    Files.createDirectories(fullPath.getParent) // Ensure directories exist
    Files.write(fullPath, file, CREATE, WRITE, TRUNCATE_EXISTING)
  }
}
