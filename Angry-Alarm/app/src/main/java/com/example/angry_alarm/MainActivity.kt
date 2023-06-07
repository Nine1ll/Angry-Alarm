package com.example.angry_alarm

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var alarmManager: AlarmManager? = null
    private var timePicker: TimePicker? = null
    private var pendingIntent: PendingIntent? = null

    var realarm = false
    var interval = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        timePicker = findViewById<TimePicker>(R.id.timePicker)
        findViewById<View>(R.id.btnStart).setOnClickListener(mClickListener)
        findViewById<View>(R.id.btnStop).setOnClickListener(mClickListener)

        // 알람 권한 허용 (Android 13부터 알림 권한(POST_NOTIFICATIONS)을 허용해야 알람을 띄울 수 있음)
        TedPermission.create()
            .setPermissionListener(object: PermissionListener {
            override fun onPermissionGranted() {
                Toast.makeText(
                    this@MainActivity,
                    "알림을 허용합니다",
                    Toast.LENGTH_SHORT).show()
            }
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                Toast.makeText(
                    this@MainActivity,
                    "알림이 제한됩니다",
                    Toast.LENGTH_SHORT). show()
            }
        })
        .setDeniedMessage("원활한 어플 이용을 위해 반드시 알림 권한을 허용해주세요. 거부 시 어플 기능이 제한됩니다.")
        .setPermissions(Manifest.permission.POST_NOTIFICATIONS)
        .check()

        // FullScreenActivity에서 넘어왔는지 확인
        if (intent != null && savedInstanceState == null) {
            realarm = intent.getBooleanExtra("realarm", false)
            interval = intent.getIntExtra("interval", 0)
            if (interval != 0) {
                pendingIntent = intent.getParcelableExtra<PendingIntent>("pendingIntent")
                stop()          // 사용자가 다시 울림 버튼을 눌렀으므로 현재 알림은 stop
                start()
            }
        }
    }

    /* 알람 시작 */
    private fun start() {
        val calendar = Calendar.getInstance()

        // 첫알림과 다시알림 구분하여 시간 설정
        if (!realarm) {
            calendar[Calendar.HOUR_OF_DAY] = timePicker!!.hour
            calendar[Calendar.MINUTE] = timePicker!!.minute
            calendar[Calendar.SECOND] = 0
        } else {                // 다시 알람일 경우 minute에 interval 더한 시간으로 수정
            calendar[Calendar.HOUR_OF_DAY] = calendar.get(Calendar.HOUR_OF_DAY)
            calendar[Calendar.MINUTE] = calendar.get(Calendar.MINUTE) + interval
            calendar[Calendar.SECOND] = 0
        }

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