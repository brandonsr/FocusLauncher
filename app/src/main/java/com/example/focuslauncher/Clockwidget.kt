package com.example.focuslauncher

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClockWidget(modifier: Modifier = Modifier) {
    var time by remember { mutableStateOf(formatTime()) }
    var date by remember { mutableStateOf(formatDate()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            time = formatTime()
            date = formatDate()
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = time,
            fontSize = 82.sp,
            fontWeight = FontWeight.Thin,
            color = Color.White,
            letterSpacing = (-3).sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = date,
            fontSize = 15.sp,
            fontWeight = FontWeight.Light,
            color = Color.White.copy(alpha = 0.55f),
            letterSpacing = 0.8.sp
        )
    }
}

private fun formatTime(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

private fun formatDate(): String =
    SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())