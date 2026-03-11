package com.antonkiselev.healthcompanion.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.antonkiselev.healthcompanion.data.HealthDatabase
import com.antonkiselev.healthcompanion.data.HealthRepository
import com.antonkiselev.healthcompanion.model.BloodPressureMeasurement
import com.antonkiselev.healthcompanion.model.MetricMode
import com.antonkiselev.healthcompanion.model.SugarMeasurement
import com.antonkiselev.healthcompanion.model.UserPreferences
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val RuLocale = Locale("ru", "RU")
private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", RuLocale)
private val DateTimeFormatterShort: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM, HH:mm", RuLocale)

val PresetScheduleSlots: List<LocalTime> = listOf(
    LocalTime.of(8, 0),
    LocalTime.of(10, 0),
    LocalTime.of(12, 0),
    LocalTime.of(14, 0),
    LocalTime.of(17, 0),
    LocalTime.of(19, 0),
    LocalTime.of(21, 0),
)

val TimezoneCatalog: List<TimezoneOption> = listOf(
    TimezoneOption("Системный часовой пояс", ZoneId.systemDefault().id),
    TimezoneOption("Калининград", "Europe/Kaliningrad"),
    TimezoneOption("Москва / Санкт-Петербург", "Europe/Moscow"),
    TimezoneOption("Самара", "Europe/Samara"),
    TimezoneOption("Екатеринбург", "Asia/Yekaterinburg"),
    TimezoneOption("Омск", "Asia/Omsk"),
    TimezoneOption("Новосибирск", "Asia/Novosibirsk"),
    TimezoneOption("Красноярск", "Asia/Krasnoyarsk"),
    TimezoneOption("Иркутск", "Asia/Irkutsk"),
    TimezoneOption("Якутск", "Asia/Yakutsk"),
    TimezoneOption("Владивосток", "Asia/Vladivostok"),
    TimezoneOption("Магадан", "Asia/Magadan"),
    TimezoneOption("Камчатка", "Asia/Kamchatka"),
)

enum class MeasurementKind {
    SUGAR,
    PRESSURE,
}

enum class ReportWindow(
    val id: String,
    val title: String,
    val days: Long?,
) {
    WEEK("week", "7 дней", 7),
    MONTH("month", "30 дней", 30),
    ALL("all", "Все время", null),
}

data class TimezoneOption(
    val label: String,
    val zoneId: String,
)

data class ChartPoint(
    val timestamp: Long,
    val value: Float,
)

data class PressureChartPoint(
    val timestamp: Long,
    val systolic: Float,
    val diastolic: Float,
    val pulse: Float,
)

data class TimelineEntryUi(
    val id: String,
    val kind: MeasurementKind,
    val title: String,
    val supportingText: String,
    val timeLabel: String,
    val timestamp: Long,
)

data class DashboardUiModel(
    val headline: String = "Соберите устойчивый ритм измерений",
    val statusTitle: String = "Готов к старту",
    val metricSummary: String = "Пока без замеров",
    val nextReminder: String = "Расписание не задано",
    val progressLabel: String = "Сегодня еще нет записей",
    val sugarAverage: String = "Нет данных",
    val pressureAverage: String = "Нет данных",
    val timezoneLabel: String = ZoneId.systemDefault().id,
    val todayCompletedCount: Int = 0,
    val todayPlannedCount: Int = 0,
    val completionFraction: Float = 0f,
    val scheduleCount: Int = 0,
    val latestEvent: String = "Последняя запись появится после первого замера.",
    val focusMessage: String = "Добавьте первый слот и первую запись.",
)

data class ReportUiModel(
    val window: ReportWindow = ReportWindow.WEEK,
    val sugarAverage: String = "Нет данных",
    val pressureAverage: String = "Нет данных",
    val sugarCount: Int = 0,
    val pressureCount: Int = 0,
    val sugarPoints: List<ChartPoint> = emptyList(),
    val pressurePoints: List<PressureChartPoint> = emptyList(),
    val recentEntries: List<TimelineEntryUi> = emptyList(),
    val insight: String = "Появится после первых записей.",
)

data class HealthUiState(
    val preferences: UserPreferences = UserPreferences(),
    val schedules: List<LocalTime> = emptyList(),
    val dashboard: DashboardUiModel = DashboardUiModel(),
    val timeline: List<TimelineEntryUi> = emptyList(),
    val reports: List<ReportUiModel> = ReportWindow.entries.map { ReportUiModel(window = it) },
)

class HealthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HealthRepository(HealthDatabase.getInstance(application).healthDao())

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val uiState: StateFlow<HealthUiState> = combine(
        repository.observePreferences(),
        repository.observeSchedule(),
        repository.observeSugarMeasurements(),
        repository.observeBloodPressureMeasurements(),
    ) { preferences, schedules, sugarMeasurements, pressureMeasurements ->
        val timeline = buildTimeline(
            sugarMeasurements = sugarMeasurements,
            pressureMeasurements = pressureMeasurements,
            timezoneId = preferences.timezoneId,
        )
        val reports = ReportWindow.entries.map { window ->
            buildReport(
                window = window,
                sugarMeasurements = sugarMeasurements,
                pressureMeasurements = pressureMeasurements,
                timezoneId = preferences.timezoneId,
            )
        }

        HealthUiState(
            preferences = preferences,
            schedules = schedules.sorted(),
            dashboard = buildDashboard(
                preferences = preferences,
                schedules = schedules,
                sugarMeasurements = sugarMeasurements,
                pressureMeasurements = pressureMeasurements,
                timeline = timeline,
            ),
            timeline = timeline,
            reports = reports,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HealthUiState(),
    )

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
    }

    fun setMetricMode(mode: MetricMode) {
        val current = uiState.value.preferences
        val updated = current.copy(
            metricMode = mode,
            simultaneous = if (mode == MetricMode.BOTH) current.simultaneous else true,
        )
        persistPreferences(updated)
    }

    fun setSimultaneous(enabled: Boolean) {
        persistPreferences(uiState.value.preferences.copy(simultaneous = enabled))
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        persistPreferences(uiState.value.preferences.copy(notificationsEnabled = enabled))
        showMessage(
            if (enabled) {
                "Напоминания включены"
            } else {
                "Напоминания выключены"
            },
        )
    }

    fun setTimezone(timezoneId: String) {
        persistPreferences(uiState.value.preferences.copy(timezoneId = timezoneId))
        showMessage("Часовой пояс обновлен")
    }

    fun toggleSchedule(time: LocalTime) {
        viewModelScope.launch {
            val current = uiState.value.schedules.toMutableList()
            val wasAdded = if (current.contains(time)) {
                current.remove(time)
                false
            } else {
                current.add(time)
                true
            }
            repository.replaceSchedule(current.sorted())
            showMessage(
                if (wasAdded) {
                    "Слот ${time.format(TimeFormatter)} добавлен"
                } else {
                    "Слот ${time.format(TimeFormatter)} убран"
                },
            )
        }
    }

    fun addSugar(value: Double, measuredAt: Long) {
        viewModelScope.launch {
            repository.addSugar(value = value, measuredAt = measuredAt)
            showMessage("Запись сахара сохранена")
        }
    }

    fun addBloodPressure(
        systolic: Int,
        diastolic: Int,
        pulse: Int,
        measuredAt: Long,
    ) {
        viewModelScope.launch {
            repository.addBloodPressure(
                systolic = systolic,
                diastolic = diastolic,
                pulse = pulse,
                measuredAt = measuredAt,
            )
            showMessage("Запись давления сохранена")
        }
    }

    fun showMessage(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    private fun persistPreferences(preferences: UserPreferences) {
        viewModelScope.launch {
            repository.updatePreferences(preferences)
        }
    }

    private fun buildDashboard(
        preferences: UserPreferences,
        schedules: List<LocalTime>,
        sugarMeasurements: List<SugarMeasurement>,
        pressureMeasurements: List<BloodPressureMeasurement>,
        timeline: List<TimelineEntryUi>,
    ): DashboardUiModel {
        val timezone = safeZoneId(preferences.timezoneId)
        val todayStart = ZonedDateTime.now(timezone).toLocalDate().atStartOfDay(timezone).toInstant()
        val todayStartMillis = todayStart.toEpochMilli()

        val sugarToday = sugarMeasurements.count { it.measuredAt >= todayStartMillis }
        val pressureToday = pressureMeasurements.count { it.measuredAt >= todayStartMillis }
        val actualEntriesToday = sugarToday + pressureToday
        val plannedEntriesToday = when (preferences.metricMode) {
            MetricMode.SUGAR, MetricMode.BLOOD_PRESSURE -> schedules.size
            MetricMode.BOTH -> if (preferences.simultaneous) schedules.size else schedules.size * 2
        }

        val weeklySugar = sugarMeasurements.filterSugarWithinDays(7)
        val weeklyPressure = pressureMeasurements.filterPressureWithinDays(7)

        val focusMessage = when {
            schedules.isEmpty() -> "Добавьте слоты, чтобы приложение стало ритмичным, а не реактивным."
            timeline.isEmpty() -> "Первый замер создаст графики и живую сводку на дашборде."
            !preferences.notificationsEnabled -> "Напоминания отключены, поэтому сегодня приложение работает только в ручном режиме."
            else -> "Следите за 7-дневным средним: это быстрее показывает тенденцию, чем одиночное значение."
        }

        val completionFraction = if (plannedEntriesToday == 0) {
            0f
        } else {
            (actualEntriesToday.toFloat() / plannedEntriesToday.toFloat()).coerceIn(0f, 1f)
        }

        val statusTitle = when {
            schedules.isEmpty() -> "Соберите ритм"
            timeline.isEmpty() -> "Готово к первому замеру"
            actualEntriesToday == 0 -> "Новый слот впереди"
            completionFraction >= 1f -> "День закрыт"
            completionFraction >= 0.5f -> "Вы в ритме"
            else -> "День уже начался"
        }

        val latestEvent = timeline.firstOrNull()?.let { entry ->
            "${entry.title} • ${entry.timeLabel}"
        } ?: "Последняя запись появится после первого замера."

        return DashboardUiModel(
            headline = "Health Compass",
            statusTitle = statusTitle,
            metricSummary = buildMetricSummary(preferences),
            nextReminder = buildNextReminderLabel(schedules, timezone),
            progressLabel = "Сегодня: $actualEntriesToday из $plannedEntriesToday ожидаемых записей",
            sugarAverage = weeklySugar.averageOrNull()?.let { String.format(RuLocale, "%.1f ммоль/л", it) } ?: "Нет данных",
            pressureAverage = weeklyPressure.averageTripleOrNull()?.let { triple ->
                "${triple.first}/${triple.second}, пульс ${triple.third}"
            } ?: "Нет данных",
            timezoneLabel = preferences.timezoneId,
            todayCompletedCount = actualEntriesToday,
            todayPlannedCount = plannedEntriesToday,
            completionFraction = completionFraction,
            scheduleCount = schedules.size,
            latestEvent = latestEvent,
            focusMessage = focusMessage,
        )
    }

    private fun buildTimeline(
        sugarMeasurements: List<SugarMeasurement>,
        pressureMeasurements: List<BloodPressureMeasurement>,
        timezoneId: String,
    ): List<TimelineEntryUi> {
        val timezone = safeZoneId(timezoneId)
        val sugarEntries = sugarMeasurements.map { measurement ->
            TimelineEntryUi(
                id = "sugar-${measurement.id}",
                kind = MeasurementKind.SUGAR,
                title = "Сахар ${String.format(RuLocale, "%.1f", measurement.value)} ммоль/л",
                supportingText = "Глюкоза",
                timeLabel = formatTimestamp(measurement.measuredAt, timezone),
                timestamp = measurement.measuredAt,
            )
        }
        val pressureEntries = pressureMeasurements.map { measurement ->
            TimelineEntryUi(
                id = "pressure-${measurement.id}",
                kind = MeasurementKind.PRESSURE,
                title = "${measurement.systolic}/${measurement.diastolic} мм рт. ст.",
                supportingText = "Пульс ${measurement.pulse} уд/мин",
                timeLabel = formatTimestamp(measurement.measuredAt, timezone),
                timestamp = measurement.measuredAt,
            )
        }

        return (sugarEntries + pressureEntries).sortedByDescending { it.timestamp }
    }

    private fun buildReport(
        window: ReportWindow,
        sugarMeasurements: List<SugarMeasurement>,
        pressureMeasurements: List<BloodPressureMeasurement>,
        timezoneId: String,
    ): ReportUiModel {
        val timezone = safeZoneId(timezoneId)
        val filteredSugar = sugarMeasurements.filterSugarWithinDays(window.days)
        val filteredPressure = pressureMeasurements.filterPressureWithinDays(window.days)
        val recentEntries = buildTimeline(filteredSugar, filteredPressure, timezoneId).take(8)

        val sugarAverageText = filteredSugar.averageOrNull()?.let {
            String.format(RuLocale, "%.1f ммоль/л", it)
        } ?: "Нет данных"

        val pressureAverageText = filteredPressure.averageTripleOrNull()?.let { triple ->
            "${triple.first}/${triple.second}, пульс ${triple.third}"
        } ?: "Нет данных"

        val insight = when {
            filteredSugar.isEmpty() && filteredPressure.isEmpty() -> "Для этого периода еще нет данных."
            filteredSugar.isNotEmpty() && filteredPressure.isEmpty() -> "Видно движение по сахару. Давление пока не фиксировалось."
            filteredSugar.isEmpty() && filteredPressure.isNotEmpty() -> "Видно движение по давлению. Добавьте сахар для полной картины."
            else -> "Оба показателя уже складываются в общую динамику."
        }

        return ReportUiModel(
            window = window,
            sugarAverage = sugarAverageText,
            pressureAverage = pressureAverageText,
            sugarCount = filteredSugar.size,
            pressureCount = filteredPressure.size,
            sugarPoints = filteredSugar
                .sortedBy { it.measuredAt }
                .map { ChartPoint(timestamp = it.measuredAt, value = it.value.toFloat()) },
            pressurePoints = filteredPressure
                .sortedBy { it.measuredAt }
                .map {
                    PressureChartPoint(
                        timestamp = it.measuredAt,
                        systolic = it.systolic.toFloat(),
                        diastolic = it.diastolic.toFloat(),
                        pulse = it.pulse.toFloat(),
                    )
                },
            recentEntries = recentEntries.map { entry ->
                entry.copy(timeLabel = formatTimestamp(entry.timestamp, timezone))
            },
            insight = insight,
        )
    }

    private fun buildMetricSummary(preferences: UserPreferences): String {
        return when (preferences.metricMode) {
            MetricMode.SUGAR -> "Режим: только сахар"
            MetricMode.BLOOD_PRESSURE -> "Режим: только давление"
            MetricMode.BOTH -> {
                if (preferences.simultaneous) {
                    "Режим: сахар и давление одним слотом"
                } else {
                    "Режим: сахар и давление раздельно"
                }
            }
        }
    }

    private fun buildNextReminderLabel(schedules: List<LocalTime>, timezone: ZoneId): String {
        if (schedules.isEmpty()) {
            return "Расписание не задано"
        }

        val now = ZonedDateTime.now(timezone)
        val next = schedules
            .map { time ->
                now.withHour(time.hour)
                    .withMinute(time.minute)
                    .withSecond(0)
                    .withNano(0)
                    .let { candidate ->
                        if (candidate.isAfter(now)) candidate else candidate.plusDays(1)
                    }
            }
            .minByOrNull { it.toInstant().toEpochMilli() }
            ?: return "Расписание не задано"

        val dayLabel = if (next.toLocalDate() == now.toLocalDate()) "Сегодня" else "Завтра"
        return "$dayLabel, ${next.format(TimeFormatter)}"
    }

    private fun formatTimestamp(timestamp: Long, timezone: ZoneId): String {
        return Instant.ofEpochMilli(timestamp).atZone(timezone).format(DateTimeFormatterShort)
    }

    private fun safeZoneId(timezoneId: String): ZoneId {
        return runCatching { ZoneId.of(timezoneId) }.getOrDefault(ZoneId.systemDefault())
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(HealthViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return HealthViewModel(application) as T
        }
    }
}

private fun List<SugarMeasurement>.averageOrNull(): Double? {
    return if (isEmpty()) null else map { it.value }.average()
}

private fun List<BloodPressureMeasurement>.averageTripleOrNull(): Triple<Int, Int, Int>? {
    if (isEmpty()) {
        return null
    }

    val averageSystolic = map { it.systolic }.average().toInt()
    val averageDiastolic = map { it.diastolic }.average().toInt()
    val averagePulse = map { it.pulse }.average().toInt()
    return Triple(averageSystolic, averageDiastolic, averagePulse)
}

private fun List<SugarMeasurement>.filterSugarWithinDays(days: Long?): List<SugarMeasurement> {
    if (days == null) {
        return this
    }
    val threshold = Instant.now().minusSeconds(days * 24 * 60 * 60).toEpochMilli()
    return filter { it.measuredAt >= threshold }
}

private fun List<BloodPressureMeasurement>.filterPressureWithinDays(days: Long?): List<BloodPressureMeasurement> {
    if (days == null) {
        return this
    }
    val threshold = Instant.now().minusSeconds(days * 24 * 60 * 60).toEpochMilli()
    return filter { it.measuredAt >= threshold }
}
