package com.example.medtrack.util

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Helpers for the Android 12+ "schedule exact alarms" runtime special access.
 *
 * The app no longer declares the restricted [android.Manifest.permission.USE_EXACT_ALARM]
 * permission. Instead it requests the user grant [android.Manifest.permission.SCHEDULE_EXACT_ALARM]
 * through the system settings screen, and gracefully degrades to inexact alarms
 * when access is not granted.
 */
object ExactAlarmPermission {

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            // Pre-Android 12 apps can always schedule exact alarms.
            true
        }
    }

    /** Intent to open the system screen where the user can grant exact-alarm access. */
    fun intentToRequestExactAlarmPermission(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        }
    }
}
