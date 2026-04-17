package com.example.focuslauncher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(appViewModel: AppViewModel = viewModel()) {
    val context = LocalContext.current
    val apps by appViewModel.apps.collectAsState()
    val pinnedPackages by appViewModel.pinnedPackages.collectAsState()
    var showDrawer by remember { mutableStateOf(false) }

    // Resolve full AppInfo for each pinned package, preserving pin order
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
                    onDragStart = { dragAccumulator = 0f },
                    onDragEnd = { dragAccumulator = 0f },
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
            apps = apps,
            pinnedApps = pinnedApps
        )

        // Drawer slides in from the left (matches swipe-left-to-open feel)
        AnimatedVisibility(
            visible = showDrawer,
            enter = slideInHorizontally { fullWidth -> -fullWidth } + fadeIn(),
            exit = slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut()
        ) {
            AppDrawerScreen(
                apps = apps,
                pinnedPackages = pinnedPackages,
                onAppClick = { app ->
                    launchApp(context, app.packageName)
                    showDrawer = false
                },
                onTogglePin = { app -> appViewModel.togglePin(app.packageName) },
                onDismiss = { showDrawer = false }
            )
        }
    }
}