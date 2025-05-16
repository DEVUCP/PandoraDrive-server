package schema

import types.ChunkId

case class ChunkMetadata(
    chunk_id: ChunkId,
    ref_count: Int, // The reference count of the chunk
    chunk_size: Int
)
