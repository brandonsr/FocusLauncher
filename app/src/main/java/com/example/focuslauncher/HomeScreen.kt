package com.example.focuslauncher

// In your HomeScreen
var showDrawer by remember { mutableStateOf(false) }

Box(modifier = Modifier
.fillMaxSize()
.pointerInput(Unit) {
    detectVerticalDragGestures { _, dragAmount ->
        if (dragAmount < -30f) showDrawer = true  // swipe up
    }
}
) {
    HomeScreenContent()

    if (showDrawer) {
        AppDrawerScreen(
            apps = appList,
            onAppClick = { app ->
                launchApp(app)
                showDrawer = false
            },
            onDismiss = { showDrawer = false }
        )
    }
}