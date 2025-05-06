package dto

import types.FileId
case class ChunkMetadataMultipartUpload(
    token: String,
    chunk_sequence: Int,
    chunk_size: Int
)
