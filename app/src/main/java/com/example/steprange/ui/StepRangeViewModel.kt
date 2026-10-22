package com.example.steprange.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.steprange.data.model.AppRoundingMode
import com.example.steprange.data.model.AppSettings
import com.example.steprange.data.model.AppTheme
import com.example.steprange.data.model.StepRangeHistory
import com.example.steprange.data.model.StepRangeResult
import com.example.steprange.data.repository.HistoryRepository
import com.example.steprange.data.repository.SettingsRepository
import com.example.steprange.domain.math.StepRangeCalculator
import com.example.steprange.domain.math.StepRangeFormatter
import com.example.steprange.domain.math.StepRangeParser
import com.example.steprange.domain.math.StepRangeValidator
import com.example.steprange.domain.math.ValidationResult
import com.example.steprange.util.HapticFeedbackHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.math.BigDecimal

class StepRangeViewModel @JvmOverloads constructor(
    application: Application,
    private val settingsRepository: SettingsRepository = SettingsRepository(application),
    private val historyRepository: HistoryRepository = HistoryRepository(application),
    private val hapticHelper: HapticFeedbackHelper = HapticFeedbackHelper(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        StepRangeUiState(
            startInput = settingsRepository.getLastStart().ifEmpty { "0" },
            incrementInput = settingsRepository.getLastIncrement().ifEmpty { "2" },
            endInput = settingsRepository.getLastEnd().ifEmpty { "10" },
            includeEnd = settingsRepository.getLastIncludeEnd(),
            settings = settingsRepository.settings.value
        )
    )
    val uiState: StateFlow<StepRangeUiState> = _uiState.asStateFlow()

    private var calculationJob: Job? = null

    init {
        // Observe settings changes
        viewModelScope.launch {
            settingsRepository.settings.collect { newSettings ->
                _uiState.update { it.copy(settings = newSettings) }
            }
        }

        // Observe history changes
        viewModelScope.launch {
            historyRepository.history.collect { newHistory ->
                _uiState.update { it.copy(history = newHistory) }
            }
        }
    }

    fun onStartInputChanged(value: String) {
        _uiState.update {
            it.copy(
                startInput = value,
                startError = null,
                errorMessage = null
            )
        }
    }

    fun onIncrementInputChanged(value: String) {
        _uiState.update {
            it.copy(
                incrementInput = value,
                incrementError = null,
                errorMessage = null
            )
        }
    }

    fun onEndInputChanged(value: String) {
        _uiState.update {
            it.copy(
                endInput = value,
                endError = null,
                errorMessage = null
            )
        }
    }

    fun onIncludeEndChanged(include: Boolean) {
        hapticHelper.performTick(_uiState.value.settings.hapticFeedback)
        _uiState.update { it.copy(includeEnd = include) }
        // If we already have a valid result, recalculate seamlessly
        if (_uiState.value.result != null) {
            generateRange()
        }
    }

    fun clearInputs() {
        hapticHelper.performClick(_uiState.value.settings.hapticFeedback)
        calculationJob?.cancel()
        _uiState.update {
            it.copy(
                startInput = "",
                incrementInput = "",
                endInput = "",
                result = null,
                errorMessage = null,
                warningMessage = null,
                startError = null,
                incrementError = null,
                endError = null,
                findValueIndex = "",
                findValueResult = null,
                findValueError = null,
                calculationSteps = emptyList(),
                isCalculating = false
            )
        }
    }

    fun generateRange() {
        calculationJob?.cancel()

        val state = _uiState.value
        val startStr = state.startInput.trim()
        val incStr = state.incrementInput.trim()
        val endStr = state.endInput.trim()

        // 1. Validate Start
        val startVal = StepRangeValidator.validateStartField(startStr)
        if (!startVal.isValid) {
            val err = startVal.errorMessage ?: "Enter a start value."
            _uiState.update { it.copy(startError = err, errorMessage = err) }
            hapticHelper.performError(state.settings.hapticFeedback)
            return
        }

        // 2. Validate Increment
        val incVal = StepRangeValidator.validateIncrementField(incStr)
        if (!incVal.isValid) {
            val err = incVal.errorMessage ?: "Enter an increment."
            _uiState.update { it.copy(incrementError = err, errorMessage = err) }
            hapticHelper.performError(state.settings.hapticFeedback)
            return
        }

        // 3. Validate End
        val endVal = StepRangeValidator.validateEndField(endStr)
        if (!endVal.isValid) {
            val err = endVal.errorMessage ?: "Enter an end value."
            _uiState.update { it.copy(endError = err, errorMessage = err) }
            hapticHelper.performError(state.settings.hapticFeedback)
            return
        }

        val start = StepRangeParser.parseDecimal(startStr).getOrNull()
        val increment = StepRangeParser.parseDecimal(incStr).getOrNull()
        val end = StepRangeParser.parseDecimal(endStr).getOrNull()

        if (start == null || increment == null || end == null) {
            _uiState.update { it.copy(errorMessage = "Enter a valid decimal number.") }
            hapticHelper.performError(state.settings.hapticFeedback)
            return
        }

        // Cross-field validation
        val rangeVal = StepRangeValidator.validateRange(start, increment, end, state.includeEnd)
        if (!rangeVal.isValid) {
            val err = rangeVal.errorMessage ?: "Invalid range configuration."
            _uiState.update { it.copy(errorMessage = err) }
            hapticHelper.performError(state.settings.hapticFeedback)
            return
        }

        calculationJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCalculating = true,
                    errorMessage = null,
                    warningMessage = null,
                    startError = null,
                    incrementError = null,
                    endError = null,
                    findValueResult = null,
                    findValueError = null
                )
            }

            try {
                withTimeout(5000L) {
                    val result = withContext(Dispatchers.Default) {
                        StepRangeCalculator.generateRange(
                            start = start,
                            increment = increment,
                            end = end,
                            includeEnd = state.includeEnd
                        )
                    }

                    result.fold(
                        onSuccess = { res ->
                            val warning = if (!res.endReached) {
                                "The increment does not land exactly on the end value."
                            } else {
                                null
                            }
                            val steps = StepRangeCalculator.generateCalculationSteps(
                                start = start,
                                increment = increment,
                                values = res.values
                            )

                            // Save last inputs
                            settingsRepository.saveLastInputs(
                                start = startStr,
                                increment = incStr,
                                end = endStr,
                                includeEnd = state.includeEnd
                            )

                            hapticHelper.performClick(state.settings.hapticFeedback)

                            _uiState.update {
                                it.copy(
                                    result = res,
                                    warningMessage = warning,
                                    calculationSteps = steps,
                                    isCalculating = false
                                )
                            }
                        },
                        onFailure = { error ->
                            hapticHelper.performError(state.settings.hapticFeedback)
                            _uiState.update {
                                it.copy(
                                    errorMessage = error.message ?: "Failed to generate range.",
                                    result = null,
                                    isCalculating = false
                                )
                            }
                        }
                    )
                }
            } catch (_: TimeoutCancellationException) {
                hapticHelper.performError(state.settings.hapticFeedback)
                _uiState.update {
                    it.copy(
                        errorMessage = "Calculation took too long. Try a smaller range or larger increment.",
                        result = null,
                        isCalculating = false
                    )
                }
            } catch (_: CancellationException) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Calculation cancelled.",
                        isCalculating = false
                    )
                }
            } catch (e: Exception) {
                hapticHelper.performError(state.settings.hapticFeedback)
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "Calculation error occurred.",
                        result = null,
                        isCalculating = false
                    )
                }
            }
        }
    }

    fun cancelCalculation() {
        calculationJob?.cancel()
        _uiState.update {
            it.copy(
                errorMessage = "Calculation cancelled.",
                isCalculating = false
            )
        }
    }

    fun onFindValueIndexChanged(value: String) {
        _uiState.update {
            it.copy(
                findValueIndex = value,
                findValueError = null
            )
        }
    }

    fun calculateValueAtIndex() {
        val state = _uiState.value
        val indexInput = state.findValueIndex.trim()
        val parseIndex = StepRangeParser.parsePositiveInteger(indexInput)

        if (parseIndex.isFailure) {
            val err = parseIndex.exceptionOrNull()?.message ?: "Enter a valid positive integer."
            _uiState.update { it.copy(findValueError = err) }
            hapticHelper.performError(state.settings.hapticFeedback)
            return
        }

        val index = parseIndex.getOrThrow()
        val startStr = state.startInput.trim()
        val incStr = state.incrementInput.trim()

        val start = StepRangeParser.parseDecimal(startStr).getOrNull()
        val inc = StepRangeParser.parseDecimal(incStr).getOrNull()

        if (start == null || inc == null) {
            _uiState.update { it.copy(findValueError = "Enter valid start and increment values first.") }
            hapticHelper.performError(state.settings.hapticFeedback)
            return
        }

        try {
            val calculatedValue = StepRangeCalculator.calculateValueAtIndex(start, inc, index)
            val formatted = StepRangeFormatter.format(
                calculatedValue,
                state.settings.decimalPlaces,
                state.settings.roundingMode
            )
            hapticHelper.performClick(state.settings.hapticFeedback)
            _uiState.update {
                it.copy(
                    findValueResult = "Value $index = $formatted",
                    findValueError = null
                )
            }
        } catch (e: Exception) {
            hapticHelper.performError(state.settings.hapticFeedback)
            _uiState.update {
                it.copy(findValueError = e.message ?: "Failed to calculate value at index.") }
        }
    }

    fun toggleFormulaExpanded() {
        hapticHelper.performTick(_uiState.value.settings.hapticFeedback)
        _uiState.update { it.copy(isFormulaExpanded = !it.isFormulaExpanded) }
    }

    fun saveCurrentToHistory() {
        val state = _uiState.value
        if (state.result == null) {
            _uiState.update { it.copy(snackbarMessage = "Generate a range before saving.") }
            return
        }

        val added = historyRepository.addHistoryItem(
            start = state.startInput.trim(),
            increment = state.incrementInput.trim(),
            end = state.endInput.trim(),
            includeEnd = state.includeEnd
        )

        hapticHelper.performClick(state.settings.hapticFeedback)
        if (added) {
            _uiState.update { it.copy(snackbarMessage = "Range saved to history.") }
        } else {
            _uiState.update { it.copy(snackbarMessage = "Range is already saved in history.") }
        }
    }

    fun restoreHistoryItem(item: StepRangeHistory) {
        hapticHelper.performClick(_uiState.value.settings.hapticFeedback)
        _uiState.update {
            it.copy(
                startInput = item.start,
                incrementInput = item.increment,
                endInput = item.end,
                includeEnd = item.includeEnd,
                startError = null,
                incrementError = null,
                endError = null,
                errorMessage = null,
                findValueResult = null,
                findValueError = null
            )
        }
        generateRange()
    }

    fun deleteHistoryItem(id: String) {
        hapticHelper.performClick(_uiState.value.settings.hapticFeedback)
        historyRepository.deleteItem(id)
    }

    fun clearHistory() {
        hapticHelper.performClick(_uiState.value.settings.hapticFeedback)
        historyRepository.clearHistory()
        _uiState.update { it.copy(snackbarMessage = "History cleared.") }
    }

    fun updateTheme(theme: AppTheme) {
        settingsRepository.updateTheme(theme)
    }

    fun updateDynamicColor(enabled: Boolean) {
        settingsRepository.updateDynamicColor(enabled)
    }

    fun updateHapticFeedback(enabled: Boolean) {
        settingsRepository.updateHapticFeedback(enabled)
    }

    fun updateAnimations(enabled: Boolean) {
        settingsRepository.updateAnimations(enabled)
    }

    fun updateConfirmDelete(enabled: Boolean) {
        settingsRepository.updateConfirmDelete(enabled)
    }

    fun updateDecimalPlaces(places: Int) {
        settingsRepository.updateDecimalPlaces(places)
        // If we have an existing result, recalculating is not needed because display precision
        // is applied during formatting only!
    }

    fun updateRoundingMode(mode: AppRoundingMode) {
        settingsRepository.updateRoundingMode(mode)
    }

    fun resetSettings() {
        settingsRepository.resetSettings()
        _uiState.update { it.copy(snackbarMessage = "Settings reset to defaults.") }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
