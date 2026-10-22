package com.example.steprange.domain.math

import com.example.steprange.data.model.CalculationStep
import com.example.steprange.data.model.RangeDirection
import com.example.steprange.data.model.StepRangeResult
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object StepRangeCalculator {

    val DEFAULT_MATH_CONTEXT = MathContext(80, RoundingMode.HALF_UP)

    fun isEndReachable(
        start: BigDecimal,
        increment: BigDecimal,
        end: BigDecimal
    ): Boolean {
        if (start.compareTo(end) == 0) return true
        if (increment.compareTo(BigDecimal.ZERO) == 0) return false

        val diff = end.subtract(start)
        if (diff.signum() != increment.signum()) return false

        val rem = diff.remainder(increment)
        return rem.compareTo(BigDecimal.ZERO) == 0
    }

    fun calculateSum(
        firstValue: BigDecimal,
        lastValue: BigDecimal,
        count: Int,
        mathContext: MathContext = DEFAULT_MATH_CONTEXT
    ): BigDecimal {
        if (count <= 0) return BigDecimal.ZERO
        val n = BigDecimal.valueOf(count.toLong())
        val firstPlusLast = firstValue.add(lastValue, mathContext)
        return n.multiply(firstPlusLast, mathContext).divide(BigDecimal.valueOf(2), mathContext)
    }

    fun calculateAverage(
        firstValue: BigDecimal,
        lastValue: BigDecimal,
        mathContext: MathContext = DEFAULT_MATH_CONTEXT
    ): BigDecimal {
        val sum = firstValue.add(lastValue, mathContext)
        return sum.divide(BigDecimal.valueOf(2), mathContext)
    }

    fun calculateValueAtIndex(
        start: BigDecimal,
        increment: BigDecimal,
        index: Int,
        mathContext: MathContext = DEFAULT_MATH_CONTEXT
    ): BigDecimal {
        require(index >= 1) { "Index must be at least 1." }
        require(index <= 100000) { "Index is too large. Maximum is 100,000." }
        val nMinusOne = BigDecimal.valueOf((index - 1).toLong())
        return start.add(nMinusOne.multiply(increment, mathContext), mathContext)
    }

    fun generateRange(
        start: BigDecimal,
        increment: BigDecimal,
        end: BigDecimal,
        includeEnd: Boolean = true,
        mathContext: MathContext = DEFAULT_MATH_CONTEXT
    ): Result<StepRangeResult> {
        val validation = StepRangeValidator.validateRange(start, increment, end, includeEnd)
        if (!validation.isValid) {
            return Result.failure(IllegalArgumentException(validation.errorMessage))
        }

        val calculatedAt = System.currentTimeMillis()

        if (start.compareTo(end) == 0) {
            return Result.success(
                StepRangeResult(
                    start = start,
                    increment = increment,
                    end = end,
                    values = listOf(start),
                    firstValue = start,
                    lastValue = start,
                    valueCount = 1,
                    minimum = start,
                    maximum = start,
                    sum = start,
                    average = start,
                    endReached = true,
                    includeEnd = includeEnd,
                    direction = RangeDirection.CONSTANT,
                    calculatedAt = calculatedAt
                )
            )
        }

        val isAscending = end > start
        val direction = if (isAscending) RangeDirection.INCREASING else RangeDirection.DECREASING
        val endReached = isEndReachable(start, increment, end)

        val values = mutableListOf<BigDecimal>()
        var index = 0

        while (true) {
            val currentValue = start.add(
                increment.multiply(BigDecimal.valueOf(index.toLong()), mathContext),
                mathContext
            )

            val comparison = currentValue.compareTo(end)
            if (isAscending && comparison > 0) break
            if (!isAscending && comparison < 0) break

            if (comparison == 0 && !includeEnd) {
                break
            }

            values.add(currentValue)
            index++

            if (values.size > StepRangeValidator.MAX_ALLOWED_VALUES) {
                return Result.failure(
                    IllegalStateException("This range would generate too many values. Increase the increment or reduce the range.")
                )
            }
        }

        if (values.isEmpty()) {
            values.add(start)
        }

        val firstValue = values.first()
        val lastValue = values.last()
        val count = values.size
        val minimum = values.minWithOrNull { a, b -> a.compareTo(b) } ?: firstValue
        val maximum = values.maxWithOrNull { a, b -> a.compareTo(b) } ?: lastValue
        val sum = calculateSum(firstValue, lastValue, count, mathContext)
        val average = calculateAverage(firstValue, lastValue, mathContext)

        return Result.success(
            StepRangeResult(
                start = start,
                increment = increment,
                end = end,
                values = values,
                firstValue = firstValue,
                lastValue = lastValue,
                valueCount = count,
                minimum = minimum,
                maximum = maximum,
                sum = sum,
                average = average,
                endReached = endReached,
                includeEnd = includeEnd,
                direction = direction,
                calculatedAt = calculatedAt
            )
        )
    }

    fun generateCalculationSteps(
        start: BigDecimal,
        increment: BigDecimal,
        values: List<BigDecimal>,
        maxSteps: Int = 10
    ): List<CalculationStep> {
        val steps = mutableListOf<CalculationStep>()
        val count = minOf(values.size, maxSteps)
        for (i in 1 until count) {
            steps.add(
                CalculationStep(
                    index = i + 1,
                    previousValue = values[i - 1],
                    increment = increment,
                    resultValue = values[i]
                )
            )
        }
        return steps
    }
}
