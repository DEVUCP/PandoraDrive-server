package routes

import org.http4s.dsl.impl.QueryParamDecoderMatcher
import types.{ChunkId, FileId, FolderId}
object FileIdQueryParamMatcher
    extends QueryParamDecoderMatcher[FileId]("file_id")
object FolderIdQueryParamMatcher
    extends QueryParamDecoderMatcher[FolderId]("folder_id")
object ParnetFolderIdQueryParamMatcher
    extends QueryParamDecoderMatcher[FolderId]("parent_folder_id")
object UserIdQueryParamMatcher extends QueryParamDecoderMatcher[Int]("user_id")
