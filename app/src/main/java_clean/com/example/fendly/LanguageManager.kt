package com.example.fendly

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LanguageManager {
    private const val PREF_NAME = "fendly_language"
    private const val KEY_LANG_CODE = "selected_language_code"
    private const val KEY_LANG_INDEX = "selected_language_index"
    private val supportedLanguageCodes = arrayOf("en", "hi", "mr", "gu", "bn", "ta", "te", "kn", "ml")

    @JvmStatic
    fun normalizeLanguageCode(languageCode: String?): String {
        val code = languageCode?.trim()?.ifEmpty { "en" } ?: "en"
        return if (supportedLanguageCodes.contains(code)) code else "en"
    }

    @JvmStatic
    @Suppress("ApplySharedPref")
    fun setAppLanguage(context: Context, languageCode: String?) {
        val safeCode = normalizeLanguageCode(languageCode)
        val index = supportedLanguageCodes.indexOf(safeCode).takeIf { it >= 0 } ?: 0

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG_CODE, safeCode)
            .putInt(KEY_LANG_INDEX, index)
            .apply()

        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(safeCode))
    }

    @JvmStatic
    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return normalizeLanguageCode(prefs.getString(KEY_LANG_CODE, "en"))
    }

    @JvmStatic
    fun getSavedLanguageIndex(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedCode = normalizeLanguageCode(prefs.getString(KEY_LANG_CODE, "en"))
        return supportedLanguageCodes.indexOf(savedCode).takeIf { it >= 0 } ?: 0
    }

    @JvmStatic
    fun wrapContext(context: Context): Context {
        val safeCode = getSavedLanguage(context)
        val locale = Locale.forLanguageTag(safeCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        return context.createConfigurationContext(config)
    }

    @JvmStatic
    fun restoreSavedLanguage(context: Context) {
        val safeCode = getSavedLanguage(context)
        val locale = Locale.forLanguageTag(safeCode)
        Locale.setDefault(locale)
        val resources = context.resources
        val config = Configuration(resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
