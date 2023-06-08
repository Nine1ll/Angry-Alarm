package com.example.angryalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

class AlarmManager(private val context: Context) {
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun setupAlarm(alarm: MyElement) {
        // 알람 시간 설정
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
        }

        // 알람을 실행할 액티비티 인텐트 생성
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("alarmId", alarm.alarm_id)
        val pendingIntent = PendingIntent.getBroadcast(context, alarm.alarm_id, intent, 0)

        // 기존에 등록된 알람 취소
        alarmManager.cancel(pendingIntent)

        // 알람 설정
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        // 알람 설정 시간 로그 출력
        Log.d("TAG", "알람이 ${alarm.getFormattedTime()}에 설정되었습니다.")
    }

    fun cancelAlarm(alarmId: Int) {
        // 알람을 실행할 액티비티 인텐트 생성
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, 0)

        // 기존에 등록된 알람 취소
        alarmManager.cancel(pendingIntent)

        // 알람 취소 로그 출력
        Log.d("TAG", "알람이 취소되었습니다.")
    }
}
