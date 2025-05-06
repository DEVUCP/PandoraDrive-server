package utils
import cats.effect.IO
import types.ChunkId

import java.security.MessageDigest

def hash_chunk(chunkBytes: Array[Byte]): IO[ChunkId] = IO {
  val digest = MessageDigest.getInstance("SHA-256")
  digest.digest(chunkBytes).map("%02x".format(_)).mkString
}
