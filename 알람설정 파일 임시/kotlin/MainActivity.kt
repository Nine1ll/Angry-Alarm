package com.example.angryalarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment

import com.example.angryalarm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var alarmViewFragment: AlarmViewFragment
    private lateinit var alarmSetFragment: AlarmSetFragment
    private lateinit var messageFragment: MessageFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
}

