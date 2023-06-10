package com.example.angry_alarm

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.example.angry_alarm.databinding.AlarmRunBinding
import java.util.*


class FullScreenActivity : Activity() {
    lateinit var binding : AlarmRunBinding
    private var alarmManager: AlarmManager? = null
    private var pendingIntent: PendingIntent? = null

    val calendar = Calendar.getInstance()
    var interval = 5

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1 // 월은 0부터 시작하므로 1을 더함
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = AlarmRunBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root) // fullscreen.xml 파일의 레이아웃을 설정
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val title = intent?.getStringExtra("title")

        // 화면 꺼져 있을 경우 잠금화면 깨우기
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        binding.timeView.text = String.format("%02d:%02d", hour, minute)
        binding.dateView.text = String.format("%d년 %02d월 %02d일", year, month, day)
        binding.memoView.text = title    ////////요기 알람 타이틀로 뜨게 수정
        binding.minusBtn.setOnClickListener {
            if (interval <= 1) {
                Toast.makeText(applicationContext, "1분 이상 설정 가능합니다", Toast.LENGTH_SHORT).show()
            } else {
                interval--
                binding.realarmBtn.text = "${interval}분 후 다시알림"
            }
        }
        binding.plusBtn.setOnClickListener {
            if (interval >= 60) {
                Toast.makeText(applicationContext, "60분 이하 설정 가능합니다", Toast.LENGTH_SHORT).show()
            }
            interval++
            binding.realarmBtn.text = "${interval}분 후 다시알림"
        }
        // 알람 및 소리 off
        binding.offBtn.setOnClickListener {
            val stopIntent = Intent(this, AlarmService::class.java)
            val intent = Intent(this, MainActivity::class.java)
            stopIntent.putExtra("state", "off")
            startService(stopIntent)

            Toast.makeText(applicationContext, "알람이 종료되었습니다", Toast.LENGTH_SHORT).show()

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            //moveTaskToBack(true) // 태스크를 백그라운드로 이동
        }
        // 다시알림 설정
        binding.realarmBtn.setOnClickListener {
            Toast.makeText(applicationContext, "${interval}분 후 다시알림이 설정되었습니다", Toast.LENGTH_SHORT).show()
            realarm(interval)
        }
    }

    private fun realarm(interval: Int) {
        // stop을 위한 pendingIntent 값 설정
        val intent = Intent(this, AlarmReceiver::class.java)
        pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        alarmManager!!.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        val reintent = Intent(this, MainActivity::class.java)

        // MainActivity로 전달
        reintent.putExtra("realarm", true)
        reintent.putExtra("interval", interval)
        reintent.putExtra("pendingIntent", pendingIntent)
        startActivity(reintent)
    }
}