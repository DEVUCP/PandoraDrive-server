package backend.utils
object Preferences {
  private var userPref: Option[String] = None
  def storeUserPreferences(pref: String): Unit = {
    userPref = Some(pref.trim.toLowerCase)
  }
  // TODO: Retrieve the stored user preference, if it exists
  def getUserPreferences(): Option[String] = ???
}