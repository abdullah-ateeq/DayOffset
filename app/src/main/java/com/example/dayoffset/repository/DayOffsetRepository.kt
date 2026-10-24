package com.example.dayoffset.repository

import com.example.dayoffset.data.OffsetPreferencesDataSource
import com.example.dayoffset.history.HistoryManager
import com.example.dayoffset.model.AppSettings
import com.example.dayoffset.model.DayOffsetHistory
import com.example.dayoffset.model.OffsetDirection
import com.example.dayoffset.model.OffsetUnit
import java.time.LocalDate

class DayOffsetRepository(
    private val dataSource: OffsetPreferencesDataSource,
    private val historyManager: HistoryManager = HistoryManager()
) {
    fun getSettings(): AppSettings = dataSource.loadSettings()

    fun updateSettings(settings: AppSettings) {
        dataSource.saveSettings(settings)
    }

    fun resetSettings(): AppSettings {
        return dataSource.resetSettingsToDefaults()
    }

    fun getInitialStartDate(today: LocalDate = LocalDate.now()): LocalDate {
        val settings = getSettings()
        return if (settings.restoreLastDate) {
            dataSource.loadLastSelectedDate() ?: today
        } else {
            today
        }
    }

    fun saveLastSelectedDate(date: LocalDate) {
        dataSource.saveLastSelectedDate(date)
    }

    fun getHistory(): List<DayOffsetHistory> {
        return dataSource.loadHistory()
    }

    fun saveCalculationToHistory(
        startDate: LocalDate,
        offset: Long,
        unit: OffsetUnit,
        direction: OffsetDirection,
        savedAt: Long = System.currentTimeMillis()
    ): List<DayOffsetHistory> {
        val current = dataSource.loadHistory()
        val updated = historyManager.addEntry(
            currentHistory = current,
            startDate = startDate,
            offset = offset,
            unit = unit,
            direction = direction,
            savedAt = savedAt
        )
        dataSource.saveHistory(updated)
        return updated
    }

    fun deleteHistoryEntry(id: String): List<DayOffsetHistory> {
        val current = dataSource.loadHistory()
        val updated = historyManager.removeEntry(current, id)
        dataSource.saveHistory(updated)
        return updated
    }

    fun clearHistory(): List<DayOffsetHistory> {
        dataSource.saveHistory(emptyList())
        return emptyList()
    }

    fun parseHistoryStartDate(entry: DayOffsetHistory): LocalDate {
        return historyManager.parseStartDate(entry)
    }
}
