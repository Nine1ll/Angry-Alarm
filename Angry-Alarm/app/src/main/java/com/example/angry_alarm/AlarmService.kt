package com.example.angry_alarm

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat


class AlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var isRunning = false

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val title = intent.getStringExtra("title")
        val alarmId = intent.getIntExtra("alarmId", 0)

        // Foreground 에서 실행되면 Notification 을 보여줘야 됨
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // fullscreen notificaition
            val fullScreenIntent = Intent(this, FullScreenActivity::class.java)
            fullScreenIntent.putExtra("title", title)
            fullScreenIntent.putExtra("alarmId", alarmId)
            val fullScreenPendingIntent = PendingIntent.getActivity (
                    this,
                    0,
                    fullScreenIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )

            // Oreo(26) 버전 이후 버전부터는 channel 이 필요함
            val channelId = createNotificationChannel()
            val builder = NotificationCompat.Builder(this, channelId)
            val notification: Notification = builder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText("Angry-Alarm을 통해 설정된 알람입니다.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(fullScreenPendingIntent)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .build()

            // fullscreen 추가
            notification.fullScreenIntent = fullScreenPendingIntent

            startForeground(1, notification)
        }
        val state = intent.getStringExtra("state")

        if (!isRunning && (state == "on")) {
            // 알람음 start
            mediaPlayer = MediaPlayer.create(this, R.raw.alarm)
            mediaPlayer!!.start()
            isRunning = true
            Log.d("AlarmService", "Alarm Start")
        } else if (isRunning and (state == "off")) {
            // 알람음 stop
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
            isRunning = false
            Log.d("AlarmService", "Alarm Stop")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                stopForeground(true)
            }
        }
        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = "Alarm"
        val channelName = getString(R.string.app_name)
        // 헤드업 알림을 위해 importace를 high로 설정
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel =
            NotificationChannel(channelId, channelName, importance)
        channel.setDescription(channelName)
        channel.setSound(null, null)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        return channelId
    }
}