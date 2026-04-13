package com.example.focuslauncher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps

    private val packageReceiver = PackageReceiver(
        onPackageChanged = { loadApps() }
    )

    init {
        loadApps()
        registerReceiver()
    }

    fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _apps.value = getInstalledApps(getApplication())
        }
    }

    private fun registerReceiver() {
        getApplication<Application>().registerReceiver(
            packageReceiver,
            PackageReceiver.createIntentFilter()
        )
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(packageReceiver)
    }
}