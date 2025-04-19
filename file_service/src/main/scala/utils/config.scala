package utils

val config = {
  val parse = (name: String) => {
    val res = sys.env(name)
    res match {
      case "" => None
      case _  => Some(res)
    }
  }
  Map(
    "JWT_SECRET" -> parse("JWT_SECRET"),
    "DB_URL" -> parse("DB_URL")
  )
}
