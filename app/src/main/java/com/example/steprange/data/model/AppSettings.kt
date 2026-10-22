package com.example.steprange.data.model

import java.math.RoundingMode

enum class AppTheme(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class AppRoundingMode(val displayName: String, val mode: RoundingMode) {
    HALF_UP("Half Up", RoundingMode.HALF_UP),
    HALF_DOWN("Half Down", RoundingMode.HALF_DOWN),
    HALF_EVEN("Half Even", RoundingMode.HALF_EVEN),
    UP("Up", RoundingMode.UP),
    DOWN("Down", RoundingMode.DOWN),
    CEILING("Ceiling", RoundingMode.CEILING),
    FLOOR("Floor", RoundingMode.FLOOR)
}

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = true,
    val hapticFeedback: Boolean = true,
    val animations: Boolean = true,
    val confirmDelete: Boolean = true,
    val decimalPlaces: Int = 10,
    val roundingMode: AppRoundingMode = AppRoundingMode.HALF_UP
) {
    companion object {
        val ALLOWED_DECIMAL_PLACES = listOf(0, 2, 4, 6, 8, 10, 12, 16, 20, 30, 50)
    }
}
