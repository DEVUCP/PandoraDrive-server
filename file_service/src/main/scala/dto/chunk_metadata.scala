package dto

import types.ChunkId
case class DTOChunkMetadata(
    chunk_id: ChunkId,
    chunk_sequence: Int,
    byte_size: BigInt
)
