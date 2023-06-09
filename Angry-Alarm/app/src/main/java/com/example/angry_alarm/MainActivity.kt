package com.example.angry_alarm

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import java.util.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.angry_alarm.databinding.ActivityMainBinding
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity(), Communicator, AlarmSetListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var alarmViewFragment: AlarmViewFragment
    private lateinit var alarmSetFragment: AlarmSetFragment
    private lateinit var messageFragment: MessageFragment

    private var alarmManager: AlarmManager? = null
    private var pendingIntent: PendingIntent? = null

    val calendar = Calendar.getInstance()
    var realarm = false
    var interval = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 알람매니저 서비스 호출
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        // 알람 권한 허용 (Android 13부터 알림 권한(POST_NOTIFICATIONS)을 허용해야 알람을 띄울 수 있음)
        TedPermission.create()
            .setPermissionListener(object: PermissionListener {
                override fun onPermissionGranted() {}
                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(applicationContext, "알림이 제한됩니다", Toast.LENGTH_SHORT).show()
                }
            })
            .setDeniedMessage("원활한 어플 이용을 위해 반드시 알림 권한을 허용해주세요. 거부 시 어플 기능이 제한됩니다.")
            .setPermissions(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.SEND_SMS)
            .check()

        // 프래그먼트 인스턴스 생성
        alarmViewFragment = AlarmViewFragment()
        alarmSetFragment = AlarmSetFragment()
        messageFragment = MessageFragment()

        // 초기에 보여줄 프래그먼트 설정
        showFragment(alarmViewFragment)

        // 버튼 클릭 이벤트 설정
        binding.list.setOnClickListener {
            showFragment(alarmViewFragment)
        }

        binding.setting.setOnClickListener {
            alarmSetFragment.setAlarmId(-1);
            showFragment(alarmSetFragment)
        }

        binding.massage.setOnClickListener {
            showFragment(messageFragment)
        }

        // FullScreenActivity에서 넘어왔는지 확인 (다시울림을 누른 경우)
        if (intent != null && savedInstanceState == null) {
            realarm = intent.getBooleanExtra("realarm", false)
            interval = intent.getIntExtra("interval", 0)
            if (interval != 0) {
                pendingIntent = intent.getParcelableExtra<PendingIntent>("pendingIntent")
                stop()          // 사용자가 다시 울림 버튼을 눌렀으므로 현재 알림은 stop
                start("다시울림", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
            }
        }
    }

    // AlarmSetFragment에서 저장 버튼 클릭 시, 알람매니저 호출
    override fun onAlarmSet(title: String, hour: Int, minute: Int) {
        start(title, hour, minute)
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentPlace, fragment)
            .commit()
    }

    fun onAlarmSelected(alarmId: Int) {
        // 설정 프래그먼트로 알람 ID 전달
        alarmSetFragment.dbHelper = AlarmDatabase.MyDbHelper(this)
        alarmSetFragment.setAlarmId(alarmId);
        // 프래그먼트 전환
        showFragment(alarmSetFragment)
        /*
         alarmSetFragment.loadAlarmFromDatabase(alarmId)*/
    }
    // MessageFragment에 알람 id를 제공해주는 함수
    override fun passDataCom(alarmId: Int) {
        val bundle = Bundle()
        bundle.putInt("alarmId",alarmId)
        val transaction = this.supportFragmentManager.beginTransaction()
        val messageFragment = MessageFragment()
        messageFragment.arguments = bundle

        //이게 지금 content_id가 activity_main.xml에 연결되어있다가 messageFragment로 연결함.
        transaction.replace(R.id.fragmentPlace, messageFragment)
        transaction.addToBackStack(null)
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.commit()
    }
    /* 알람 시작 */
    private fun start(
        title: String,
        hour: Int,
        minute: Int,
//        alarmDays: String,
//        repeatCount: Int,
//        repeatInterval: Int
    ) {
        val requestCode = System.currentTimeMillis().toInt()

        // 첫알림과 다시알림 구분하여 시간 설정
        if (!realarm) {
            // alarm DB 에서 저장된 시간 가져오기
            calendar[Calendar.HOUR_OF_DAY] = hour
            calendar[Calendar.MINUTE] = minute
            calendar[Calendar.SECOND] = 0
        } else {                // 다시 알람일 경우 현재 minute에 interval 더한 시간으로 수정
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
            PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        // 알람 설정
        alarmManager!![AlarmManager.RTC_WAKEUP, calendar.timeInMillis] = pendingIntent

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
}
interface Communicator {
    fun passDataCom(alarmId: Int)
}

