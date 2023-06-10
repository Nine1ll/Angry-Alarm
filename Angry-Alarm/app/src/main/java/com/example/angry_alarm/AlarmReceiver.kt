package com.example.angry_alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sIntent = Intent(context, AlarmService::class.java)
        sIntent.putExtra("state", intent.getStringExtra("state"))
        sIntent.putExtra("title", intent.getStringExtra("title"))
        sIntent.putExtra("alarmId", intent.getIntExtra("alarmId", 0))

        // Oreo(26) 버전 이후부터는 Background 에서 실행을 금지하기 때문에 Foreground 에서 실행해야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(sIntent)
        } else {
            context.startService(sIntent)
        }
    }
}