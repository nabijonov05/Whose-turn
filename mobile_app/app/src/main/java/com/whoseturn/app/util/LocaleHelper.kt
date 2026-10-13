package com.whoseturn.app.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {
    private const val PREFS_NAME = "whose_turn_locale"
    private const val KEY_LANG = "app_language"

    const val LANG_UZ = "uz"
    const val LANG_RU = "ru"
    const val LANG_EN = "en"

    fun setLocale(context: Context, langCode: String): Context {
        persistLanguage(context, langCode)
        return updateResources(context, langCode)
    }

    fun getLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANG, LANG_UZ) ?: LANG_UZ
    }

    private fun persistLanguage(context: Context, langCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, langCode)
            .apply()
    }

    fun onAttach(context: Context): Context {
        val lang = getLanguage(context)
        return updateResources(context, lang)
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}
