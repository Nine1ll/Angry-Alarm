package com.example.angry_alarm

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.example.angry_alarm.databinding.AlarmRunBinding
import com.example.angry_alarm.textdb.TextDaoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class FullScreenActivity : Activity() {
    lateinit var binding : AlarmRunBinding
    private var alarmManager: AlarmManager? = null
    private var pendingIntent: PendingIntent? = null
    //데이터 베이스
    private lateinit var db: TextDaoDatabase
    lateinit var dbHelper: AlarmDatabase.MyDbHelper

    private var title: String? = null
    private var alarmId: Int = 0

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

        title = intent?.getStringExtra("title")
        alarmId = intent!!.getIntExtra("alarmId", 0)
        Log.d("alarmId","$alarmId")
        // 화면 꺼져 있을 경우 잠금화면 깨우기
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        binding.timeView.text = String.format("%02d:%02d", hour, minute)
        binding.dateView.text = String.format("%d년 %02d월 %02d일", year, month, day)
        binding.memoView.text = title
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
            if (title != null && alarmId != null) {
                //realarm(interval, title, alarmId)
                realarm(interval)
            }
        }

        sendMessage(alarmId)
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
        reintent.putExtra("title", title)
        reintent.putExtra("alarmId", alarmId)
        reintent.putExtra("pendingIntent", pendingIntent)
        startActivity(reintent)
    }
    private fun sendMessage(alarmId: Int){
        dbHelper = AlarmDatabase.MyDbHelper(applicationContext)
        val alarmDB = dbHelper.readableDatabase

        val myEntry = AlarmDatabase.MyDBContract.MyEntry
        val selection = "${myEntry.alarm_id} = ?"
        val selectionArgs = arrayOf(alarmId.toString())
        val cursor = alarmDB.query(myEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null)

        cursor.moveToFirst()
        var repeatCount = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmDatabase.MyDBContract.MyEntry.repeat_count))
        Log.d("sendMessage","$repeatCount")
        cursor.close()
        if (repeatCount > 0) {
            repeatCount-- // repeat_count 값을 1 감소시킵니다.

            val contentValues = ContentValues()
            contentValues.put(AlarmDatabase.MyDBContract.MyEntry.repeat_count, repeatCount)

            val affectedRows = alarmDB.update(myEntry.TABLE_NAME, contentValues, selection, selectionArgs)
            if (affectedRows > 0) {
                Log.d("sendMessage", "repeat_count 업데이트 완료")
            } else {
                Log.d("sendMessage", "repeat_count 업데이트 실패")
            }
        }
        alarmDB.close()

        db = TextDaoDatabase.getDatabase(applicationContext)!!
        if (repeatCount <= 0){
            CoroutineScope(Dispatchers.Main).launch {
                var getList =
                    withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                        db.textDAO().selectByAlarmId(alarmId)
                    }
                withContext(Dispatchers.Main) {
                    for (element in getList){
                        val pNumber = element.phoneNumber
                        val sms = element.message
                        try {
                            val smsManager: SmsManager = SmsManager.getDefault()
                            smsManager.sendTextMessage(pNumber, null, sms, null, null)
                            Log.d("전송 완료","$sms")
                        } catch (e: Exception) {
                            Log.d("실패","$e")
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}