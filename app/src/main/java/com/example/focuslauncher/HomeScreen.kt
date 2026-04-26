package com.example.focuslauncher

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(appViewModel: AppViewModel = viewModel()) {
    val context = LocalContext.current
    val apps by appViewModel.apps.collectAsState()
    val pinnedPackages by appViewModel.pinnedPackages.collectAsState()
    val musicState by appViewModel.musicState.collectAsState()
    var showDrawer by remember { mutableStateOf(false) }

    // Poll listener status so the widget updates automatically after the user
    // grants access in Settings and returns to the launcher.
    var isListenerEnabled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            isListenerEnabled = appViewModel.isNotificationListenerEnabled()
            delay(2000L)
        }
    }

    val pinnedApps = remember(apps, pinnedPackages) {
        pinnedPackages.mapNotNull { pkg -> apps.find { it.packageName == pkg } }
    }

    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 80f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(showDrawer) {
                detectHorizontalDragGestures(
                    onDragStart  = { dragAccumulator = 0f },
                    onDragEnd    = { dragAccumulator = 0f },
                    onDragCancel = { dragAccumulator = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulator += dragAmount
                        when {
                            !showDrawer && dragAccumulator < -swipeThreshold -> {
                                showDrawer = true
                                dragAccumulator = 0f
                            }
                            showDrawer && dragAccumulator > swipeThreshold -> {
                                showDrawer = false
                                dragAccumulator = 0f
                            }
                        }
                    }
                )
            }
    ) {
        HomeScreenContent(
            pinnedApps = pinnedApps,
            musicState = musicState,
            isListenerEnabled = isListenerEnabled,
            onEnableListener = {
                context.startActivity(
                    Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                )
            },
            onPlayPause  = { appViewModel.playPause() },
            onNext       = { appViewModel.nextTrack() },
            onPrevious   = { appViewModel.previousTrack() }
        )

        // App drawer — slides in from the left on swipe-left
        AnimatedVisibility(
            visible = showDrawer,
            enter = slideInHorizontally { -it } + fadeIn(),
            exit  = slideOutHorizontally { -it } + fadeOut()
        ) {
            AppDrawerScreen(
                apps = apps,
                pinnedPackages = pinnedPackages,
                onAppClick = { app ->
                    launchApp(context, app.packageName)
                    showDrawer = false
                },
                onTogglePin = { app -> appViewModel.togglePin(app.packageName) },
                onDismiss   = { showDrawer = false }
            )
        }
    }
}