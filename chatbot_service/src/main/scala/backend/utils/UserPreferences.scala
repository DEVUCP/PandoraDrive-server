package backend.utils

object Preferences {
  private var userPref: Option[String] = None

  // TODO: Store the user preference as an Option
  def storeUserPreferences(pref: String): Unit = ???

  // TODO: Retrieve the stored user preference, if it exists
  def getUserPreferences(): Option[String] = ???
}
