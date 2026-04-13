package com.example.focuslauncher

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(appViewModel: AppViewModel = viewModel()) {
    val context = LocalContext.current
    val apps by appViewModel.apps.collectAsState()
    var showDrawer: Boolean by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change: PointerInputChange, dragAmount: Float ->
                        change.consume()
                        if (dragAmount < -30f) showDrawer = true
                    }
                )
            }
    ) {
        HomeScreenContent(apps = apps)

        if (showDrawer) {
            AppDrawerScreen(
                apps = apps,
                onAppClick = { app ->
                    launchApp(context, app.packageName)
                    showDrawer = false
                },
                onDismiss = { showDrawer = false }
            )
        }
    }
}