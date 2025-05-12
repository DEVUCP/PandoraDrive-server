package utils

object config {
  val JWT_SECRET: String = sys.env.getOrElse("JWT_SECRET", "MOCK_SECRET")
  val ENCRYPT_SECRET: String =
    sys.env.getOrElse("ENCRYPT_SECRET", "MOCK_SECRET")
  val DB_URL: String = sys.env.getOrElse("DB_URL", "")
  val CHUNK_SIZE: Int =
    sys.env.get("CHUNK_SIZE").flatMap(_.toIntOption).getOrElse(1024 * 1024)
  val JWT_EXPIRY_IN_SECONDS =
    sys.env.get("JWT_EXPIRY_IN_SECONDS").flatMap(_.toIntOption).getOrElse(3600)
  val SERVICE_PORT: String =
    sys.env.getOrElse("FILE_SERVICE_PORT", "55555")
  val SERVICE_URL: String =
    sys.env.getOrElse("FILE_SERVICE_URL", "http://localhost")
}
