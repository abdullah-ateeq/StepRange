package com.example.steprange.domain.math

import java.math.BigDecimal
import java.math.BigInteger

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()

    val isValid: Boolean get() = this is Valid
    val errorMessage: String? get() = (this as? Invalid)?.message
}

object StepRangeValidator {

    const val MAX_ALLOWED_VALUES = 1000

    fun validateStartField(input: String): ValidationResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Enter a start value.")
        }
        val parseResult = StepRangeParser.parseDecimal(trimmed)
        return parseResult.fold(
            onSuccess = { ValidationResult.Valid },
            onFailure = { ValidationResult.Invalid(it.message ?: "Enter a valid decimal number.") }
        )
    }

    fun validateIncrementField(input: String): ValidationResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Enter an increment.")
        }
        val parseResult = StepRangeParser.parseDecimal(trimmed)
        return parseResult.fold(
            onSuccess = { parsed ->
                if (parsed.compareTo(BigDecimal.ZERO) == 0) {
                    ValidationResult.Invalid("Increment cannot be zero.")
                } else {
                    ValidationResult.Valid
                }
            },
            onFailure = { ValidationResult.Invalid(it.message ?: "Enter a valid decimal number.") }
        )
    }

    fun validateEndField(input: String): ValidationResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Enter an end value.")
        }
        val parseResult = StepRangeParser.parseDecimal(trimmed)
        return parseResult.fold(
            onSuccess = { ValidationResult.Valid },
            onFailure = { ValidationResult.Invalid(it.message ?: "Enter a valid decimal number.") }
        )
    }

    fun validateRange(
        start: BigDecimal,
        increment: BigDecimal,
        end: BigDecimal,
        includeEnd: Boolean = true
    ): ValidationResult {
        if (increment.compareTo(BigDecimal.ZERO) == 0) {
            return ValidationResult.Invalid("Increment cannot be zero.")
        }

        val comparison = end.compareTo(start)
        if (comparison > 0 && increment.signum() <= 0) {
            return ValidationResult.Invalid("Use a positive increment when the end value is greater than the start value.")
        }
        if (comparison < 0 && increment.signum() >= 0) {
            return ValidationResult.Invalid("Use a negative increment when the end value is less than the start value.")
        }

        // Check if the range would produce more than MAX_ALLOWED_VALUES
        val estimatedCount = estimateValueCount(start, increment, end, includeEnd)
        if (estimatedCount > BigInteger.valueOf(MAX_ALLOWED_VALUES.toLong())) {
            return ValidationResult.Invalid("This range would generate too many values. Increase the increment or reduce the range.")
        }

        return ValidationResult.Valid
    }

    fun estimateValueCount(
        start: BigDecimal,
        increment: BigDecimal,
        end: BigDecimal,
        includeEnd: Boolean = true
    ): BigInteger {
        if (start.compareTo(end) == 0) {
            return BigInteger.ONE
        }

        val distance = end.subtract(start).abs()
        val incAbs = increment.abs()
        val divAndRem = distance.divideAndRemainder(incAbs)
        val quotient = divAndRem[0].toBigInteger()
        val remainder = divAndRem[1]
        val isExact = remainder.compareTo(BigDecimal.ZERO) == 0

        return if (isExact) {
            if (includeEnd) {
                quotient + BigInteger.ONE
            } else {
                quotient
            }
        } else {
            quotient + BigInteger.ONE
        }
    }
}
