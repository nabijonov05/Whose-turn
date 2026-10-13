package com.whoseturn.app.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {
    private const val PREFS_NAME = "whose_turn_theme"
    private const val KEY_THEME = "app_theme_mode"

    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    fun applyTheme(context: Context) {
        val mode = getThemeMode(context)
        when (mode) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun setThemeMode(context: Context, mode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, mode)
            .apply()
        applyTheme(context)
    }

    fun getThemeMode(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM
    }
}
