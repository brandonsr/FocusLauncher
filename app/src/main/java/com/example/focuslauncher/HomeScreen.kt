package com.example.focuslauncher

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(appViewModel: AppViewModel = viewModel()) {
    val context = LocalContext.current
    val apps by appViewModel.apps.collectAsState()
    var showDrawer by remember { mutableStateOf(false) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // available.y is negative when swiping up
                if (available.y < -30f) {
                    showDrawer = true
                }
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
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