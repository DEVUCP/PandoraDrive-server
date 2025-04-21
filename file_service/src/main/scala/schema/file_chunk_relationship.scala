package schema
import types.{ChunkId, FileId}

case class FileChunkRelation(
    chunk_id: ChunkId,
    file_id: FileId,
    sequence: Int
)
