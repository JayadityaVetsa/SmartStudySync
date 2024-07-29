package com.example.timemanagement

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Preview
@Composable
fun CalendarPreview(){
    val sampleEvents = mapOf(
        LocalDate.of(2024, 6, 19) to listOf(
            "Meeting with Bob",
            "Dentist appointment"
        ),
        LocalDate.of(2024, 6, 20) to listOf(
            "Project deadline"
        ),
        LocalDate.of(2024, 6, 25) to listOf(
            "Team lunch",
            "Presentation"
        ),
        LocalDate.of(2024, 7, 1) to listOf(
            "Monthly review meeting"
        )
    )
    CalendarApp(sampleEvents)
}

@Composable
fun CalendarApp(
    events: Map<LocalDate, List<String>>, // Use non-nullable types
    viewModel: CalendarViewModel = viewModel()
) {
    val previousEvents = remember { mutableStateOf<Map<LocalDate, List<String>>>(emptyMap()) }

    LaunchedEffect(events) {
        if (events != previousEvents.value) {
            previousEvents.value = events
            viewModel.loadEvents(events)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val eventsState by viewModel.events.collectAsState()

    // Load the passed events into the ViewModel only once
    Log.e("CalendarApp", "Received events: $events")
    events.forEach { (date, eventList) ->
        Log.e("CalendarApp", "Date: $date, Events: $eventList")
        // Render logic for red dots or events
    }

    Scaffold(
        modifier = Modifier
    ) { padding ->
        Surface(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
            ) {
                CalendarWidget(
                    days = DateUtil.daysOfWeek,
                    yearMonth = uiState.yearMonth,
                    dates = uiState.dates,
                    events = eventsState,
                    onPreviousMonthButtonClicked = { prevMonth ->
                        viewModel.toPreviousMonth(prevMonth)
                    },
                    onNextMonthButtonClicked = { nextMonth ->
                        viewModel.toNextMonth(nextMonth)
                    },
                    onDateClickListener = { date ->
                        val selectedDate = LocalDate.of(
                            uiState.yearMonth.year,
                            uiState.yearMonth.month,
                            date.dayOfMonth.toInt()
                        )
                        viewModel.selectDate(selectedDate)
                    }
                )
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp).padding(16.dp)) {
                    val selectedDate = uiState.selectedDate
                    if (selectedDate != null) {
                        val eventsForSelectedDate = eventsState[selectedDate].orEmpty()
                        if (eventsForSelectedDate.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Click the plus icon to add events")
                                }
                            }
                        } else {
                            items(eventsForSelectedDate) { event ->
                                Text(event, modifier = Modifier.padding(8.dp))
                            }
                        }
                    } else {
                        item {
                            Text("No date selected", modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarWidget(
    days: Array<String>,
    yearMonth: YearMonth,
    dates: List<CalendarUiState.Date>,
    events: Map<LocalDate, List<String>>,
    onPreviousMonthButtonClicked: (YearMonth) -> Unit,
    onNextMonthButtonClicked: (YearMonth) -> Unit,
    onDateClickListener: (CalendarUiState.Date) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Display days of the week
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp) // Adjust horizontal padding if needed
        ) {
            repeat(days.size) {
                val item = days[it]
                DayItem(day = item, modifier = Modifier.weight(1f))
            }
        }
        // Display calendar header
        Header(
            yearMonth = yearMonth,
            onPreviousMonthButtonClicked = onPreviousMonthButtonClicked,
            onNextMonthButtonClicked = onNextMonthButtonClicked
        )
        // Display calendar content
        Content(
            dates = dates,
            events = events,
            yearMonth = yearMonth,
            onDateClickListener = onDateClickListener
        )
    }
}

@Composable
fun Header(
    yearMonth: YearMonth,
    onPreviousMonthButtonClicked: (YearMonth) -> Unit,
    onNextMonthButtonClicked: (YearMonth) -> Unit,
) {
    Row {
        IconButton(onClick = {
            onPreviousMonthButtonClicked.invoke(yearMonth.minusMonths(1))
        }) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Month"
            )
        }
        Text(
            text = yearMonth.getDisplayName(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        )
        IconButton(onClick = {
            onNextMonthButtonClicked.invoke(yearMonth.plusMonths(1))
        }) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Next Month"
            )
        }
    }
}

@Composable
fun DayItem(day: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(2.dp) // Adjust padding to ensure that items fit well
            .fillMaxWidth()
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(vertical = 8.dp) // Adjust vertical padding if needed
        )
    }
}

@Composable
fun Content(
    dates: List<CalendarUiState.Date>,
    events: Map<LocalDate, List<String>>,
    yearMonth: YearMonth,
    onDateClickListener: (CalendarUiState.Date) -> Unit,
) {
    Column {
        var index = 0
        repeat(6) {
            if (index >= dates.size) return@repeat
            Row {
                repeat(7) {
                    val item = if (index < dates.size) dates[index] else CalendarUiState.Date.Empty
                    ContentItem(
                        date = item,
                        events = events,
                        yearMonth = yearMonth,
                        onClickListener = onDateClickListener,
                        modifier = Modifier.weight(1f)
                    )
                    index++
                }
            }
        }
    }
}

@Composable
fun ContentItem(
    date: CalendarUiState.Date,
    events: Map<LocalDate, List<String>>,
    yearMonth: YearMonth,
    onClickListener: (CalendarUiState.Date) -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine if the current date has any events
    val hasEvents = date.dayOfMonth.toIntOrNull()?.let { day ->
        events.containsKey(LocalDate.of(yearMonth.year, yearMonth.month, day))
    } ?: false

    Box(
        modifier = modifier
            .background(
                color = if (date.isSelected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    Color.Transparent
                }
            )
            .clickable {
                if (date.dayOfMonth.isNotEmpty()) {
                    onClickListener(date)
                }
            }
    ) {
        Text(
            text = date.dayOfMonth,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(10.dp)
        )
        // Add a condition to draw the red dot
        if (hasEvents) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color.Red)
                    .align(Alignment.BottomCenter)
            )
        }
    }
    Log.e("ContentItem", "Date: ${date.dayOfMonth}, hasEvents: $hasEvents")
}


object DateUtil {
    val daysOfWeek: Array<String>
        get() {
            val daysOfWeek = Array(7) { "" }
            for (dayOfWeek in DayOfWeek.entries) {
                val localizedDayName = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                daysOfWeek[dayOfWeek.ordinal] = localizedDayName
            }
            return daysOfWeek
        }
}

fun YearMonth.getDayOfMonthStartingFromMonday(): List<LocalDate> {
    val firstDayOfMonth = LocalDate.of(year, month, 1)
    val firstDayOfNextMonth = firstDayOfMonth.plusMonths(1)
    val firstDayOfCalendar = firstDayOfMonth.with(DayOfWeek.MONDAY)

    return generateSequence(firstDayOfCalendar) { it.plusDays(1) }
        .takeWhile { it.isBefore(firstDayOfNextMonth) || it.dayOfWeek != DayOfWeek.MONDAY }
        .toList()
}

fun YearMonth.getDisplayName(): String {
    return "${month.getDisplayName(TextStyle.FULL, Locale.getDefault())} $year"
}

class CalendarDataSource {
    fun getDates(yearMonth: YearMonth, selectedDate: LocalDate?): List<CalendarUiState.Date> {
        val firstDayOfMonth = LocalDate.of(yearMonth.year, yearMonth.month, 1)
        val lastDayOfMonth = yearMonth.atEndOfMonth()
        val startOfMonth = firstDayOfMonth.with(DayOfWeek.MONDAY)
        val endOfMonth = lastDayOfMonth.with(DayOfWeek.SUNDAY)

        return generateSequence(startOfMonth) { it.plusDays(1) }
            .takeWhile { !it.isAfter(endOfMonth) }
            .map { date ->
                CalendarUiState.Date(
                    dayOfMonth = if (date.month == yearMonth.month) "${date.dayOfMonth}" else "",
                    isSelected = date == selectedDate
                )
            }
            .toList()
    }
}

data class CalendarUiState(
    val yearMonth: YearMonth,
    val dates: List<Date>,
    val selectedDate: LocalDate? = LocalDate.now()
) {
    data class Date(
        val dayOfMonth: String,
        val isSelected: Boolean
    ) {
        companion object {
            val Empty = Date("", false)
        }
    }

    companion object {
        val Init = CalendarUiState(
            yearMonth = YearMonth.now(),
            dates = emptyList()
        )
    }
}

class CalendarViewModel : ViewModel() {
    private val dataSource by lazy { CalendarDataSource() }
    private val _uiState = MutableStateFlow(CalendarUiState.Init)
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<Map<LocalDate, List<String>>>(emptyMap())
    val events: StateFlow<Map<LocalDate, List<String>>> = _events.asStateFlow()

    init {
        updateDates()
    }

    private fun updateDates() {
        val currentMonth = _uiState.value.yearMonth
        val selectedDate = _uiState.value.selectedDate
        _uiState.value = _uiState.value.copy(
            dates = dataSource.getDates(currentMonth, selectedDate)
        )
    }

    fun toNextMonth(nextMonth: YearMonth) {
        val selectedDate = _uiState.value.selectedDate
        _uiState.value = _uiState.value.copy(
            yearMonth = nextMonth,
            dates = dataSource.getDates(nextMonth, selectedDate)
        )
    }

    fun toPreviousMonth(prevMonth: YearMonth) {
        val selectedDate = _uiState.value.selectedDate
        _uiState.value = _uiState.value.copy(
            yearMonth = prevMonth,
            dates = dataSource.getDates(prevMonth, selectedDate)
        )
    }

    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        updateDates()
    }

    fun loadEvents(events: Map<LocalDate, List<String>>) {
        _events.value = events
    }
}
