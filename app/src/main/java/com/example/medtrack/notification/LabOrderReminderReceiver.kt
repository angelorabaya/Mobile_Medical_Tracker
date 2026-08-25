package com.example.medtrack.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class LabOrderReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val orderId = intent.getIntExtra("order_id", 0)
        val testName = intent.getStringExtra("test_name") ?: "Lab Test"
        val scheduledDate = intent.getStringExtra("scheduled_date") ?: ""
        val scheduledTime = intent.getStringExtra("scheduled_time") ?: ""
        val facilityName = intent.getStringExtra("facility_name") ?: ""
        val fastingInstructions = intent.getStringExtra("fasting_instructions") ?: ""

        NotificationHelper.showLabOrderReminderNotification(
            context = context,
            orderId = orderId,
            testName = testName,
            scheduledDate = scheduledDate,
            scheduledTime = scheduledTime,
            facilityName = facilityName,
            fastingInstructions = fastingInstructions
        )
    }
}
