
package utils

import com.comcast.ip4s.Port
import  com.comcast.ip4s.port

object config {
    val DB_URL = sys.env.getOrElse("DB_URL", "jdbc:sqlite:db.db")
    val SERVICE_PORT : Port = Port.fromString(sys.env.getOrElse("GATEWAY_PORT", "55551")).getOrElse(port"55551")
    val FILE_SERVICE_PORT : Port = Port.fromString(sys.env.getOrElse("FILE_SERVICE_PORT", "55555")).getOrElse(port"55555")
    val JWT_SECRET : String = sys.env.getOrElse("JWT_SECRET", "MOCK_SECRET")
    val JWT_EXPIRY_IN_SECONDS = sys.env.get("JWT_EXPIRY_IN_SECONDS").flatMap(_.toIntOption).getOrElse(2592000)
    val CHATBOT_SERVICE_PORT: String = sys.env.getOrElse("CHATBOT_SERVICE_PORT", "55550")
    val CHATBOT_SERVICE_URL: String = sys.env.getOrElse("CHATBOT_SERVICE_URL", "http://localhost")
    val CLIENT_PORT: Int = sys.env.get("CLIENT_PORT").flatMap(_.toIntOption).getOrElse(5173)
    val CLIENT_DOMAIN: String = sys.env.getOrElse("CLIENT_DOMAIN", "localhost")

}