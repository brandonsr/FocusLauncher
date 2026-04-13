package com.example.focuslauncher

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel

class AppViewModel(application: Application) : AndroidViewModel(application) {
    val apps = mutableStateOf(getInstalledApps(application))
}