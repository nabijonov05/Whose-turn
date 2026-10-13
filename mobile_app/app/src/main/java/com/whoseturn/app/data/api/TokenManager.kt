package com.whoseturn.app.data.api

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("whose_turn_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_USERNAME = "username"
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/api/v1/"
    }

    fun saveTokens(access: String, refresh: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, access)
            .putString(KEY_REFRESH_TOKEN, refresh)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun saveUsername(username: String) {
        prefs.edit().putString(KEY_USERNAME, username).apply()
    }

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun getBaseUrl(): String {
        var url = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        if (!url.endsWith("/")) {
            url += "/"
        }
        return url
    }

    fun setBaseUrl(url: String) {
        var cleanUrl = url.trim()
        if (!cleanUrl.endsWith("/")) {
            cleanUrl += "/"
        }
        prefs.edit().putString(KEY_BASE_URL, cleanUrl).apply()
    }

    fun clear() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USERNAME)
            .apply()
    }

    fun isLoggedIn(): Boolean = !getAccessToken().isNullOrEmpty()
}
