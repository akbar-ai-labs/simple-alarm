package com.simplealarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// AlarmManager calls this at the alarm time; it starts the ringing service.
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.startForegroundService(Intent(context, AlarmService::class.java))
    }
}
