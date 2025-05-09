
package utils

import com.comcast.ip4s.Port
import  com.comcast.ip4s.port

object config {
    val DB_URL = sys.env.getOrElse("DB_URL", "jdbc:sqlite:db.db")
    val SERVICE_PORT : Port = Port.fromString(sys.env.getOrElse("GATEWAY_PORT", "55551")).getOrElse(port"55551")
    val FILE_SERVICE_PORT : Port = Port.fromString(sys.env.getOrElse("FILE_SERVICE_PORT", "55555")).getOrElse(port"55555")
}