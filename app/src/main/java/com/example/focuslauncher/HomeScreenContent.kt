package com.example.focuslauncher

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreenContent(
    pinnedApps: List<AppInfo>,
    musicState: MusicState?,
    isListenerEnabled: Boolean,
    onEnableListener: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
    ) {
        // ── Clock ────────────────────────────────────────────────────────────
        ClockWidget(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp)
        )

        Spacer(Modifier.height(28.dp))

        // ── Music widget ─────────────────────────────────────────────────────
        MusicWidget(
            musicState = musicState,
            isListenerEnabled = isListenerEnabled,
            onEnableListener = onEnableListener,
            onPlayPause = onPlayPause,
            onNext = onNext,
            onPrevious = onPrevious,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(24.dp))

        // ── Pinned apps — centered text list ─────────────────────────────────
        if (pinnedApps.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(pinnedApps, key = { it.packageName }) { app ->
                    Text(
                        text = app.label,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .clickable { launchApp(context, app.packageName) }
                            .padding(horizontal = 32.dp, vertical = 11.dp)
                    )
                }
            }
        } else {
            Spacer(Modifier.weight(1f))
        }
    }
}