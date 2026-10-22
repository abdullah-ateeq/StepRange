package com.example.steprange.ui

import com.example.steprange.data.model.AppSettings
import com.example.steprange.data.model.CalculationStep
import com.example.steprange.data.model.StepRangeHistory
import com.example.steprange.data.model.StepRangeResult

data class StepRangeUiState(
    val startInput: String = "",
    val incrementInput: String = "",
    val endInput: String = "",

    val includeEnd: Boolean = true,

    val result: StepRangeResult? = null,

    val errorMessage: String? = null,
    val warningMessage: String? = null,

    val isCalculating: Boolean = false,

    val history: List<StepRangeHistory> = emptyList(),

    // Field-level errors
    val startError: String? = null,
    val incrementError: String? = null,
    val endError: String? = null,

    // Direct index lookup
    val findValueIndex: String = "",
    val findValueResult: String? = null,
    val findValueError: String? = null,

    // UI presentation states
    val isFormulaExpanded: Boolean = false,
    val calculationSteps: List<CalculationStep> = emptyList(),
    val snackbarMessage: String? = null,

    // Settings
    val settings: AppSettings = AppSettings()
)
