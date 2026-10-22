package com.example.steprange.data.model

data class StepRangeHistory(
    val id: String,
    val start: String,
    val increment: String,
    val end: String,
    val includeEnd: Boolean,
    val savedAt: Long
)
