package utils
import java.security.MessageDigest
import types.ChunkId
import cats.effect.IO

def hash_chunk(chunkBytes: Array[Byte]): IO[ChunkId] = IO {
  val digest = MessageDigest.getInstance("SHA-256")
  digest.digest(chunkBytes).map("%02x".format(_)).mkString
}
