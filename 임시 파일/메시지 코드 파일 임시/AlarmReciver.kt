package com.example.angryalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 알람이 울릴 때 실행되는 로직을 여기에 작성합니다.
        // 이 예시에서는 알람이 울릴 때 Toast 메시지를 표시하고 기본 알람 소리를 재생합니다.

        // 알람 ID 가져오기
        val alarmId = intent.getIntExtra("alarmId", -1)

        // 알람 ID를 사용하여 추가적인 로직 수행

        // Toast 메시지 표시
        Toast.makeText(context, "알람이 울립니다!", Toast.LENGTH_SHORT).show()

        // 기본 알람 소리 재생
        val ringtone: Ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            .let { RingtoneManager.getRingtone(context, it) }
        ringtone.play()

        // 알람 울린 후 추가적인 작업을 수행할 수 있습니다.
        // 예를 들어, Notification 생성, 다른 액티비티 실행 등의 작업을 수행할 수 있습니다.
    }
}
