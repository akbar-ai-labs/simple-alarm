package com.simplealarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder

// Plays the default alarm sound and shows the alarm screen until STOP is pressed.
class AlarmService : Service() {

    private var player: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channel = NotificationChannel("alarm", "Alarm", NotificationManager.IMPORTANCE_HIGH)
        channel.setSound(null, null) // the sound is played below
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val screenIntent = Intent(this, AlarmActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val openScreen = PendingIntent.getActivity(this, 0, screenIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = Notification.Builder(this, "alarm")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarm Ringing")
            .setContentText("Tap to stop")
            .setCategory(Notification.CATEGORY_ALARM)
            .setOngoing(true)
            .setContentIntent(openScreen)
            .setFullScreenIntent(openScreen, true)
            .build()

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(1, notification)
        }

        if (player == null) startSound()

        // Opens the alarm screen directly when the app is on screen
        // (otherwise the full-screen notification above opens it).
        startActivity(screenIntent)
        return START_NOT_STICKY
    }

    private fun startSound() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: return
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
            )
            setDataSource(this@AlarmService, uri)
            isLooping = true
            prepare()
            start()
        }
    }

    override fun onDestroy() {
        player?.stop()
        player?.release()
        player = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
