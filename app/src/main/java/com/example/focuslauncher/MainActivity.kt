package com.example.focuslauncher

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.focuslauncher.ui.theme.FocusLauncherTheme

class MainActivity : ComponentActivity() {

    // Launcher to handle the result of the default app request (Android 10+)
    private val roleRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // You can handle the result here if needed (e.g., check if they actually set it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check and prompt the user to set this app as the default launcher on startup
        checkAndPromptDefaultLauncher()

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

    private fun checkAndPromptDefaultLauncher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 (API 29) and above, use RoleManager
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager

            // Check if the home role is available and if our app doesn't already hold it
            if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {

                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                roleRequestLauncher.launch(intent)
            }
        } else {
            // Fallback for Android 9 (API 28) and below
            val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
            val resolveInfo = packageManager.resolveActivity(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)

            // If our app is not the default, open the system settings screen
            if (resolveInfo?.activityInfo?.packageName != packageName) {
                val settingsIntent = Intent(Settings.ACTION_HOME_SETTINGS)
                startActivity(settingsIntent)
            }
        }
    }
}