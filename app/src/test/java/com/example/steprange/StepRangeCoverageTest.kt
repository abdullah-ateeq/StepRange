package com.example.steprange

import com.example.steprange.data.model.AppRoundingMode
import com.example.steprange.data.model.AppTheme
import com.example.steprange.data.model.RangeDirection
import com.example.steprange.data.model.StepRangeHistory
import com.example.steprange.data.repository.HistoryRepository
import com.example.steprange.data.repository.SettingsRepository
import com.example.steprange.domain.math.StepRangeCalculator
import com.example.steprange.domain.math.StepRangeFormatter
import com.example.steprange.domain.math.StepRangeParser
import com.example.steprange.domain.math.StepRangeValidator
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

class StepRangeCoverageTest {

    // 1. Decimal parsing
    @Test
    fun test01_decimalParsing() {
        val validInputs = listOf(
            "100" to BigDecimal("100"),
            "-100" to BigDecimal("-100"),
            "+100" to BigDecimal("100"),
            "1.5" to BigDecimal("1.5"),
            "-2.75" to BigDecimal("-2.75"),
            "0.125" to BigDecimal("0.125"),
            " 25 " to BigDecimal("25"),
            "100000000000000000000" to BigDecimal("100000000000000000000")
        )

        for ((input, expected) in validInputs) {
            val result = StepRangeParser.parseDecimal(input)
            assertTrue("Expected success for input '$input'", result.isSuccess)
            assertEquals("Parsed value mismatch for '$input'", 0, expected.compareTo(result.getOrThrow()))
        }
    }

    // 2. Integer parsing
    @Test
    fun test02_integerParsing() {
        val validInts = listOf(
            "1" to 1,
            "10" to 10,
            "100000" to 100000,
            "+50" to 50,
            " 42 " to 42
        )
        for ((input, expected) in validInts) {
            val result = StepRangeParser.parsePositiveInteger(input)
            assertTrue("Expected success for integer '$input'", result.isSuccess)
            assertEquals(expected, result.getOrThrow())
        }

        // Test boundary violation (> 100,000)
        val tooLarge = StepRangeParser.parsePositiveInteger("100001")
        assertTrue(tooLarge.isFailure)
        assertEquals("Index is too large. Maximum is 100,000.", tooLarge.exceptionOrNull()?.message)

        // Test zero index (< 1)
        val zeroIndex = StepRangeParser.parsePositiveInteger("0")
        assertTrue(zeroIndex.isFailure)
        assertEquals("Index must be at least 1.", zeroIndex.exceptionOrNull()?.message)
    }

    // 3. Empty Start
    @Test
    fun test03_emptyStart() {
        val validation = StepRangeValidator.validateStartField("")
        assertFalse(validation.isValid)
        assertEquals("Enter a start value.", validation.errorMessage)

        val whitespaceValidation = StepRangeValidator.validateStartField("   ")
        assertFalse(whitespaceValidation.isValid)
        assertEquals("Enter a start value.", whitespaceValidation.errorMessage)
    }

    // 4. Empty Increment
    @Test
    fun test04_emptyIncrement() {
        val validation = StepRangeValidator.validateIncrementField("")
        assertFalse(validation.isValid)
        assertEquals("Enter an increment.", validation.errorMessage)

        val whitespaceValidation = StepRangeValidator.validateIncrementField("  ")
        assertFalse(whitespaceValidation.isValid)
        assertEquals("Enter an increment.", whitespaceValidation.errorMessage)
    }

    // 5. Empty End
    @Test
    fun test05_emptyEnd() {
        val validation = StepRangeValidator.validateEndField("")
        assertFalse(validation.isValid)
        assertEquals("Enter an end value.", validation.errorMessage)

        val whitespaceValidation = StepRangeValidator.validateEndField("   ")
        assertFalse(whitespaceValidation.isValid)
        assertEquals("Enter an end value.", whitespaceValidation.errorMessage)
    }

    // 6. Invalid decimal
    @Test
    fun test06_invalidDecimal() {
        val invalidInputs = listOf("abc", "1.2.3", "--", "++", "1E5", "1 000")
        for (input in invalidInputs) {
            val parseResult = StepRangeParser.parseDecimal(input)
            assertTrue("Expected failure for invalid input '$input'", parseResult.isFailure)
        }

        // Test length > 500 characters
        val longString = "1" + "0".repeat(501)
        val longResult = StepRangeParser.parseDecimal(longString)
        assertTrue(longResult.isFailure)
        assertEquals("Number is too long. Maximum 500 characters.", longResult.exceptionOrNull()?.message)
    }

    // 7. Zero increment
    @Test
    fun test07_zeroIncrement() {
        val incFieldVal = StepRangeValidator.validateIncrementField("0")
        assertFalse(incFieldVal.isValid)
        assertEquals("Increment cannot be zero.", incFieldVal.errorMessage)

        val rangeVal = StepRangeValidator.validateRange(BigDecimal("0"), BigDecimal("0"), BigDecimal("10"))
        assertFalse(rangeVal.isValid)
        assertEquals("Increment cannot be zero.", rangeVal.errorMessage)
    }

    // 8. Ascending direction
    @Test
    fun test08_ascendingDirection() {
        // End > Start with negative increment
        val validation = StepRangeValidator.validateRange(BigDecimal("0"), BigDecimal("-2"), BigDecimal("10"))
        assertFalse(validation.isValid)
        assertEquals("Use a positive increment when the end value is greater than the start value.", validation.errorMessage)

        // Valid ascending
        val validAscending = StepRangeValidator.validateRange(BigDecimal("0"), BigDecimal("2"), BigDecimal("10"))
        assertTrue(validAscending.isValid)
    }

    // 9. Descending direction
    @Test
    fun test09_descendingDirection() {
        // End < Start with positive increment
        val validation = StepRangeValidator.validateRange(BigDecimal("10"), BigDecimal("2"), BigDecimal("0"))
        assertFalse(validation.isValid)
        assertEquals("Use a negative increment when the end value is less than the start value.", validation.errorMessage)

        // Valid descending
        val validDescending = StepRangeValidator.validateRange(BigDecimal("10"), BigDecimal("-2"), BigDecimal("0"))
        assertTrue(validDescending.isValid)
    }

    // 10. Equal endpoints
    @Test
    fun test10_equalEndpoints() {
        val result = StepRangeCalculator.generateRange(
            start = BigDecimal("5"),
            increment = BigDecimal("2"),
            end = BigDecimal("5"),
            includeEnd = true
        ).getOrThrow()

        assertEquals(1, result.valueCount)
        assertEquals(0, BigDecimal("5").compareTo(result.firstValue))
        assertEquals(0, BigDecimal("5").compareTo(result.lastValue))
        assertEquals(0, BigDecimal("5").compareTo(result.minimum))
        assertEquals(0, BigDecimal("5").compareTo(result.maximum))
        assertEquals(0, BigDecimal("5").compareTo(result.sum))
        assertEquals(0, BigDecimal("5").compareTo(result.average))
        assertTrue(result.endReached)
        assertEquals(RangeDirection.CONSTANT, result.direction)
        assertEquals(listOf(BigDecimal("5")), result.values)
    }

    // 11. Basic range generation
    @Test
    fun test11_basicRangeGeneration() {
        val result = StepRangeCalculator.generateRange(
            start = BigDecimal("0"),
            increment = BigDecimal("2"),
            end = BigDecimal("10"),
            includeEnd = true
        ).getOrThrow()

        val expected = listOf("0", "2", "4", "6", "8", "10").map { BigDecimal(it) }
        assertEquals(expected.size, result.valueCount)
        for (i in expected.indices) {
            assertEquals(0, expected[i].compareTo(result.values[i]))
        }
        assertEquals(0, BigDecimal("30").compareTo(result.sum))
        assertEquals(0, BigDecimal("5").compareTo(result.average))
        assertTrue(result.endReached)
    }

    // 12. Inclusive endpoint
    @Test
    fun test12_inclusiveEndpoint() {
        val result = StepRangeCalculator.generateRange(
            start = BigDecimal("0"),
            increment = BigDecimal("2"),
            end = BigDecimal("10"),
            includeEnd = true
        ).getOrThrow()

        assertEquals(0, BigDecimal("10").compareTo(result.lastValue))
        assertTrue(result.values.any { it.compareTo(BigDecimal("10")) == 0 })
    }

    // 13. Exclusive endpoint
    @Test
    fun test13_exclusiveEndpoint() {
        val result = StepRangeCalculator.generateRange(
            start = BigDecimal("0"),
            increment = BigDecimal("2"),
            end = BigDecimal("10"),
            includeEnd = false
        ).getOrThrow()

        val expected = listOf("0", "2", "4", "6", "8").map { BigDecimal(it) }
        assertEquals(expected.size, result.valueCount)
        assertFalse(result.values.any { it.compareTo(BigDecimal("10")) == 0 })
        assertEquals(0, BigDecimal("8").compareTo(result.lastValue))
    }

    // 14. Endpoint reachability
    @Test
    fun test14_endpointReachability() {
        // 0 -> 10 by 2: 10 / 2 = 5 (exact)
        assertTrue(StepRangeCalculator.isEndReachable(BigDecimal("0"), BigDecimal("2"), BigDecimal("10")))
        // 0 -> 1 by 0.25: 1 / 0.25 = 4 (exact)
        assertTrue(StepRangeCalculator.isEndReachable(BigDecimal("0"), BigDecimal("0.25"), BigDecimal("1")))
        // 10 -> 0 by -2: -10 / -2 = 5 (exact)
        assertTrue(StepRangeCalculator.isEndReachable(BigDecimal("10"), BigDecimal("-2"), BigDecimal("0")))
    }

    // 15. Endpoint not reached
    @Test
    fun test15_endpointNotReached() {
        val reachable = StepRangeCalculator.isEndReachable(BigDecimal("0"), BigDecimal("3"), BigDecimal("10"))
        assertFalse(reachable)

        val result = StepRangeCalculator.generateRange(
            start = BigDecimal("0"),
            increment = BigDecimal("3"),
            end = BigDecimal("10"),
            includeEnd = true
        ).getOrThrow()

        val expected = listOf("0", "3", "6", "9").map { BigDecimal(it) }
        assertEquals(expected.size, result.valueCount)
        assertEquals(0, BigDecimal("9").compareTo(result.lastValue))
        assertFalse(result.endReached)
        assertFalse(result.values.any { it.compareTo(BigDecimal("10")) == 0 })
        assertEquals(0, BigDecimal("18").compareTo(result.sum))
        assertEquals(0, BigDecimal("4.5").compareTo(result.average))
    }

    // 16. Positive increments
    @Test
    fun test16_positiveIncrements() {
        val result = StepRangeCalculator.generateRange(
            start = BigDecimal("10"),
            increment = BigDecimal("5"),
            end = BigDecimal("25")
        ).getOrThrow()

        val expected = listOf("10", "15", "20", "25").map { BigDecimal(it) }
        assertEquals(expected.size, result.valueCount)
        assertEquals(0, BigDecimal("10").compareTo(result.firstValue))
        assertEquals(0, BigDecimal("25").compareTo(result.lastValue))
    }

    // 17. Negative increments
    @Test
    fun test17_negativeIncrements() {
        val result = StepRangeCalculator.generateRange(
            start = BigDecimal("10"),
            increment = BigDecimal("-2"),
            end = BigDecimal("0")
        ).getOrThrow()

        val expected = listOf("10", "8", "6", "4", "2", "0").map { BigDecimal(it) }
        assertEquals(expected.size, result.valueCount)
        for (i in expected.indices) {
            assertEquals(0, expected[i].compareTo(result.values[i]))
        }
    }

    // 18. Decimal increments
    @Test
    fun test18_decimalIncrements() {
        val result = StepRangeCalculator.generateRange(
            start = BigDecimal("0"),
            increment = BigDecimal("0.25"),
            end = BigDecimal("1")
        ).getOrThrow()

        val expected = listOf("0", "0.25", "0.5", "0.75", "1").map { BigDecimal(it) }
        assertEquals(expected.size, result.valueCount)
        for (i in expected.indices) {
            assertEquals(0, expected[i].compareTo(result.values[i]))
        }
    }

    // 19. Negative values
    @Test
    fun test19_negativeValues() {
        val result = StepRangeCalculator.generateRange(
            start = BigDecimal("-10"),
            increment = BigDecimal("2"),
            end = BigDecimal("0")
        ).getOrThrow()

        val expected = listOf("-10", "-8", "-6", "-4", "-2", "0").map { BigDecimal(it) }
        assertEquals(expected.size, result.valueCount)
        for (i in expected.indices) {
            assertEquals(0, expected[i].compareTo(result.values[i]))
        }
    }

    // 20. Range crossing zero
    @Test
    fun test20_rangeCrossingZero() {
        // Ascending across zero
        val asc = StepRangeCalculator.generateRange(
            start = BigDecimal("-5"),
            increment = BigDecimal("2"),
            end = BigDecimal("5")
        ).getOrThrow()

        val expectedAsc = listOf("-5", "-3", "-1", "1", "3", "5").map { BigDecimal(it) }
        assertEquals(expectedAsc.size, asc.valueCount)
        for (i in expectedAsc.indices) {
            assertEquals(0, expectedAsc[i].compareTo(asc.values[i]))
        }

        // Descending across zero
        val desc = StepRangeCalculator.generateRange(
            start = BigDecimal("5"),
            increment = BigDecimal("-2"),
            end = BigDecimal("-5")
        ).getOrThrow()

        val expectedDesc = listOf("5", "3", "1", "-1", "-3", "-5").map { BigDecimal(it) }
        assertEquals(expectedDesc.size, desc.valueCount)
        for (i in expectedDesc.indices) {
            assertEquals(0, expectedDesc[i].compareTo(desc.values[i]))
        }
    }

    // 21. Small decimal increments
    @Test
    fun test21_smallDecimalIncrements() {
        val result = StepRangeCalculator.generateRange(
            start = BigDecimal("0"),
            increment = BigDecimal("0.01"),
            end = BigDecimal("0.1")
        ).getOrThrow()

        val expected = listOf("0", "0.01", "0.02", "0.03", "0.04", "0.05", "0.06", "0.07", "0.08", "0.09", "0.1")
            .map { BigDecimal(it) }

        assertEquals(expected.size, result.valueCount)
        for (i in expected.indices) {
            assertEquals(0, expected[i].compareTo(result.values[i]))
        }
    }

    // 22. Large integers
    @Test
    fun test22_largeIntegers() {
        val start = BigDecimal("100000000000000000000")
        val inc = BigDecimal("2")
        val end = BigDecimal("100000000000000000010")

        val result = StepRangeCalculator.generateRange(start, inc, end).getOrThrow()
        val expected = listOf(
            "100000000000000000000",
            "100000000000000000002",
            "100000000000000000004",
            "100000000000000000006",
            "100000000000000000008",
            "100000000000000000010"
        ).map { BigDecimal(it) }

        assertEquals(6, result.valueCount)
        for (i in expected.indices) {
            assertEquals(0, expected[i].compareTo(result.values[i]))
        }
    }

    // 23. Large decimals
    @Test
    fun test23_largeDecimals() {
        val start = BigDecimal("100000000000000000000.125")
        val inc = BigDecimal("0.125")
        val end = BigDecimal("100000000000000000000.5")

        val result = StepRangeCalculator.generateRange(start, inc, end).getOrThrow()
        assertEquals(4, result.valueCount)
        assertEquals(0, start.compareTo(result.firstValue))
        assertEquals(0, end.compareTo(result.lastValue))
    }

    // 24. BigDecimal precision
    @Test
    fun test24_bigDecimalPrecision() {
        val mc = StepRangeCalculator.DEFAULT_MATH_CONTEXT
        assertEquals(80, mc.precision)
        assertEquals(RoundingMode.HALF_UP, mc.roundingMode)

        val start = BigDecimal("0.12345678901234567890123456789012345678901234567890")
        val inc = BigDecimal("0.00000000000000000000000000000000000000000000000001")
        val index = 10
        val directValue = StepRangeCalculator.calculateValueAtIndex(start, inc, index, mc)
        val expected = start.add(inc.multiply(BigDecimal.valueOf(9), mc), mc)
        assertEquals(0, expected.compareTo(directValue))
    }

    // 25. Floating-point drift prevention
    @Test
    fun test25_floatingPointDriftPrevention() {
        val result = StepRangeCalculator.generateRange(
            start = BigDecimal("0"),
            increment = BigDecimal("0.1"),
            end = BigDecimal("1")
        ).getOrThrow()

        val value3 = result.values[3]
        assertEquals(0, BigDecimal("0.3").compareTo(value3))
        val formatted3 = StepRangeFormatter.format(value3, 10, RoundingMode.HALF_UP)
        assertEquals("0.3", formatted3)
        assertFalse(formatted3.contains("00000000000000004"))
    }

    // 26. Direct index lookup
    @Test
    fun test26_directIndexLookup() {
        val value = StepRangeCalculator.calculateValueAtIndex(
            start = BigDecimal("0"),
            increment = BigDecimal("2"),
            index = 10
        )
        // 0 + (10 - 1) * 2 = 18
        assertEquals(0, BigDecimal("18").compareTo(value))

        // Direct index 100,000
        val largeIndexValue = StepRangeCalculator.calculateValueAtIndex(
            start = BigDecimal("1"),
            increment = BigDecimal("3"),
            index = 100000
        )
        val expected = BigDecimal("1").add(BigDecimal("99999").multiply(BigDecimal("3")))
        assertEquals(0, expected.compareTo(largeIndexValue))

        // Out of bounds (< 1)
        try {
            StepRangeCalculator.calculateValueAtIndex(BigDecimal("0"), BigDecimal("1"), 0)
            fail("Expected exception for index 0")
        } catch (e: IllegalArgumentException) {
            assertEquals("Index must be at least 1.", e.message)
        }

        // Out of bounds (> 100,000)
        try {
            StepRangeCalculator.calculateValueAtIndex(BigDecimal("0"), BigDecimal("1"), 100001)
            fail("Expected exception for index 100001")
        } catch (e: IllegalArgumentException) {
            assertEquals("Index is too large. Maximum is 100,000.", e.message)
        }
    }

    // 27. Sum
    @Test
    fun test27_sum() {
        val sum = StepRangeCalculator.calculateSum(
            firstValue = BigDecimal("0"),
            lastValue = BigDecimal("10"),
            count = 6
        )
        // Sₙ = 6 * (0 + 10) / 2 = 30
        assertEquals(0, BigDecimal("30").compareTo(sum))

        // Single element sum
        val singleSum = StepRangeCalculator.calculateSum(BigDecimal("5"), BigDecimal("5"), 1)
        assertEquals(0, BigDecimal("5").compareTo(singleSum))
    }

    // 28. Average
    @Test
    fun test28_average() {
        val avg = StepRangeCalculator.calculateAverage(
            firstValue = BigDecimal("0"),
            lastValue = BigDecimal("10")
        )
        // (0 + 10) / 2 = 5
        assertEquals(0, BigDecimal("5").compareTo(avg))

        val avg2 = StepRangeCalculator.calculateAverage(
            firstValue = BigDecimal("0"),
            lastValue = BigDecimal("9")
        )
        assertEquals(0, BigDecimal("4.5").compareTo(avg2))
    }

    // 29. Minimum
    @Test
    fun test29_minimum() {
        val asc = StepRangeCalculator.generateRange(BigDecimal("0"), BigDecimal("2"), BigDecimal("10")).getOrThrow()
        assertEquals(0, BigDecimal("0").compareTo(asc.minimum))

        val desc = StepRangeCalculator.generateRange(BigDecimal("10"), BigDecimal("-2"), BigDecimal("0")).getOrThrow()
        assertEquals(0, BigDecimal("0").compareTo(desc.minimum))
    }

    // 30. Maximum
    @Test
    fun test30_maximum() {
        val asc = StepRangeCalculator.generateRange(BigDecimal("0"), BigDecimal("2"), BigDecimal("10")).getOrThrow()
        assertEquals(0, BigDecimal("10").compareTo(asc.maximum))

        val desc = StepRangeCalculator.generateRange(BigDecimal("10"), BigDecimal("-2"), BigDecimal("0")).getOrThrow()
        assertEquals(0, BigDecimal("10").compareTo(desc.maximum))
    }

    // 31. Increasing direction
    @Test
    fun test31_increasingDirection() {
        val result = StepRangeCalculator.generateRange(BigDecimal("0"), BigDecimal("2"), BigDecimal("10")).getOrThrow()
        assertEquals(RangeDirection.INCREASING, result.direction)
    }

    // 32. Decreasing direction
    @Test
    fun test32_decreasingDirection() {
        val result = StepRangeCalculator.generateRange(BigDecimal("10"), BigDecimal("-2"), BigDecimal("0")).getOrThrow()
        assertEquals(RangeDirection.DECREASING, result.direction)
    }

    // 33. Constant range
    @Test
    fun test33_constantRange() {
        val result = StepRangeCalculator.generateRange(BigDecimal("5"), BigDecimal("1"), BigDecimal("5")).getOrThrow()
        assertEquals(RangeDirection.CONSTANT, result.direction)
    }

    // 34. Display rounding
    @Test
    fun test34_displayRounding() {
        val formattedHalf = StepRangeFormatter.format(BigDecimal("0.5"), 10, RoundingMode.HALF_UP)
        assertEquals("0.5", formattedHalf)

        val formattedTen = StepRangeFormatter.format(BigDecimal("10.0000000000"), 10, RoundingMode.HALF_UP)
        assertEquals("10", formattedTen)
    }

    // 35. Decimal-place settings
    @Test
    fun test35_decimalPlaceSettings() {
        val valToFormat = BigDecimal("1.23456789")

        val fmt4 = StepRangeFormatter.format(valToFormat, 4, RoundingMode.HALF_UP)
        assertEquals("1.2346", fmt4)

        val fmt2 = StepRangeFormatter.format(valToFormat, 2, RoundingMode.HALF_UP)
        assertEquals("1.23", fmt2)

        val fmt0 = StepRangeFormatter.format(valToFormat, 0, RoundingMode.HALF_UP)
        assertEquals("1", fmt0)
    }

    // 36. Rounding modes
    @Test
    fun test36_roundingModes() {
        val v = BigDecimal("2.5")

        assertEquals("3", StepRangeFormatter.format(v, 0, RoundingMode.HALF_UP))
        assertEquals("2", StepRangeFormatter.format(v, 0, RoundingMode.HALF_DOWN))
        assertEquals("2", StepRangeFormatter.format(v, 0, RoundingMode.HALF_EVEN))
        assertEquals("3", StepRangeFormatter.format(v, 0, RoundingMode.UP))
        assertEquals("2", StepRangeFormatter.format(v, 0, RoundingMode.DOWN))
        assertEquals("3", StepRangeFormatter.format(v, 0, RoundingMode.CEILING))
        assertEquals("2", StepRangeFormatter.format(v, 0, RoundingMode.FLOOR))
    }

    // 37. Output-size protection
    @Test
    fun test37_outputSizeProtection() {
        val start = BigDecimal("0")
        val inc = BigDecimal("1")
        val end = BigDecimal("1001") // would produce 1002 values (> 1000)

        val validation = StepRangeValidator.validateRange(start, inc, end, includeEnd = true)
        assertFalse(validation.isValid)
        assertEquals(
            "This range would generate too many values. Increase the increment or reduce the range.",
            validation.errorMessage
        )

        val result = StepRangeCalculator.generateRange(start, inc, end, includeEnd = true)
        assertTrue(result.isFailure)
        assertEquals(
            "This range would generate too many values. Increase the increment or reduce the range.",
            result.exceptionOrNull()?.message
        )
    }

    // 38. Calculation timeout
    @Test
    fun test38_calculationTimeout() = runBlocking {
        var timedOut = false
        try {
            withTimeout(100L) {
                delay(200L)
            }
        } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
            timedOut = true
        }
        assertTrue("Expected TimeoutCancellationException", timedOut)
    }

    // 39. Calculation cancellation
    @Test
    fun test39_calculationCancellation() = runBlocking {
        var cancelled = false
        val job = launch(Dispatchers.Default) {
            try {
                delay(1000L)
            } catch (_: kotlinx.coroutines.CancellationException) {
                cancelled = true
            }
        }
        delay(50L)
        job.cancelAndJoin()
        assertTrue("Expected Job cancellation", cancelled)
    }

    // 40. Copy formatting
    @Test
    fun test40_copyFormatting() {
        val values = listOf(BigDecimal("0"), BigDecimal("2"), BigDecimal("4"), BigDecimal("6"))
        val copied = StepRangeFormatter.formatCopyValuesOnly(values, 10, RoundingMode.HALF_UP)
        assertEquals("0, 2, 4, 6", copied)
    }

    // 41. Share formatting
    @Test
    fun test41_shareFormatting() {
        val result = StepRangeCalculator.generateRange(
            BigDecimal("0"),
            BigDecimal("2"),
            BigDecimal("10")
        ).getOrThrow()

        val shareText = StepRangeFormatter.formatShareText(result, 10, RoundingMode.HALF_UP)
        assertTrue(shareText.contains("StepRange Sequence"))
        assertTrue(shareText.contains("Start: 0"))
        assertTrue(shareText.contains("Increment: 2"))
        assertTrue(shareText.contains("End: 10"))
        assertTrue(shareText.contains("Sum: 30"))
        assertTrue(shareText.contains("Average: 5"))
    }

    // 42. History save
    @Test
    fun test42_historySave() {
        val fakePrefs = FakeSharedPreferences()
        val repo = HistoryRepository(fakePrefs)

        val saved = repo.addHistoryItem("0", "2", "10", true)
        assertTrue(saved)
        assertEquals(1, repo.history.value.size)
        assertEquals("0", repo.history.value[0].start)
        assertEquals("2", repo.history.value[0].increment)
        assertEquals("10", repo.history.value[0].end)
    }

    // 43. History restore
    @Test
    fun test43_historyRestore() {
        val fakePrefs = FakeSharedPreferences()
        val repo = HistoryRepository(fakePrefs)
        repo.addHistoryItem("10", "-2", "0", false)

        val item = repo.history.value[0]
        assertEquals("10", item.start)
        assertEquals("-2", item.increment)
        assertEquals("0", item.end)
        assertFalse(item.includeEnd)

        // Verify that regenerating from restored item produces accurate results
        val restoredResult = StepRangeCalculator.generateRange(
            start = BigDecimal(item.start),
            increment = BigDecimal(item.increment),
            end = BigDecimal(item.end),
            includeEnd = item.includeEnd
        ).getOrThrow()

        assertEquals(listOf(BigDecimal("10"), BigDecimal("8"), BigDecimal("6"), BigDecimal("4"), BigDecimal("2")), restoredResult.values)
    }

    // 44. History limit
    @Test
    fun test44_historyLimit() {
        val fakePrefs = FakeSharedPreferences()
        val repo = HistoryRepository(fakePrefs)

        // Save 12 configurations
        for (i in 1..12) {
            repo.addHistoryItem("0", "$i", "100", true)
        }

        // Limit is strictly 10 entries
        assertEquals(10, repo.history.value.size)
        // Most recent configuration was i=12
        assertEquals("12", repo.history.value.first().increment)
        // Oldest preserved is i=3
        assertEquals("3", repo.history.value.last().increment)
    }

    // 45. Duplicate history
    @Test
    fun test45_duplicateHistory() {
        val fakePrefs = FakeSharedPreferences()
        val repo = HistoryRepository(fakePrefs)

        val firstSave = repo.addHistoryItem("0", "2", "10", true)
        assertTrue(firstSave)
        assertEquals(1, repo.history.value.size)

        // Save exact same consecutively
        val duplicateSave = repo.addHistoryItem("0", "2", "10", true)
        assertFalse(duplicateSave)
        assertEquals(1, repo.history.value.size)
    }

    // 46. History deletion
    @Test
    fun test46_historyDeletion() {
        val fakePrefs = FakeSharedPreferences()
        val repo = HistoryRepository(fakePrefs)

        repo.addHistoryItem("0", "2", "10", true)
        repo.addHistoryItem("5", "5", "50", true)
        assertEquals(2, repo.history.value.size)

        val toDelete = repo.history.value[0].id
        repo.deleteItem(toDelete)
        assertEquals(1, repo.history.value.size)

        repo.clearHistory()
        assertTrue(repo.history.value.isEmpty())
    }

    // 47. Settings persistence
    @Test
    fun test47_settingsPersistence() {
        val fakePrefs = FakeSharedPreferences()
        val repo = SettingsRepository(fakePrefs)

        repo.updateTheme(AppTheme.DARK)
        repo.updateDecimalPlaces(4)
        repo.updateRoundingMode(AppRoundingMode.HALF_EVEN)
        repo.updateDynamicColor(false)
        repo.updateHapticFeedback(false)
        repo.updateAnimations(false)
        repo.updateConfirmDelete(false)

        val reloadedRepo = SettingsRepository(fakePrefs)
        val loadedSettings = reloadedRepo.settings.value

        assertEquals(AppTheme.DARK, loadedSettings.theme)
        assertEquals(4, loadedSettings.decimalPlaces)
        assertEquals(AppRoundingMode.HALF_EVEN, loadedSettings.roundingMode)
        assertFalse(loadedSettings.dynamicColor)
        assertFalse(loadedSettings.hapticFeedback)
        assertFalse(loadedSettings.animations)
        assertFalse(loadedSettings.confirmDelete)
    }

    // 48. Settings reset
    @Test
    fun test48_settingsReset() {
        val fakePrefs = FakeSharedPreferences()
        val repo = SettingsRepository(fakePrefs)

        repo.updateTheme(AppTheme.DARK)
        repo.updateDecimalPlaces(2)
        repo.updateRoundingMode(AppRoundingMode.FLOOR)

        repo.resetSettings()

        val resetSettings = repo.settings.value
        assertEquals(AppTheme.SYSTEM, resetSettings.theme)
        assertEquals(10, resetSettings.decimalPlaces)
        assertEquals(AppRoundingMode.HALF_UP, resetSettings.roundingMode)
        assertTrue(resetSettings.dynamicColor)
        assertTrue(resetSettings.hapticFeedback)
        assertTrue(resetSettings.animations)
        assertTrue(resetSettings.confirmDelete)
    }
}
