package com.example.steprange.data.model

import java.math.BigDecimal

data class CalculationStep(
    val index: Int,
    val previousValue: BigDecimal,
    val increment: BigDecimal,
    val resultValue: BigDecimal
)
