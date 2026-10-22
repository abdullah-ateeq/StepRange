package com.example.steprange.domain.math

import java.math.BigDecimal
import java.math.BigInteger

object StepRangeParser {

    private const val MAX_INPUT_LENGTH = 500
    private const val MAX_DIRECT_INDEX = 100000
    private val DECIMAL_REGEX = Regex("^[+-]?(?:\\d+(?:\\.\\d+)?|\\.\\d+|\\d+\\.)$")
    private val INTEGER_REGEX = Regex("^\\+?\\d+$")

    fun parseDecimal(input: String): Result<BigDecimal> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Enter a valid decimal number."))
        }
        if (trimmed.length > MAX_INPUT_LENGTH) {
            return Result.failure(IllegalArgumentException("Number is too long. Maximum 500 characters."))
        }
        if (trimmed.contains(Regex("\\s"))) {
            return Result.failure(IllegalArgumentException("Enter a valid decimal number."))
        }
        if (trimmed.contains('e', ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Enter a valid decimal number."))
        }
        if (!DECIMAL_REGEX.matches(trimmed)) {
            return Result.failure(IllegalArgumentException("Enter a valid decimal number."))
        }

        return try {
            Result.success(BigDecimal(trimmed))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Enter a valid decimal number."))
        }
    }

    fun parsePositiveInteger(input: String): Result<Int> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Enter an index."))
        }
        if (trimmed.length > MAX_INPUT_LENGTH) {
            return Result.failure(IllegalArgumentException("Number is too long. Maximum 500 characters."))
        }
        if (trimmed.contains(Regex("\\s"))) {
            return Result.failure(IllegalArgumentException("Enter a valid positive integer."))
        }
        if (trimmed.contains('e', ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Enter a valid positive integer."))
        }
        if (!INTEGER_REGEX.matches(trimmed)) {
            return Result.failure(IllegalArgumentException("Enter a valid positive integer."))
        }

        return try {
            val sanitized = trimmed.removePrefix("+")
            val bigInt = BigInteger(sanitized)
            if (bigInt < BigInteger.ONE) {
                return Result.failure(IllegalArgumentException("Index must be at least 1."))
            }
            if (bigInt > BigInteger.valueOf(MAX_DIRECT_INDEX.toLong())) {
                return Result.failure(IllegalArgumentException("Index is too large. Maximum is 100,000."))
            }
            Result.success(bigInt.toInt())
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Enter a valid positive integer."))
        }
    }
}
