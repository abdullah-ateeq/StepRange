package com.example.steprange.domain.math

import com.example.steprange.data.model.AppRoundingMode
import com.example.steprange.data.model.StepRangeResult
import java.math.BigDecimal
import java.math.RoundingMode

object StepRangeFormatter {

    fun format(
        value: BigDecimal,
        decimalPlaces: Int = 10,
        roundingMode: RoundingMode = RoundingMode.HALF_UP
    ): String {
        return try {
            val rounded = value.setScale(decimalPlaces, roundingMode)
            val stripped = rounded.stripTrailingZeros()
            val plain = stripped.toPlainString()
            if (plain == "-0") "0" else plain
        } catch (e: Exception) {
            value.toPlainString()
        }
    }

    fun format(
        value: BigDecimal,
        decimalPlaces: Int,
        appRoundingMode: AppRoundingMode
    ): String = format(value, decimalPlaces, appRoundingMode.mode)

    fun formatSequenceCompact(
        values: List<BigDecimal>,
        decimalPlaces: Int = 10,
        roundingMode: RoundingMode = RoundingMode.HALF_UP,
        maxPreviewCount: Int = 50
    ): String {
        val formattedList = values.take(maxPreviewCount).map { format(it, decimalPlaces, roundingMode) }
        val joined = formattedList.joinToString(", ")
        return if (values.size > maxPreviewCount) {
            "$joined, … (${values.size - maxPreviewCount} more)"
        } else {
            joined
        }
    }

    fun formatShareText(
        result: StepRangeResult,
        decimalPlaces: Int = 10,
        roundingMode: RoundingMode = RoundingMode.HALF_UP
    ): String {
        val startStr = format(result.start, decimalPlaces, roundingMode)
        val endStr = format(result.end, decimalPlaces, roundingMode)
        val incStr = format(result.increment, decimalPlaces, roundingMode)
        val firstStr = format(result.firstValue, decimalPlaces, roundingMode)
        val lastStr = format(result.lastValue, decimalPlaces, roundingMode)
        val minStr = format(result.minimum, decimalPlaces, roundingMode)
        val maxStr = format(result.maximum, decimalPlaces, roundingMode)
        val sumStr = format(result.sum, decimalPlaces, roundingMode)
        val avgStr = format(result.average, decimalPlaces, roundingMode)
        val endpointMode = if (result.includeEnd) "Include End" else "Exclude End"
        val endReachedStr = if (result.endReached) "Yes" else "No"

        val valuesFormatted = result.values.joinToString(", ") { format(it, decimalPlaces, roundingMode) }

        return buildString {
            appendLine("StepRange Sequence")
            appendLine("─────────────────────────")
            appendLine("Start: $startStr")
            appendLine("Increment: $incStr")
            appendLine("End: $endStr ($endpointMode)")
            appendLine("Direction: ${result.direction.name.lowercase().replaceFirstChar { it.uppercase() }}")
            appendLine()
            appendLine("Generated Values (${result.valueCount}):")
            appendLine(valuesFormatted)
            appendLine()
            appendLine("Summary:")
            appendLine("• First Value: $firstStr")
            appendLine("• Last Value: $lastStr")
            appendLine("• End Reached: $endReachedStr")
            appendLine("• Minimum: $minStr")
            appendLine("• Maximum: $maxStr")
            appendLine("• Sum: $sumStr")
            appendLine("• Average: $avgStr")
            appendLine("─────────────────────────")
            appendLine("Generated with StepRange (Offline)")
        }
    }

    fun formatCopyValuesOnly(
        values: List<BigDecimal>,
        decimalPlaces: Int = 10,
        roundingMode: RoundingMode = RoundingMode.HALF_UP
    ): String {
        return values.joinToString(", ") { format(it, decimalPlaces, roundingMode) }
    }
}
