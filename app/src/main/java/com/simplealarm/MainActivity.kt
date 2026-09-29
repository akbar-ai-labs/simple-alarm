package com.simplealarm

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Calendar

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 13+ asks the user before showing notifications
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    val now = Calendar.getInstance()
                    val timeState = rememberTimePickerState(
                        initialHour = now.get(Calendar.HOUR_OF_DAY),
                        initialMinute = now.get(Calendar.MINUTE)
                    )
                    var message by remember { mutableStateOf("") }

                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Simple Alarm", style = MaterialTheme.typography.headlineMedium)
                        TimePicker(state = timeState, modifier = Modifier.padding(vertical = 24.dp))
                        Button(onClick = {
                            message = setAlarm(timeState.hour, timeState.minute)
                        }) {
                            Text("Set Alarm")
                        }
                        Text(message, modifier = Modifier.padding(top = 16.dp))
                    }
                }
            }
        }
    }

    private fun setAlarm(hour: Int, minute: Int): String {
        val alarmManager = getSystemService(AlarmManager::class.java)

        // Android 12 may require the user to allow exact alarms
        if (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms()) {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            return "Please allow alarms, then tap Set Alarm again"
        }

        // Next time the clock shows hour:minute (today, or tomorrow if already passed)
        val time = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_MONTH, 1)
        }

        val alarmIntent = PendingIntent.getBroadcast(
            this, 0, Intent(this, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val showIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(time.timeInMillis, showIntent), alarmIntent)

        return "Alarm set for %02d:%02d".format(hour, minute)
    }
}
