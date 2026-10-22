package com.example.steprange.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.steprange.data.model.AppRoundingMode
import com.example.steprange.data.model.AppSettings
import com.example.steprange.data.model.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val themeName = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        val theme = try { AppTheme.valueOf(themeName) } catch (_: Exception) { AppTheme.SYSTEM }

        val dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        val hapticFeedback = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true)
        val animations = prefs.getBoolean(KEY_ANIMATIONS, true)
        val confirmDelete = prefs.getBoolean(KEY_CONFIRM_DELETE, true)
        val decimalPlaces = prefs.getInt(KEY_DECIMAL_PLACES, 10)

        val roundingModeName = prefs.getString(KEY_ROUNDING_MODE, AppRoundingMode.HALF_UP.name)
            ?: AppRoundingMode.HALF_UP.name
        val roundingMode = try { AppRoundingMode.valueOf(roundingModeName) } catch (_: Exception) { AppRoundingMode.HALF_UP }

        return AppSettings(
            theme = theme,
            dynamicColor = dynamicColor,
            hapticFeedback = hapticFeedback,
            animations = animations,
            confirmDelete = confirmDelete,
            decimalPlaces = decimalPlaces,
            roundingMode = roundingMode
        )
    }

    fun updateTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _settings.value = _settings.value.copy(theme = theme)
    }

    fun updateDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _settings.value = _settings.value.copy(dynamicColor = enabled)
    }

    fun updateHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply()
        _settings.value = _settings.value.copy(hapticFeedback = enabled)
    }

    fun updateAnimations(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANIMATIONS, enabled).apply()
        _settings.value = _settings.value.copy(animations = enabled)
    }

    fun updateConfirmDelete(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CONFIRM_DELETE, enabled).apply()
        _settings.value = _settings.value.copy(confirmDelete = enabled)
    }

    fun updateDecimalPlaces(places: Int) {
        prefs.edit().putInt(KEY_DECIMAL_PLACES, places).apply()
        _settings.value = _settings.value.copy(decimalPlaces = places)
    }

    fun updateRoundingMode(mode: AppRoundingMode) {
        prefs.edit().putString(KEY_ROUNDING_MODE, mode.name).apply()
        _settings.value = _settings.value.copy(roundingMode = mode)
    }

    fun saveLastInputs(start: String, increment: String, end: String, includeEnd: Boolean) {
        prefs.edit()
            .putString(KEY_LAST_START, start)
            .putString(KEY_LAST_INCREMENT, increment)
            .putString(KEY_LAST_END, end)
            .putBoolean(KEY_LAST_INCLUDE_END, includeEnd)
            .apply()
    }

    fun getLastStart(): String = prefs.getString(KEY_LAST_START, "") ?: ""
    fun getLastIncrement(): String = prefs.getString(KEY_LAST_INCREMENT, "") ?: ""
    fun getLastEnd(): String = prefs.getString(KEY_LAST_END, "") ?: ""
    fun getLastIncludeEnd(): Boolean = prefs.getBoolean(KEY_LAST_INCLUDE_END, true)

    fun resetSettings() {
        prefs.edit()
            .remove(KEY_THEME)
            .remove(KEY_DYNAMIC_COLOR)
            .remove(KEY_HAPTIC_FEEDBACK)
            .remove(KEY_ANIMATIONS)
            .remove(KEY_CONFIRM_DELETE)
            .remove(KEY_DECIMAL_PLACES)
            .remove(KEY_ROUNDING_MODE)
            .apply()
        _settings.value = AppSettings()
    }

    companion object {
        private const val PREFS_NAME = "step_range_settings"
        private const val KEY_THEME = "theme"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        private const val KEY_ANIMATIONS = "animations"
        private const val KEY_CONFIRM_DELETE = "confirm_delete"
        private const val KEY_DECIMAL_PLACES = "decimal_places"
        private const val KEY_ROUNDING_MODE = "rounding_mode"

        private const val KEY_LAST_START = "last_start"
        private const val KEY_LAST_INCREMENT = "last_increment"
        private const val KEY_LAST_END = "last_end"
        private const val KEY_LAST_INCLUDE_END = "last_include_end"
    }
}
