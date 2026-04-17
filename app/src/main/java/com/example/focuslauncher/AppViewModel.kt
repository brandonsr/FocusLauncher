package com.example.focuslauncher

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("focus_launcher", Context.MODE_PRIVATE)

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps

    private val _pinnedPackages = MutableStateFlow<List<String>>(loadPinnedPackages())
    val pinnedPackages: StateFlow<List<String>> = _pinnedPackages

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

    fun togglePin(packageName: String) {
        val current = _pinnedPackages.value.toMutableList()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        _pinnedPackages.value = current
        savePinnedPackages(current)
    }

    private fun loadPinnedPackages(): List<String> {
        val raw = prefs.getString("pinned_packages", "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(",")
    }

    private fun savePinnedPackages(packages: List<String>) {
        prefs.edit().putString("pinned_packages", packages.joinToString(",")).apply()
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