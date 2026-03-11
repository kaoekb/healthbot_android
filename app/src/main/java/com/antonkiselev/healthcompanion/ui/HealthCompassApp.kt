package com.antonkiselev.healthcompanion.ui

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.antonkiselev.healthcompanion.R
import com.antonkiselev.healthcompanion.model.MetricMode
import com.antonkiselev.healthcompanion.model.UserPreferences
import com.antonkiselev.healthcompanion.notifications.ReminderScheduler
import com.antonkiselev.healthcompanion.reports.ReportExporter
import com.antonkiselev.healthcompanion.ui.theme.Blush
import com.antonkiselev.healthcompanion.ui.theme.Clay
import com.antonkiselev.healthcompanion.ui.theme.ClayDark
import com.antonkiselev.healthcompanion.ui.theme.Danger
import com.antonkiselev.healthcompanion.ui.theme.LimeSoft
import com.antonkiselev.healthcompanion.ui.theme.Mist
import com.antonkiselev.healthcompanion.ui.theme.Sand
import com.antonkiselev.healthcompanion.ui.theme.Teal
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

private val UiLocale = Locale("ru", "RU")
private val UiTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", UiLocale)
private val UiDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM, HH:mm", UiLocale)
private val PebbleShape = GenericShape { size, _ ->
    moveTo(size.width * 0.12f, size.height * 0.3f)
    cubicTo(
        size.width * 0.25f,
        size.height * 0.02f,
        size.width * 0.85f,
        size.height * 0.04f,
        size.width * 0.92f,
        size.height * 0.32f,
    )
    cubicTo(
        size.width,
        size.height * 0.64f,
        size.width * 0.86f,
        size.height,
        size.width * 0.52f,
        size.height * 0.96f,
    )
    cubicTo(
        size.width * 0.14f,
        size.height * 0.9f,
        size.width * 0.02f,
        size.height * 0.54f,
        size.width * 0.12f,
        size.height * 0.3f,
    )
    close()
}

private enum class RootSection(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    DASHBOARD("Пульс дня", Icons.Rounded.Home),
    JOURNAL("Журнал", Icons.Rounded.MonitorHeart),
    REPORTS("Отчеты", Icons.Rounded.AutoGraph),
    SETTINGS("Настройка", Icons.Rounded.Tune),
}

private enum class JournalFilter(val title: String) {
    ALL("Все"),
    SUGAR("Сахар"),
    PRESSURE("Давление"),
}

private enum class EntrySheetTab(val title: String) {
    SUGAR("Сахар"),
    PRESSURE("Давление"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthCompassApp(
    uiState: HealthUiState,
    viewModel: HealthViewModel,
) {
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var selectedSection by rememberSaveable { mutableStateOf(RootSection.DASHBOARD) }
    var showEntrySheet by rememberSaveable { mutableStateOf(false) }
    val sectionSupportText = when (selectedSection) {
        RootSection.DASHBOARD -> uiState.dashboard.nextReminder
        RootSection.JOURNAL -> "${uiState.timeline.size} записей в журнале"
        RootSection.REPORTS -> "Графики, средние и PDF"
        RootSection.SETTINGS -> uiState.preferences.timezoneId
    }

    val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) != PackageManager.PERMISSION_GRANTED

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.showMessage(
            if (granted) {
                "Разрешение на уведомления выдано"
            } else {
                "Без разрешения системные уведомления не будут показаны"
            },
        )
    }

    LaunchedEffect(uiState.preferences, uiState.schedules) {
        ReminderScheduler.sync(context, uiState.schedules, uiState.preferences)
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Sand.copy(alpha = 0.9f),
                        Blush,
                        LimeSoft.copy(alpha = 0.65f),
                    ),
                ),
            ),
    ) {
        AtmosphereBackdrop()
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 10.dp)
                        .heightIn(min = 92.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Bottom),
                ) {
                    Text(
                        text = "Health Compass",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    )
                    Text(
                        text = selectedSection.title,
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Text(
                        text = sectionSupportText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                ) {
                    RootSection.entries.forEach { section ->
                        NavigationBarItem(
                            selected = section == selectedSection,
                            onClick = { selectedSection = section },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Clay,
                                selectedTextColor = ClayDark,
                                indicatorColor = Sand,
                            ),
                            icon = { Icon(section.icon, contentDescription = section.title) },
                            label = { Text(section.title) },
                        )
                    }
                }
            },
            floatingActionButton = {
                if (selectedSection != RootSection.SETTINGS) {
                    ElevatedButton(
                        onClick = { showEntrySheet = true },
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.size(10.dp))
                        Text("Новая запись")
                    }
                }
            },
        ) { innerPadding ->
            AnimatedContent(
                targetState = selectedSection,
                modifier = Modifier.padding(innerPadding),
                transitionSpec = {
                    (fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                        slideInHorizontally(animationSpec = tween(280)) { fullWidth -> fullWidth / 8 })
                        .togetherWith(
                            fadeOut(animationSpec = tween(180)) +
                                slideOutHorizontally(animationSpec = tween(180)) { fullWidth -> -fullWidth / 10 },
                        )
                },
                label = "root-sections",
            ) { section ->
                when (section) {
                    RootSection.DASHBOARD -> DashboardScreen(
                        modifier = Modifier,
                        dashboard = uiState.dashboard,
                        schedules = uiState.schedules,
                        showPermissionCard = needsNotificationPermission && uiState.preferences.notificationsEnabled,
                        onRequestPermission = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                        onAddMeasurement = { showEntrySheet = true },
                        onOpenReports = { selectedSection = RootSection.REPORTS },
                        onOpenSettings = { selectedSection = RootSection.SETTINGS },
                    )

                    RootSection.JOURNAL -> JournalScreen(
                        modifier = Modifier,
                        entries = uiState.timeline,
                        onAddMeasurement = { showEntrySheet = true },
                    )

                    RootSection.REPORTS -> ReportsScreen(
                        modifier = Modifier,
                        reports = uiState.reports,
                        onShareReport = { report ->
                            runCatching {
                                ReportExporter.shareReport(context, report, report.recentEntries)
                            }.onSuccess {
                                viewModel.showMessage("PDF-сводка подготовлена")
                            }.onFailure {
                                viewModel.showMessage("Не удалось подготовить PDF")
                            }
                        },
                    )

                    RootSection.SETTINGS -> SettingsScreen(
                        modifier = Modifier,
                        preferences = uiState.preferences,
                        schedules = uiState.schedules,
                        onMetricModeChanged = viewModel::setMetricMode,
                        onSimultaneousChanged = viewModel::setSimultaneous,
                        onNotificationsChanged = viewModel::setNotificationsEnabled,
                        onTimezoneChanged = viewModel::setTimezone,
                        onToggleSchedule = viewModel::toggleSchedule,
                    )
                }
            }
        }

        if (showEntrySheet) {
            MeasurementEntrySheet(
                preferences = uiState.preferences,
                onDismiss = { showEntrySheet = false },
                onSaveSugar = { value, measuredAt ->
                    viewModel.addSugar(value, measuredAt)
                    showEntrySheet = false
                },
                onSavePressure = { systolic, diastolic, pulse, measuredAt ->
                    viewModel.addBloodPressure(systolic, diastolic, pulse, measuredAt)
                    showEntrySheet = false
                },
                onMessage = viewModel::showMessage,
            )
        }
    }
}

@Composable
private fun DashboardScreen(
    modifier: Modifier = Modifier,
    dashboard: DashboardUiModel,
    schedules: List<LocalTime>,
    showPermissionCard: Boolean,
    onRequestPermission: () -> Unit,
    onAddMeasurement: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HeroCard(
                dashboard = dashboard,
                onAddMeasurement = onAddMeasurement,
            )
        }

        if (showPermissionCard) {
            item {
                PermissionCard(onRequestPermission = onRequestPermission)
            }
        }

        item {
            DailyRhythmCard(dashboard = dashboard)
        }

        item {
            QuickActionStrip(
                onAddMeasurement = onAddMeasurement,
                onOpenReports = onOpenReports,
                onOpenSettings = onOpenSettings,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Сахар, 7 дней",
                    value = dashboard.sugarAverage,
                    icon = Icons.Rounded.Favorite,
                    tint = Clay,
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Давление, 7 дней",
                    value = dashboard.pressureAverage,
                    icon = Icons.Rounded.MonitorHeart,
                    tint = Teal,
                )
            }
        }

        item {
            SoftCard {
                SectionHeader(
                    title = "Ритм напоминаний",
                    subtitle = dashboard.nextReminder,
                )
                if (schedules.isEmpty()) {
                    EmptyStateBlock(
                        title = "Пока нет слотов",
                        body = "Добавьте время на экране настройки, чтобы приложение стало работать проактивно.",
                    )
                } else {
                    ScheduleChipRow(schedule = schedules)
                }
            }
        }

        item {
            SoftCard {
                SectionHeader(
                    title = "Фокус дня",
                    subtitle = dashboard.progressLabel,
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoBadge(text = dashboard.timezoneLabel, icon = Icons.Rounded.Schedule)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = dashboard.focusMessage,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JournalScreen(
    modifier: Modifier = Modifier,
    entries: List<TimelineEntryUi>,
    onAddMeasurement: () -> Unit,
) {
    var selectedFilter by rememberSaveable { mutableStateOf(JournalFilter.ALL) }
    val filteredEntries = remember(entries, selectedFilter) {
        when (selectedFilter) {
            JournalFilter.ALL -> entries
            JournalFilter.SUGAR -> entries.filter { it.kind == MeasurementKind.SUGAR }
            JournalFilter.PRESSURE -> entries.filter { it.kind == MeasurementKind.PRESSURE }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SoftCard {
                SectionHeader(
                    title = "Живой журнал",
                    subtitle = "${filteredEntries.size} записей в текущем фильтре",
                )
                Text(
                    text = "Здесь удобнее всего замечать ритм, пропуски и то, как меняется состояние в течение недели.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                JournalFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.title) },
                    )
                }
            }
        }

        if (filteredEntries.isEmpty()) {
            item {
                SoftCard {
                    EmptyStateBlock(
                        title = "Журнал пока пуст",
                        body = "Первая запись сразу появится здесь и станет основой для графиков и отчета.",
                        actionLabel = "Добавить замер",
                        onAction = onAddMeasurement,
                    )
                }
            }
        } else {
            items(filteredEntries, key = { it.id }) { entry ->
                EntryCard(entry = entry)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReportsScreen(
    modifier: Modifier = Modifier,
    reports: List<ReportUiModel>,
    onShareReport: (ReportUiModel) -> Unit,
) {
    var selectedWindow by rememberSaveable { mutableStateOf(ReportWindow.WEEK) }
    val selectedReport = reports.firstOrNull { it.window == selectedWindow }
        ?: reports.firstOrNull()
        ?: return

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ReportWindow.entries.forEach { window ->
                    FilterChip(
                        selected = selectedWindow == window,
                        onClick = { selectedWindow = window },
                        label = { Text(window.title) },
                    )
                }
            }
        }

        item {
            SoftCard {
                SectionHeader(
                    title = "Сводка периода",
                    subtitle = selectedReport.insight,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Средний сахар",
                        value = selectedReport.sugarAverage,
                        icon = Icons.Rounded.Favorite,
                        tint = Clay,
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Среднее давление",
                        value = selectedReport.pressureAverage,
                        icon = Icons.Rounded.MonitorHeart,
                        tint = Teal,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    InfoBadge(text = "Сахар: ${selectedReport.sugarCount}", icon = Icons.Rounded.Favorite)
                    InfoBadge(text = "Давление: ${selectedReport.pressureCount}", icon = Icons.Rounded.MonitorHeart)
                }
            }
        }

        item {
            SoftCard {
                SectionHeader(
                    title = "Сахар",
                    subtitle = "Тренд по значениям",
                )
                ChartContainer {
                    SugarChart(points = selectedReport.sugarPoints)
                }
            }
        }

        item {
            SoftCard {
                SectionHeader(
                    title = "Давление и пульс",
                    subtitle = "Три линии в одном ритме",
                )
                ChartContainer {
                    PressureChart(points = selectedReport.pressurePoints)
                }
            }
        }

        item {
            SoftCard {
                SectionHeader(
                    title = "PDF-выгрузка",
                    subtitle = "Краткая сводка и последние записи",
                )
                ElevatedButton(
                    onClick = { onShareReport(selectedReport) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Сформировать PDF")
                }
            }
        }

        if (selectedReport.recentEntries.isNotEmpty()) {
            item {
                Text(
                    text = "Последние записи периода",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            items(selectedReport.recentEntries, key = { it.id }) { entry ->
                EntryCard(entry = entry)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SettingsScreen(
    modifier: Modifier = Modifier,
    preferences: UserPreferences,
    schedules: List<LocalTime>,
    onMetricModeChanged: (MetricMode) -> Unit,
    onSimultaneousChanged: (Boolean) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onTimezoneChanged: (String) -> Unit,
    onToggleSchedule: (LocalTime) -> Unit,
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val timezoneLabel = TimezoneCatalog.firstOrNull { it.zoneId == preferences.timezoneId }?.label
        ?: preferences.timezoneId

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SettingsStatusCard(
                preferences = preferences,
                scheduleCount = schedules.size,
            )
        }

        item {
            SoftCard {
                SectionHeader(
                    title = "Что измеряем",
                    subtitle = "Режим влияет на тексты напоминаний и дашборд",
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricMode.values().forEach { mode: MetricMode ->
                        MetricModeCard(
                            mode = mode,
                            selected = preferences.metricMode == mode,
                            onClick = { onMetricModeChanged(mode) },
                        )
                    }
                }

                if (preferences.metricMode == MetricMode.BOTH) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(14.dp))
                    SettingToggleRow(
                        title = "Один слот для двух показателей",
                        subtitle = "Когда включено, один слот означает одну комплексную сессию замера.",
                        checked = preferences.simultaneous,
                        onCheckedChange = onSimultaneousChanged,
                    )
                }
            }
        }

        item {
            SoftCard {
                SectionHeader(
                    title = "Напоминания",
                    subtitle = "Расписание работает через локальные уведомления",
                )
                SettingToggleRow(
                    title = "Уведомления",
                    subtitle = "Если выключить, расписание останется, но сигналов не будет.",
                    checked = preferences.notificationsEnabled,
                    onCheckedChange = onNotificationsChanged,
                )
                Spacer(modifier = Modifier.height(16.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PresetScheduleSlots.forEach { time: LocalTime ->
                        val isSelected = schedules.contains(time)
                        val chipIcon: (@Composable () -> Unit)? = if (isSelected) {
                            { Icon(Icons.Rounded.NotificationsActive, contentDescription = null) }
                        } else {
                            null
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { onToggleSchedule(time) },
                            label = { Text(time.format(UiTimeFormatter)) },
                            leadingIcon = chipIcon,
                        )
                    }

                    schedules.filterNot { it in PresetScheduleSlots }.forEach { time: LocalTime ->
                        FilterChip(
                            selected = true,
                            onClick = { onToggleSchedule(time) },
                            label = { Text(time.format(UiTimeFormatter)) },
                            leadingIcon = { Icon(Icons.Rounded.Schedule, contentDescription = null) },
                        )
                    }

                    AssistChip(
                        onClick = {
                            showTimePicker(context, LocalTime.now()) { picked: LocalTime ->
                                onToggleSchedule(picked)
                            }
                        },
                        label = { Text("+ Время") },
                        leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    )
                }
            }
        }

        item {
            SoftCard {
                SectionHeader(
                    title = "Часовой пояс",
                    subtitle = "Напоминания и отчеты считаются в выбранной зоне",
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        value = timezoneLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Часовой пояс") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        TimezoneCatalog.forEach { option: TimezoneOption ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    onTimezoneChanged(option.zoneId)
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementEntrySheet(
    preferences: UserPreferences,
    onDismiss: () -> Unit,
    onSaveSugar: (Double, Long) -> Unit,
    onSavePressure: (Int, Int, Int, Long) -> Unit,
    onMessage: (String) -> Unit,
) {
    val context = LocalContext.current
    val availableTabs = when (preferences.metricMode) {
        MetricMode.SUGAR -> listOf(EntrySheetTab.SUGAR)
        MetricMode.BLOOD_PRESSURE -> listOf(EntrySheetTab.PRESSURE)
        MetricMode.BOTH -> listOf(EntrySheetTab.SUGAR, EntrySheetTab.PRESSURE)
    }
    var selectedTab by rememberSaveable(preferences.metricMode.name) { mutableStateOf(availableTabs.first()) }

    var sugarValue by rememberSaveable { mutableStateOf("") }
    var sugarTimestamp by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }

    var systolic by rememberSaveable { mutableStateOf("") }
    var diastolic by rememberSaveable { mutableStateOf("") }
    var pulse by rememberSaveable { mutableStateOf("") }
    var pressureTimestamp by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Mist,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Новая запись",
                style = MaterialTheme.typography.headlineLarge,
            )

            if (availableTabs.size > 1) {
                TabRow(selectedTabIndex = availableTabs.indexOf(selectedTab)) {
                    availableTabs.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { Text(tab.title) },
                        )
                    }
                }
            }

            when (selectedTab) {
                EntrySheetTab.SUGAR -> {
                    OutlinedTextField(
                        value = sugarValue,
                        onValueChange = { sugarValue = it },
                        label = { Text("Сахар, ммоль/л") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    MeasurementTimeRow(
                        timestamp = sugarTimestamp,
                        timezoneId = preferences.timezoneId,
                        onNow = { sugarTimestamp = System.currentTimeMillis() },
                        onPick = {
                            showDateTimePicker(context, sugarTimestamp) { chosen ->
                                sugarTimestamp = chosen
                            }
                        },
                    )
                    Button(
                        onClick = {
                            val parsed = sugarValue.replace(",", ".").toDoubleOrNull()
                            when {
                                parsed == null -> onMessage("Введите сахар числом")
                                parsed <= 0.0 || parsed > 40.0 -> onMessage("Сахар должен быть в диапазоне 0..40")
                                else -> onSaveSugar(parsed, sugarTimestamp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Сохранить сахар")
                    }
                }

                EntrySheetTab.PRESSURE -> {
                    OutlinedTextField(
                        value = systolic,
                        onValueChange = { systolic = it },
                        label = { Text("Систолическое") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = diastolic,
                        onValueChange = { diastolic = it },
                        label = { Text("Диастолическое") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = pulse,
                        onValueChange = { pulse = it },
                        label = { Text("Пульс") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    MeasurementTimeRow(
                        timestamp = pressureTimestamp,
                        timezoneId = preferences.timezoneId,
                        onNow = { pressureTimestamp = System.currentTimeMillis() },
                        onPick = {
                            showDateTimePicker(context, pressureTimestamp) { chosen ->
                                pressureTimestamp = chosen
                            }
                        },
                    )
                    Button(
                        onClick = {
                            val sys = systolic.toIntOrNull()
                            val dia = diastolic.toIntOrNull()
                            val pul = pulse.toIntOrNull()
                            when {
                                sys == null || dia == null || pul == null -> onMessage("Введите три числа для давления")
                                sys !in 40..300 || dia !in 30..200 || pul !in 20..250 ->
                                    onMessage("Проверьте диапазоны давления и пульса")
                                else -> onSavePressure(sys, dia, pul, pressureTimestamp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Сохранить давление")
                    }
                }
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text("Закрыть")
            }
        }
    }
}

@Composable
private fun HeroCard(
    dashboard: DashboardUiModel,
    onAddMeasurement: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Clay, Teal, LimeSoft.copy(alpha = 0.9f)),
                    ),
                )
                .padding(22.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                InfoBadge(
                    text = dashboard.statusTitle,
                    icon = Icons.Rounded.MonitorHeart,
                    dark = true,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dashboard.headline,
                            style = MaterialTheme.typography.displayLarge,
                            color = Mist,
                        )
                        Text(
                            text = dashboard.metricSummary,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Mist.copy(alpha = 0.9f),
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.health_popeye_logo_1),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(82.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Mist.copy(alpha = 0.18f))
                            .padding(8.dp),
                    )
                }

                InfoBadge(text = dashboard.nextReminder, icon = Icons.Rounded.NotificationsActive, dark = true)
                InfoBadge(text = dashboard.progressLabel, icon = Icons.Rounded.Schedule, dark = true)

                ElevatedButton(
                    onClick = onAddMeasurement,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Mist,
                        contentColor = Clay,
                    ),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.size(10.dp))
                    Text("Записать замер")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyRhythmCard(dashboard: DashboardUiModel) {
    val progressColor by animateColorAsState(
        targetValue = if (dashboard.completionFraction >= 0.66f) Teal else Clay,
        label = "progress-color",
    )

    SoftCard {
        SectionHeader(
            title = "Ритм дня",
            subtitle = dashboard.latestEvent,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProgressOrbit(
                progress = dashboard.completionFraction,
                tint = progressColor,
                centerText = "${dashboard.todayCompletedCount}",
                footerText = "/${dashboard.todayPlannedCount}",
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = dashboard.statusTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = dashboard.progressLabel,
                    style = MaterialTheme.typography.bodyLarge,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    InfoBadge(
                        text = "${dashboard.scheduleCount} слотов",
                        icon = Icons.Rounded.Schedule,
                    )
                    InfoBadge(
                        text = dashboard.metricSummary.removePrefix("Режим: "),
                        icon = Icons.Rounded.NotificationsActive,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickActionStrip(
    onAddMeasurement: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ActionTile(
            title = "Новый замер",
            subtitle = "Открыть форму ввода",
            icon = Icons.Rounded.Add,
            tint = Clay,
            onClick = onAddMeasurement,
        )
        ActionTile(
            title = "Отчеты",
            subtitle = "Графики и PDF",
            icon = Icons.Rounded.AutoGraph,
            tint = Teal,
            onClick = onOpenReports,
        )
        ActionTile(
            title = "Ритм",
            subtitle = "Слоты и уведомления",
            icon = Icons.Rounded.Tune,
            tint = ClayDark,
            onClick = onOpenSettings,
        )
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(14.dp),
                color = tint.copy(alpha = 0.14f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = tint)
                }
            }
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun PermissionCard(onRequestPermission: () -> Unit) {
    SoftCard {
        SectionHeader(
            title = "Нужен доступ к уведомлениям",
            subtitle = "Без системного разрешения расписание сохранится, но баннеры не появятся",
        )
        ElevatedButton(onClick = onRequestPermission) {
            Text("Разрешить уведомления")
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                color = tint.copy(alpha = 0.14f),
                shape = CircleShape,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = tint)
                }
            }
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsStatusCard(
    preferences: UserPreferences,
    scheduleCount: Int,
) {
    SoftCard {
        SectionHeader(
            title = "Профиль режима",
            subtitle = "Текущее состояние локального трекера",
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            InfoBadge(text = preferences.metricMode.label, icon = Icons.Rounded.MonitorHeart)
            InfoBadge(
                text = if (preferences.notificationsEnabled) "Уведомления включены" else "Уведомления выключены",
                icon = Icons.Rounded.NotificationsActive,
            )
            InfoBadge(text = "$scheduleCount слотов", icon = Icons.Rounded.Schedule)
        }
        Text(
            text = "Часовой пояс: ${preferences.timezoneId}",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun EmptyStateBlock(
    title: String,
    body: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
        )
        if (actionLabel != null && onAction != null) {
            Button(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun EntryCard(entry: TimelineEntryUi) {
    val color = if (entry.kind == MeasurementKind.SUGAR) Clay else Teal
    val icon = if (entry.kind == MeasurementKind.SUGAR) Icons.Rounded.Favorite else Icons.Rounded.MonitorHeart

    SoftCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(16.dp),
                color = color.copy(alpha = 0.14f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = entry.supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
            Text(
                text = entry.timeLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun MetricModeCard(
    mode: MetricMode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val container = if (selected) {
        Brush.linearGradient(listOf(Clay.copy(alpha = 0.16f), Teal.copy(alpha = 0.12f)))
    } else {
        Brush.linearGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(container)
                .animateContentSize(animationSpec = tween(durationMillis = 220))
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = when (mode) {
                        MetricMode.SUGAR -> "Фокус только на глюкозе."
                        MetricMode.BLOOD_PRESSURE -> "Фокус на давлении и пульсе."
                        MetricMode.BOTH -> "Полная картина по двум типам измерений."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun MeasurementTimeRow(
    timestamp: Long,
    timezoneId: String,
    onNow: () -> Unit,
    onPick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Время измерения: ${formatTimestamp(timestamp, timezoneId)}",
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onNow) {
                Text("Сейчас")
            }
            Button(onClick = onPick) {
                Text("Выбрать время")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScheduleChipRow(schedule: List<LocalTime>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        schedule.forEach { time ->
            InfoBadge(text = time.format(UiTimeFormatter), icon = Icons.Rounded.Schedule)
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
        )
    }
}

@Composable
private fun InfoBadge(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    dark: Boolean = false,
) {
    val container = if (dark) {
        Mist.copy(alpha = 0.16f)
    } else {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f)
    }
    val tint = if (dark) Mist else MaterialTheme.colorScheme.secondary

    Surface(
        shape = RoundedCornerShape(100.dp),
        color = container,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (dark) Mist else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SoftCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.animateContentSize(animationSpec = tween(durationMillis = 240)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun ProgressOrbit(
    progress: Float,
    tint: Color,
    centerText: String,
    footerText: String,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "progress-orbit",
    )

    Box(
        modifier = Modifier.size(124.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18f
            drawArc(
                color = tint.copy(alpha = 0.16f),
                startAngle = -210f,
                sweepAngle = 240f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(tint.copy(alpha = 0.6f), tint),
                ),
                startAngle = -210f,
                sweepAngle = 240f * animatedProgress.coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerText,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = footerText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
            )
        }
    }
}

@Composable
private fun AtmosphereBackdrop() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 32.dp, end = 12.dp)
                .size(220.dp, 180.dp),
            shape = PebbleShape,
            color = Clay.copy(alpha = 0.08f),
        ) {}

        Surface(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 0.dp)
                .size(180.dp, 220.dp),
            shape = PebbleShape,
            color = Teal.copy(alpha = 0.08f),
        ) {}

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 24.dp)
                .size(150.dp, 140.dp),
            shape = CircleShape,
            color = LimeSoft.copy(alpha = 0.16f),
        ) {}
    }
}

@Composable
private fun ChartContainer(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp)
            .background(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                shape = RoundedCornerShape(24.dp),
            )
            .padding(12.dp),
    ) {
        content()
    }
}

@Composable
private fun SugarChart(points: List<ChartPoint>) {
    if (points.isEmpty()) {
        EmptyChartState(text = "Пока нет точек по сахару")
        return
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
    ) {
        val padding = 28f
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2

        drawChartGrid(padding, chartWidth, chartHeight)

        val minX = points.minOf { it.timestamp }
        val maxX = points.maxOf { it.timestamp }
        val minY = (points.minOf { it.value } - 1f).coerceAtLeast(0f)
        val maxY = (points.maxOf { it.value } + 1f).coerceAtLeast(minY + 0.5f)

        val offsets = points.map { point ->
            Offset(
                x = padding + normalizeLong(point.timestamp, minX, maxX, chartWidth),
                y = padding + chartHeight - normalize(point.value, minY, maxY, chartHeight),
            )
        }

        val path = buildLinePath(offsets)
        val fillPath = Path().apply {
            if (offsets.isNotEmpty()) {
                moveTo(offsets.first().x, padding + chartHeight)
                offsets.forEachIndexed { index, offset ->
                    if (index == 0) lineTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                }
                lineTo(offsets.last().x, padding + chartHeight)
                close()
            }
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Clay.copy(alpha = 0.28f), Color.Transparent),
                startY = padding,
                endY = size.height,
            ),
        )
        drawPath(
            path = path,
            color = Clay,
            style = Stroke(width = 7f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        offsets.forEach { offset ->
            drawCircle(color = Mist, radius = 10f, center = offset)
            drawCircle(color = Clay, radius = 6f, center = offset)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PressureChart(points: List<PressureChartPoint>) {
    if (points.isEmpty()) {
        EmptyChartState(text = "Пока нет точек по давлению")
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LegendChip(label = "Систолическое", color = Teal)
            LegendChip(label = "Диастолическое", color = Clay)
            LegendChip(label = "Пульс", color = Danger)
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
        ) {
            val padding = 28f
            val chartWidth = size.width - padding * 2
            val chartHeight = size.height - padding * 2

            drawChartGrid(padding, chartWidth, chartHeight)

            val minX = points.minOf { it.timestamp }
            val maxX = points.maxOf { it.timestamp }
            val values = points.flatMap { listOf(it.systolic, it.diastolic, it.pulse) }
            val minY = (values.minOrNull() ?: 0f) - 5f
            val maxY = (values.maxOrNull() ?: 1f) + 5f

            drawSeries(
                points = points.map {
                    Offset(
                        x = padding + normalizeLong(it.timestamp, minX, maxX, chartWidth),
                        y = padding + chartHeight - normalize(it.systolic, minY, maxY, chartHeight),
                    )
                },
                color = Teal,
            )
            drawSeries(
                points = points.map {
                    Offset(
                        x = padding + normalizeLong(it.timestamp, minX, maxX, chartWidth),
                        y = padding + chartHeight - normalize(it.diastolic, minY, maxY, chartHeight),
                    )
                },
                color = Clay,
            )
            drawSeries(
                points = points.map {
                    Offset(
                        x = padding + normalizeLong(it.timestamp, minX, maxX, chartWidth),
                        y = padding + chartHeight - normalize(it.pulse, minY, maxY, chartHeight),
                    )
                },
                color = Danger,
            )
        }
    }
}

@Composable
private fun EmptyChartState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
        )
    }
}

@Composable
private fun LegendChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChartGrid(
    padding: Float,
    width: Float,
    height: Float,
) {
    repeat(4) { index ->
        val y = padding + (height / 3f) * index
        drawLine(
            color = Color.LightGray.copy(alpha = 0.35f),
            start = Offset(padding, y),
            end = Offset(padding + width, y),
            strokeWidth = 2f,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSeries(
    points: List<Offset>,
    color: Color,
) {
    val path = buildLinePath(points)
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
    points.forEach { point ->
        drawCircle(color = Mist, radius = 8f, center = point)
        drawCircle(color = color, radius = 5f, center = point)
    }
}

private fun buildLinePath(points: List<Offset>): Path {
    return Path().apply {
        points.forEachIndexed { index, point ->
            if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
        }
    }
}

private fun normalize(value: Float, min: Float, max: Float, size: Float): Float {
    if (max == min) {
        return size / 2f
    }
    return ((value - min) / (max - min)) * size
}

private fun normalizeLong(value: Long, min: Long, max: Long, size: Float): Float {
    if (max == min) {
        return size / 2f
    }
    return ((value - min).toFloat() / (max - min).toFloat()) * size
}

private fun formatTimestamp(timestamp: Long, timezoneId: String): String {
    val zone = runCatching { ZoneId.of(timezoneId) }.getOrDefault(ZoneId.systemDefault())
    return Instant.ofEpochMilli(timestamp).atZone(zone).format(UiDateFormatter)
}

private fun showTimePicker(
    context: Context,
    initialTime: LocalTime,
    onSelected: (LocalTime) -> Unit,
) {
    TimePickerDialog(
        context,
        { _, hour, minute -> onSelected(LocalTime.of(hour, minute)) },
        initialTime.hour,
        initialTime.minute,
        true,
    ).show()
}

private fun showDateTimePicker(
    context: Context,
    initialMillis: Long,
    onSelected: (Long) -> Unit,
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = initialMillis
    }

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val pickedCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
            }

            TimePickerDialog(
                context,
                { _, hour, minute ->
                    pickedCalendar.set(Calendar.HOUR_OF_DAY, hour)
                    pickedCalendar.set(Calendar.MINUTE, minute)
                    onSelected(pickedCalendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true,
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH),
    ).show()
}
