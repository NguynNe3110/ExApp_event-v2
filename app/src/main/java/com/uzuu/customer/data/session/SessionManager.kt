package com.uzuu.customer.data.session

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREF_NAME = "app_prefs"
    private const val KEY_TOKEN    = "access_token"
    private const val KEY_USERNAME = "username"
    private const val KEY_AVATAR   = "avatar_uri"

    private lateinit var prefs: SharedPreferences

    // session events
    enum class SessionEvent { LoggedOut }
    private val _events = kotlinx.coroutines.flow.MutableSharedFlow<SessionEvent>(extraBufferCapacity = 1)

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveUsername(username: String) = prefs.edit().putString(KEY_USERNAME, username).apply()
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun saveAvatarUri(uri: String) = prefs.edit().putString(KEY_AVATAR, uri).apply()
    fun getAvatarUri(): String? = prefs.getString(KEY_AVATAR, null)

    fun clear() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USERNAME)
            .apply()
    }

    fun sessionEvents(): kotlinx.coroutines.flow.SharedFlow<SessionEvent> = _events

    suspend fun notifyLoggedOut() {
        _events.emit(SessionEvent.LoggedOut)
    }
}