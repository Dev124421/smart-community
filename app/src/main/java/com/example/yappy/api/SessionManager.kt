package com.example.yappy.api

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("yappy_session", Context.MODE_PRIVATE)

    fun saveTokens(access: String, refresh: String) {
        prefs.edit().putString("access", access).putString("refresh", refresh).apply()
    }
    fun getAccessToken(): String? = prefs.getString("access", null)
    fun getRefreshToken(): String? = prefs.getString("refresh", null)
    fun isLoggedIn(): Boolean = getAccessToken() != null
    fun clear() = prefs.edit().clear().apply()
    fun setRememberMe(remember: Boolean) {
        prefs.edit().putBoolean("remember", remember).apply()
    }
    fun shouldAutoLogin(): Boolean = isLoggedIn() && prefs.getBoolean("remember", false)

}