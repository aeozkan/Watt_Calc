package com.hobbycoding.wattbench.util

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {
    private const val PREFS_NAME = "watt_benchmark_prefs"
    private const val KEY_LANG = "app_language"

    fun getSelectedLanguage(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, null) // null = System default
    }

    fun setLanguage(context: Context, langCode: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            if (langCode == null) {
                remove(KEY_LANG)
            } else {
                putString(KEY_LANG, langCode)
            }
            apply()
        }

        applyLanguage(context, langCode)

        if (context is Activity) {
            context.recreate()
        }
    }

    fun applyLanguage(context: Context, langCode: String?) {
        val targetLocale = if (langCode != null) {
            Locale(langCode)
        } else {
            Locale.getDefault()
        }

        Locale.setDefault(targetLocale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(targetLocale)
        config.setLayoutDirection(targetLocale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as? LocaleManager
            if (langCode != null) {
                localeManager?.applicationLocales = LocaleList.forLanguageTags(langCode)
            } else {
                localeManager?.applicationLocales = LocaleList.getEmptyLocaleList()
            }
        }
    }

    fun wrapContext(context: Context): Context {
        val langCode = getSelectedLanguage(context) ?: return context
        val targetLocale = Locale(langCode)
        Locale.setDefault(targetLocale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(targetLocale)
        config.setLayoutDirection(targetLocale)

        return context.createConfigurationContext(config)
    }
}
