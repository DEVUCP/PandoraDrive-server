package utils

val config =
  Map[String, Option[String]](
    "JWT_SECRET" -> sys.env.get("JWT_SECRET"),
    "DB_URL" -> sys.env.get("DB_URL")
  )
