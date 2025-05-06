package routes

import org.http4s.dsl.impl.QueryParamDecoderMatcher
import types.ChunkId
import types.FileId
import types.FolderId
object FileIdQueryParamMatcher
    extends QueryParamDecoderMatcher[FileId]("file_id")
object FolderIdQueryParamMatcher
    extends QueryParamDecoderMatcher[FolderId]("folder_id")
