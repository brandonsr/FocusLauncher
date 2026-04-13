package com.example.focuslauncher

import android.content.Context
import android.content.Intent

fun getInstalledApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    return pm.queryIntentActivities(intent, 0).map { resolveInfo ->
        AppInfo(
            label = resolveInfo.loadLabel(pm).toString(),
            packageName = resolveInfo.activityInfo.packageName,
            icon = resolveInfo.loadIcon(pm)
        )
    }.sortedBy { it.label }
}

fun launchApp(context: Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    intent?.let { context.startActivity(it) }
}