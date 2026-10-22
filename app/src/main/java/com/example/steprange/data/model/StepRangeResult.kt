package com.example.steprange.data.model

import java.math.BigDecimal

data class StepRangeResult(
    val start: BigDecimal,
    val increment: BigDecimal,
    val end: BigDecimal,
    val values: List<BigDecimal>,
    val firstValue: BigDecimal,
    val lastValue: BigDecimal,
    val valueCount: Int,
    val minimum: BigDecimal,
    val maximum: BigDecimal,
    val sum: BigDecimal,
    val average: BigDecimal,
    val endReached: Boolean,
    val includeEnd: Boolean,
    val direction: RangeDirection,
    val calculatedAt: Long
)
