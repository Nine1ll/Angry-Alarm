package com.example.angry_alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var alarmManager: AlarmManager? = null
    private var timePicker: TimePicker? = null
    private var pendingIntent: PendingIntent? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        timePicker = findViewById<TimePicker>(R.id.timePicker)
        findViewById<View>(R.id.btnStart).setOnClickListener(mClickListener)
        findViewById<View>(R.id.btnStop).setOnClickListener(mClickListener)
    }

    /* 알람 시작 */
    private fun start() {
        // 시간 설정
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = timePicker!!.hour
        calendar[Calendar.MINUTE] = timePicker!!.minute
        calendar[Calendar.SECOND] = 0

        // 현재시간보다 이전이면
        if (calendar.before(Calendar.getInstance())) {
            // 다음날로 설정
            calendar.add(Calendar.DATE, 1)
        }

        // Receiver 설정
        val intent = Intent(this, AlarmReceiver::class.java)
        // state 값이 on 이면 알람시작, off 이면 중지
        intent.putExtra("state", "on")
        pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        // 알람 설정
        alarmManager!![AlarmManager.RTC_WAKEUP, calendar.timeInMillis] = pendingIntent

        // Toast 보여주기 (알람 시간 표시)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        Toast.makeText(this, "Alarm : " + format.format(calendar.time), Toast.LENGTH_SHORT).show()
    }

    /* 알람 중지 */
    private fun stop() {
        if (pendingIntent == null) {
            return
        }

        // 알람 취소
        alarmManager!!.cancel(pendingIntent)

        // 알람 중지 Broadcast
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra("state", "off")
        sendBroadcast(intent)
        pendingIntent = null
    }

    var mClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.btnStart ->                     // 알람 시작
                start()
            R.id.btnStop ->                     // 알람 중지
                stop()
        }
    }
}