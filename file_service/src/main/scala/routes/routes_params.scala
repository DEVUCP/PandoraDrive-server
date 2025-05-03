package routes

import org.http4s.dsl.impl.QueryParamDecoderMatcher
import types.{FileId, FolderId, ChunkId}
object FileIdQueryParamMatcher
    extends QueryParamDecoderMatcher[FileId]("file_id")
object FolderIdQueryParamMatcher
    extends QueryParamDecoderMatcher[FolderId]("folder_id")
object ChunkIdQueryParamMatcher
    extends QueryParamDecoderMatcher[ChunkId]("chunk_id")
