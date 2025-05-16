package backend.utils

import com.comcast.ip4s.Port
import  com.comcast.ip4s.port

object config {
  val TOKENS_FILE_PATH : String = sys.env.getOrElse("TOKENS_FILE_PATH", "src/main/scala/backend/data/tokens.json")
  val SERVICE_PORT : Port = Port.fromString(sys.env.getOrElse("CHATBOT_SERVICE_PORT", "55550")).getOrElse(port"55550")
  val ANALYTICS_SERVICE_PORT : Port = Port.fromString(sys.env.getOrElse("ANALYTICS_SERVICE_PORT", "55552")).getOrElse(port"55552")
  val ANALYTICS_SERVICE_HOST: String = sys.env.getOrElse("ANALYTICS_SERVICE_HOST", "http://localhost")
  val ANALYTICS_SERVICE_PATH: String = sys.env.getOrElse("ANALYTICS_SERVICE_PATH", "analytics")
  val ANALYTICS_SERVICE_QUERY_PARAM: String = sys.env.getOrElse("ANALYTICS_SERVICE_QUERY_PARAM", "user_id")
}