package com.example.focuslauncher

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.focuslauncher.ui.theme.FocusLauncherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Show the system wallpaper behind the launcher
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)

        setContent {
            FocusLauncherTheme {
                // Swallow the back button — launchers should never "go back"
                BackHandler(enabled = true) { }

                HomeScreen()
            }
        }
    }
}