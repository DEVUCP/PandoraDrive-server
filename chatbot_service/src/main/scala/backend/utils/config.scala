package backend.utils

import com.comcast.ip4s.Port
import  com.comcast.ip4s.port

object config {
  val TOKENS_FILE_PATH : String = sys.env.getOrElse("TOKENS_FILE_PATH", "src/main/scala/backend/data/tokens.json")
  val SERVICE_PORT : Port = Port.fromString(sys.env.getOrElse("CHATBOT_SERVICE_PORT", "55550")).getOrElse(port"55550")
  val FILE_SERVICE_PORT : Port = Port.fromString(sys.env.getOrElse("FILE_SERVICE_PORT", "55555")).getOrElse(port"55555")
}