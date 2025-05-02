package dto

import types.FileId
case class ChunkMetadataMultipartUpload(
    file_id: FileId,
    chunk_sequence: Int,
    chunk_size: Int
)
