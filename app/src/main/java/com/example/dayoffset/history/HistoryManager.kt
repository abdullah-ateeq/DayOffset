package com.example.dayoffset.history

import com.example.dayoffset.model.DayOffsetHistory
import com.example.dayoffset.model.OffsetDirection
import com.example.dayoffset.model.OffsetUnit
import java.time.LocalDate
import java.util.UUID

class HistoryManager(
    private val maxEntries: Int = MAX_HISTORY_ENTRIES
) {
    companion object {
        const val MAX_HISTORY_ENTRIES = 10
        private const val ENTRY_SEPARATOR = "\n"
        private const val FIELD_SEPARATOR = "|"
    }

    /**
     * Adds a calculation to history.
     * - Stores startDate as ISO-8601 (yyyy-MM-dd).
     * - Prevents consecutive duplicate entries with identical (startDate, offset, unit, direction).
     * - Caps history at [maxEntries] (10), dropping the oldest entry when full.
     */
    fun addEntry(
        currentHistory: List<DayOffsetHistory>,
        startDate: LocalDate,
        offset: Long,
        unit: OffsetUnit,
        direction: OffsetDirection,
        savedAt: Long = System.currentTimeMillis(),
        id: String = UUID.randomUUID().toString()
    ): List<DayOffsetHistory> {
        val isoStartDate = startDate.toString() // Always ISO-8601 yyyy-MM-dd
        val effectiveDirection = if (offset == 0L) OffsetDirection.ADD else direction
        val magnitude = kotlin.math.abs(offset)

        val latest = currentHistory.firstOrNull()
        if (latest != null &&
            latest.startDate == isoStartDate &&
            latest.offset == magnitude &&
            latest.unit == unit &&
            latest.direction == effectiveDirection
        ) {
            // Consecutive duplicate: do not create an extra duplicate entry
            return currentHistory
        }

        val newEntry = DayOffsetHistory(
            id = id,
            startDate = isoStartDate,
            offset = magnitude,
            unit = unit,
            direction = effectiveDirection,
            savedAt = savedAt
        )

        return (listOf(newEntry) + currentHistory).take(maxEntries)
    }

    fun removeEntry(
        currentHistory: List<DayOffsetHistory>,
        id: String
    ): List<DayOffsetHistory> {
        return currentHistory.filterNot { it.id == id }
    }

    fun parseStartDate(entry: DayOffsetHistory): LocalDate {
        return LocalDate.parse(entry.startDate)
    }

    fun serialize(history: List<DayOffsetHistory>): String {
        if (history.isEmpty()) return ""
        return history.take(maxEntries).joinToString(ENTRY_SEPARATOR) { item ->
            listOf(
                item.id.replace(FIELD_SEPARATOR, "_"),
                item.startDate,
                item.offset.toString(),
                item.unit.name,
                item.direction.name,
                item.savedAt.toString()
            ).joinToString(FIELD_SEPARATOR)
        }
    }

    fun deserialize(serialized: String?): List<DayOffsetHistory> {
        if (serialized.isNullOrBlank()) return emptyList()
        return serialized
            .split(ENTRY_SEPARATOR)
            .mapNotNull { line ->
                val parts = line.split(FIELD_SEPARATOR)
                if (parts.size != 6) return@mapNotNull null
                try {
                    val id = parts[0]
                    val startDate = LocalDate.parse(parts[1]).toString()
                    val offset = parts[2].toLong()
                    val unit = OffsetUnit.valueOf(parts[3])
                    val direction = OffsetDirection.valueOf(parts[4])
                    val savedAt = parts[5].toLong()
                    DayOffsetHistory(
                        id = id,
                        startDate = startDate,
                        offset = offset,
                        unit = unit,
                        direction = direction,
                        savedAt = savedAt
                    )
                } catch (_: Exception) {
                    null
                }
            }
            .take(maxEntries)
    }
}
