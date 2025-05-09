package dto

case class DTOFileDownloadBody(
    download_link: String,
    data: List[DTOChunkMetadata]
)
