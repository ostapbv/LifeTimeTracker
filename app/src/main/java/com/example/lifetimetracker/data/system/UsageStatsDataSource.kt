package com.example.lifetimetracker.data.system

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class UsageStatsDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager

    // MVP mapping dictionary
    private val packageCategoryMap = mapOf(
        "com.google.android.youtube" to "social_video",
        "com.instagram.android" to "social_video",
        "com.zhiliaoapp.musically" to "social_video", // TikTok
        "com.facebook.katana" to "social_video",
        "com.twitter.android" to "social_video",
        "com.Slack" to "work",
        "com.microsoft.teams" to "work",
        "com.google.android.apps.docs.editors.docs" to "work",
        "com.duolingo" to "learning",
        "org.coursera.android" to "learning",
        "org.telegram.messenger" to "personal",
        "com.whatsapp" to "personal"
    )

    fun hasUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getUsageStatsForDate(date: LocalDate): List<RawUsageStat> {
        if (!hasUsagePermission()) return emptyList()

        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            endOfDay
        )

        return stats.mapNotNull { stat ->
            val totalTimeForeground = stat.totalTimeInForeground
            val minutes = (totalTimeForeground / 1000 / 60).toInt()
            
            if (minutes > 0) {
                val appLabel = try {
                    val appInfo = packageManager.getApplicationInfo(stat.packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }

                val categoryKey = packageCategoryMap[stat.packageName] ?: "other"

                RawUsageStat(
                    packageName = stat.packageName,
                    appLabel = appLabel,
                    categoryKey = categoryKey,
                    foregroundMinutes = minutes
                )
            } else {
                null
            }
        }
    }
}

data class RawUsageStat(
    val packageName: String,
    val appLabel: String?,
    val categoryKey: String,
    val foregroundMinutes: Int
)
