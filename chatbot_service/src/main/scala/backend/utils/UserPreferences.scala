package backend.utils

object UserPreferences {
  private var userPref: Option[String] = None

  def storeUserPreferences(pref: String): Unit = {
    userPref = Some(pref.trim.toLowerCase)
  }

  def getUserPreferences(): Option[String] = userPref
}
