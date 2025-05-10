package schema

import types.FileId
import types.ChunkId

case class FileChunkRelation(
    chunk_id: ChunkId,
    file_id: FileId,
    chunk_seq: Int
)
