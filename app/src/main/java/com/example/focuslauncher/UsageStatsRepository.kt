package com.example.focuslauncher

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import java.util.*
import java.util.concurrent.TimeUnit

data class ScreenTimeStats(
    val totalTimeFormatted: String,
    val topApps: List<AppUsage>
)

data class AppUsage(
    val name: String,
    val durationFormatted: String
)

class UsageStatsRepository {

    fun hasPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getTodayStats(context: Context): ScreenTimeStats? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        return try {
            // Try queryUsageStats first as it is generally more reliable across Android versions
            val statsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            if (statsList.isNullOrEmpty()) {
                // Fallback to aggregate query if the list is empty
                val statsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
                if (statsMap.isNullOrEmpty()) return null

                val totalForegroundTimeMs = statsMap.values.sumOf { it.totalTimeInForeground }
                val topApps = statsMap.entries
                    .filter { it.value.totalTimeInForeground > 0 }
                    .sortedByDescending { it.value.totalTimeInForeground }
                    .take(5)
                    .map { (packageName, stats) ->
                        val appName = try {
                            val pm = context.packageManager
                            val appInfo = pm.getApplicationInfo(packageName, 0)
                            pm.getApplicationLabel(appInfo).toString()
                        } catch (e: Exception) {
                            packageName
                        }
                        AppUsage(name = appName, durationFormatted = formatMillis(stats.totalTimeInForeground))
                    }
                return ScreenTimeStats(totalTimeFormatted = formatMillis(totalForegroundTimeMs), topApps = topApps)
            }

            // Aggregate stats from the list
            val aggregatedStats = statsList.groupBy { it.packageName }
                .mapValues { (_, stats) ->
                    stats.sumOf { it.totalTimeInForeground }
                }

            val totalForegroundTimeMs = aggregatedStats.values.sum()

            val topApps = aggregatedStats.entries
                .filter { it.value > 0 }
                .sortedByDescending { it.value }
                .take(5)
                .map { (packageName, totalTime) ->
                    val appName = try {
                        val pm = context.packageManager
                        val appInfo = pm.getApplicationInfo(packageName, 0)
                        pm.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        packageName
                    }
                    AppUsage(name = appName, durationFormatted = formatMillis(totalTime))
                }

            ScreenTimeStats(
                totalTimeFormatted = formatMillis(totalForegroundTimeMs),
                topApps = topApps
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun formatMillis(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
