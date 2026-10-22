package com.example.steprange.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.steprange.data.model.StepRangeHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class HistoryRepository(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    private val _history = MutableStateFlow(loadHistory())
    val history: StateFlow<List<StepRangeHistory>> = _history.asStateFlow()

    private fun loadHistory(): List<StepRangeHistory> {
        val raw = prefs.getString(KEY_HISTORY_LIST, null) ?: return emptyList()
        return try {
            if (raw.isBlank()) return emptyList()
            raw.split(RECORD_SEP).mapNotNull { record ->
                val fields = record.split(FIELD_SEP)
                if (fields.size >= 6) {
                    StepRangeHistory(
                        id = fields[0],
                        start = fields[1],
                        increment = fields[2],
                        end = fields[3],
                        includeEnd = fields[4].toBooleanStrictOrNull() ?: true,
                        savedAt = fields[5].toLongOrNull() ?: System.currentTimeMillis()
                    )
                } else null
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun persist(list: List<StepRangeHistory>) {
        val serialized = list.joinToString(RECORD_SEP) { item ->
            listOf(
                item.id,
                item.start,
                item.increment,
                item.end,
                item.includeEnd.toString(),
                item.savedAt.toString()
            ).joinToString(FIELD_SEP)
        }
        prefs.edit().putString(KEY_HISTORY_LIST, serialized).apply()
        _history.value = list
    }

    fun addHistoryItem(
        start: String,
        increment: String,
        end: String,
        includeEnd: Boolean
    ): Boolean {
        val currentList = _history.value

        val firstItem = currentList.firstOrNull()
        if (firstItem != null &&
            firstItem.start == start &&
            firstItem.increment == increment &&
            firstItem.end == end &&
            firstItem.includeEnd == includeEnd
        ) {
            return false
        }

        val newItem = StepRangeHistory(
            id = UUID.randomUUID().toString(),
            start = start,
            increment = increment,
            end = end,
            includeEnd = includeEnd,
            savedAt = System.currentTimeMillis()
        )

        val updatedList = mutableListOf(newItem)
        updatedList.addAll(currentList)

        val trimmedList = if (updatedList.size > MAX_HISTORY_ITEMS) {
            updatedList.take(MAX_HISTORY_ITEMS)
        } else {
            updatedList
        }

        persist(trimmedList)
        return true
    }

    fun deleteItem(id: String) {
        val updated = _history.value.filterNot { it.id == id }
        persist(updated)
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY_LIST).apply()
        _history.value = emptyList()
    }

    companion object {
        private const val PREFS_NAME = "step_range_history"
        private const val KEY_HISTORY_LIST = "history_list"
        private const val RECORD_SEP = "\u001E"
        private const val FIELD_SEP = "\u001F"
        const val MAX_HISTORY_ITEMS = 10
    }
}
