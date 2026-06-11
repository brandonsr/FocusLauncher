package com.example.focuslauncher

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.database.Cursor
import android.provider.CalendarContract
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ── Data Class for Events ──────────────────────────────────────────────────
data class CalendarEvent(val title: String, val startTime: Long)

// ── Helper Function to Fetch Events ─────────────────────────────────────────
fun getTodayEvents(context: Context): List<CalendarEvent> {
    val events = mutableListOf<CalendarEvent>()
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    val startOfDay = calendar.timeInMillis

    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    val endOfDay = calendar.timeInMillis

    val projection = arrayOf(
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DTSTART
    )

    val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
    val selectionArgs = arrayOf(startOfDay.toString(), endOfDay.toString())

    try {
        val cursor: Cursor? = context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${CalendarContract.Events.DTSTART} ASC"
        )

        cursor?.use {
            val titleIndex = it.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
            val startIndex = it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)

            while (it.moveToNext()) {
                events.add(
                    CalendarEvent(
                        title = it.getString(titleIndex),
                        startTime = it.getLong(startIndex)
                    )
                )
            }
        }
    } catch (e: SecurityException) {
        // Permission was not granted
    }
    return events
}

@Composable
fun FocusModeScreen(
    musicState: MusicState?,
    isListenerEnabled: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onEnableListener: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val notificationManager = remember {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    val hasDndAccess by remember {
        derivedStateOf { notificationManager.isNotificationPolicyAccessGranted }
    }

    var minutesInput by remember { mutableStateOf("25") }
    var timerStarted by remember { mutableStateOf(false) }
    var timerRunning by remember { mutableStateOf(false) }
    var secondsRemaining by remember { mutableIntStateOf(0) }

    // Calendar state
    var hasCalendarPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CALENDAR
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCalendarPermission = granted }
    )

    // Request calendar permission on launch if needed
    LaunchedEffect(Unit) {
        if (!hasCalendarPermission) {
            calendarPermissionLauncher.launch(android.Manifest.permission.READ_CALENDAR)
        }
    }

    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (secondsRemaining > 0 && timerRunning) {
                delay(1000L)
                secondsRemaining--
            }
            if (secondsRemaining == 0 && timerStarted) {
                restoreDnd(notificationManager, hasDndAccess)
                timerRunning = false
                timerStarted = false
            }
        }
    }

    fun startSession() {
        val minutes = minutesInput.toIntOrNull()?.coerceIn(1, 180) ?: 25
        minutesInput = minutes.toString()
        secondsRemaining = minutes * 60
        timerStarted = true
        timerRunning = true
        if (hasDndAccess) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        }
    }

    fun endSession() {
        timerRunning = false
        timerStarted = false
        restoreDnd(notificationManager, hasDndAccess)
        onExit()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
    ) {
        if (!timerStarted) {
            // ── Setup screen (centered) ───────────────────────────────────────
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Focus Mode",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Thin,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Spacer(Modifier.height(24.dp))

                if (!hasDndAccess) {
                    Text(
                        "Grant DND access to silence notifications",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                    TextButton(onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        )
                    }) {
                        Text("Grant Access", color = Color(0xFF4FC3F7), fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Duration", fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
                    Spacer(Modifier.width(16.dp))
                    OutlinedTextField(
                        value = minutesInput,
                        onValueChange = { v ->
                            minutesInput = v.filter { it.isDigit() }.take(3)
                        },
                        suffix = { Text("min", color = Color.White.copy(alpha = 0.4f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White.copy(alpha = 0.4f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            cursorColor = Color.White
                        ),
                        modifier = Modifier.width(100.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextButton(onClick = onExit) {
                        Text("Cancel", fontSize = 13.sp, color = Color.White.copy(alpha = 0.3f))
                    }
                    Box(
                        modifier = Modifier.background(
                            Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(12.dp)
                        )
                    ) {
                        TextButton(onClick = { startSession() }) {
                            Text("Begin Session", fontSize = 15.sp, color = Color.White)
                        }
                    }
                }
            }
        } else {
            // ── Active timer: left / right split ─────────────────────────────
            Row(modifier = Modifier.fillMaxSize()) {

                // ── LEFT: timer + end session ─────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 40.dp, end = 20.dp, top = 28.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(Modifier.height(1.dp)) // top anchor

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val mm = secondsRemaining / 60
                        val ss = secondsRemaining % 60
                        Text(
                            text = "%02d:%02d".format(mm, ss),
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Thin,
                            color = Color.White,
                            letterSpacing = (-2).sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (hasDndAccess) "Focus Mode  ·  DND On"
                            else "Focus Mode",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.28f),
                            letterSpacing = 1.sp
                        )
                    }

                    TextButton(onClick = { endSession() }) {
                        Text(
                            "End Session",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.35f)
                        )
                    }
                }

                // Thin vertical divider
                Box(
                    modifier = Modifier
                        .width(0.5.dp)
                        .fillMaxHeight()
                        .padding(vertical = 28.dp)
                        .background(Color.White.copy(alpha = 0.08f))
                )

                // ── RIGHT: music widget + scrollable calendar ────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 20.dp, end = 40.dp, top = 28.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    MusicWidget(
                        musicState = musicState,
                        isListenerEnabled = isListenerEnabled,
                        onEnableListener = onEnableListener,
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                        onPrevious = onPrevious,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Scrollable section for Calendar and Events
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // Takes remaining space
                            .background(
                                Color.White.copy(alpha = 0.07f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        item {
                            MiniCalendar(modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(16.dp))

                            // Events Header
                            Text(
                                "Today's Agenda",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (!hasCalendarPermission) {
                                Text(
                                    "Calendar permission required to view events.",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.3f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                )
                            }
                        }

                        if (hasCalendarPermission) {
                            val events = getTodayEvents(context)
                            if (events.isEmpty()) {
                                item {
                                    Text(
                                        "No events today",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            } else {
                                items(events) { event ->
                                    EventItem(event)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Individual Event Item Composable ─────────────────────────────────────────

@Composable
fun EventItem(event: CalendarEvent) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(event.startTime))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(Color(0xFF4FC3F7), CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = event.title,
                fontSize = 13.sp,
                color = Color.White,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formattedTime,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.45f)
            )
        }
    }
}

// ── Mini calendar widget ──────────────────────────────────────────────────────

@Composable
private fun MiniCalendar(modifier: Modifier = Modifier) {
    val today = remember { Calendar.getInstance() }
    val currentDay = today.get(Calendar.DAY_OF_MONTH)
    val monthName = today.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: ""
    val year = today.get(Calendar.YEAR)

    val firstOfMonth = remember {
        Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
    }
    // Monday-first offset (Calendar.SUNDAY=1 … SATURDAY=7)
    val offset = (firstOfMonth.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7
    val daysInMonth = firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val rows = (offset + daysInMonth + 6) / 7

    Column(modifier = modifier) {
        Text(
            "$monthName $year",
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
            color = Color.White.copy(alpha = 0.45f)
        )

        Spacer(Modifier.height(8.dp))

        // Day-of-week header
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                Text(
                    text = d,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.22f)
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Day grid
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val day = row * 7 + col - offset + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day in 1..daysInMonth) {
                            if (day == currentDay) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(Color.White.copy(alpha = 0.18f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        day.toString(),
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Text(
                                    day.toString(),
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.38f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun restoreDnd(nm: NotificationManager, hasDndAccess: Boolean) {
    if (hasDndAccess) {
        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
    }
}