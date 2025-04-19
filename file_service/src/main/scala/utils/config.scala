package utils

val config =
  Map(
    "JWT_SECRET" -> sys.env.get("JWT_SECRET"),
    "DB_URL" -> sys.env.get("DB_URL")
  )
