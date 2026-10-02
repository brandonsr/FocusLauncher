package com.example.focuslauncher

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlin.math.abs

enum class GestureAxis { NONE, HORIZONTAL, VERTICAL }

@Composable
fun HomeScreen(appViewModel: AppViewModel = viewModel()) {
    val context = LocalContext.current
    val apps by appViewModel.apps.collectAsState()
    val pinnedPackages by appViewModel.pinnedPackages.collectAsState()
    val musicState by appViewModel.musicState.collectAsState()

    var showDrawer by remember { mutableStateOf(false) }
    var showFocusMode by remember { mutableStateOf(false) }
    var showScreenTime by remember { mutableStateOf(false) }

    var isListenerEnabled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            isListenerEnabled = appViewModel.isNotificationListenerEnabled()
            delay(2000L)
        }
    }

    // Performance optimization: Load screen time stats only when screen is shown
    LaunchedEffect(showScreenTime) {
        if (showScreenTime) {
            appViewModel.loadScreenTimeStats()
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isListenerEnabled = appViewModel.isNotificationListenerEnabled()
                appViewModel.loadScreenTimeStats()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // Initial check
        isListenerEnabled = appViewModel.isNotificationListenerEnabled()

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val pinnedApps = remember(apps, pinnedPackages) {
        pinnedPackages.mapNotNull { pkg -> apps.find { it.packageName == pkg } }
    }

    // Focus mode takes over the whole screen
    if (showFocusMode) {
        FocusModeScreen(
            appViewModel = appViewModel,
            musicState = musicState,
            isListenerEnabled = isListenerEnabled,
            onPlayPause = { appViewModel.playPause() },
            onNext = { appViewModel.nextTrack() },
            onPrevious = { appViewModel.previousTrack() },
            onEnableListener = {
                context.startActivity(
                    Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                )
            },
            onExit = { showFocusMode = false }
        )
        return
    }

    var dragAccumulatorX by remember { mutableFloatStateOf(0f) }
    var dragAccumulatorY by remember { mutableFloatStateOf(0f) }
    var lockedAxis by remember { mutableStateOf(GestureAxis.NONE) }
    val swipeThreshold = 80f
    val axisLockThreshold = 30f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(showDrawer) {
                detectDragGestures(
                    onDragStart = {
                        dragAccumulatorX = 0f
                        dragAccumulatorY = 0f
                        lockedAxis = GestureAxis.NONE
                    },
                    onDragEnd = {
                        dragAccumulatorX = 0f
                        dragAccumulatorY = 0f
                        lockedAxis = GestureAxis.NONE
                    },
                    onDragCancel = {
                        dragAccumulatorX = 0f
                        dragAccumulatorY = 0f
                        lockedAxis = GestureAxis.NONE
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()

                        if (lockedAxis == GestureAxis.NONE) {
                            dragAccumulatorX += dragAmount.x
                            dragAccumulatorY += dragAmount.y

                            if (abs(dragAccumulatorX) > axisLockThreshold && abs(dragAccumulatorX) > abs(dragAccumulatorY)) {
                                lockedAxis = GestureAxis.HORIZONTAL
                            } else if (abs(dragAccumulatorY) > axisLockThreshold && abs(dragAccumulatorY) > abs(dragAccumulatorX)) {
                                lockedAxis = GestureAxis.VERTICAL
                            }
                        } else if (lockedAxis == GestureAxis.HORIZONTAL) {
                            dragAccumulatorX += dragAmount.x

                            when {
                                !showDrawer && dragAccumulatorX < -swipeThreshold -> {
                                    showDrawer = true
                                    dragAccumulatorX = 0f
                                }
                                showDrawer && dragAccumulatorX > swipeThreshold -> {
                                    showDrawer = false
                                    dragAccumulatorX = 0f
                                }
                                !showDrawer && dragAccumulatorX > swipeThreshold -> {
                                    showFocusMode = true
                                    dragAccumulatorX = 0f
                                }
                            }
                        } else if (lockedAxis == GestureAxis.VERTICAL) {
                            dragAccumulatorY += dragAmount.y

                            if (!showDrawer && dragAccumulatorY > swipeThreshold) {
                                showScreenTime = true
                                dragAccumulatorY = 0f
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

        AnimatedVisibility(
            visible = showScreenTime,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            ScreenTimeScreen(
                stats = appViewModel.screenTimeStats.collectAsState().value,
                onDismiss = { showScreenTime = false },
                onEnablePermission = {
                    appViewModel.requestUsageStatsPermission()
                }
            )
        }
    }
}