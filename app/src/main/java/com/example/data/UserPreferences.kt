package com.example.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lumina_prefs", Context.MODE_PRIVATE)

    var customGeminiApiKey: String
        get() = prefs.getString("gemini_api_key", "") ?: ""
        set(value) = prefs.edit().putString("gemini_api_key", value.trim()).apply()

    var defaultExportQuality: Int
        get() = prefs.getInt("export_quality", 100)
        set(value) = prefs.edit().putInt("export_quality", value).apply()

    var defaultExportFormat: String
        get() = prefs.getString("export_format", "JPG") ?: "JPG"
        set(value) = prefs.edit().putString("export_format", value).apply()
}
