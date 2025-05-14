package backend.utils
import org.http4s._
import org.http4s.client.Client
import org.http4s.implicits._
import cats.effect.IO
import io.circe._
import io.circe.generic.semiauto._
import org.http4s.circe._
import backend.utils.config
import org.http4s.ember.client.EmberClientBuilder

case class AnalyticsResponse(
  LargestFile: String,
  SmallestFile: String,
  NumFiles: Int,
  MostRecentFile: String,
  MostRecentFileUploadDate: String,
  TotalSize: Long,
  NumFilesToday: Int,
  NumFilesThisWeek: Int,
  NumFilesThisMonth: Int,
  NumFilesThisYear: Int,
  LongestVideoLength: String,
  ShortestVideoLength: String,
  NumVideos: Int,
  NumPhotos: Int,
  NumFolders: Int,
  SizeLeft: Long,
  BiggestFile: Int
)

object AnalyticsResponse {
  implicit val decoder: Decoder[AnalyticsResponse] = deriveDecoder
  implicit val encoder: Encoder[AnalyticsResponse] = deriveEncoder
  implicit val entityDecoder: EntityDecoder[IO, AnalyticsResponse] = jsonOf
}

object AnalyticsServiceClient {

  def fetchAnalytics(userId: Int): IO[AnalyticsResponse] = {
    val urlString = s"${config.ANALYTICS_SERVICE_HOST}:${config.ANALYTICS_SERVICE_PORT}/${config.ANALYTICS_SERVICE_PATH}?${config.ANALYTICS_SERVICE_QUERY_PARAM}=${userId}"

    EmberClientBuilder.default[IO].build.use { client =>
      Uri.fromString(urlString) match {
        case Right(uri) =>
          val req = Request[IO](Method.GET, uri = uri)
          client.expect[AnalyticsResponse](req)

        case Left(parseFailure) =>
          IO.raiseError(new RuntimeException(s"Invalid Analytics Service URI: ${parseFailure.details}"))
      }
    }
  }

}
