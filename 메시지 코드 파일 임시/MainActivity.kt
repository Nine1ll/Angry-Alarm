package com.example.angryalarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

import com.example.angryalarm.databinding.ActivityMainBinding
import kr.nine1ll.newtext.textdb.TextDaoDatabase

class MainActivity : AppCompatActivity(),Communicator {
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
        // 설정 프래그먼트 알람 ID 전달
        alarmSetFragment.dbHelper = AlarmDatabase.MyDbHelper(this)
        alarmSetFragment.loadAlarmFromDatabase(alarmId)

        // 프래그먼트 전환
        showFragment(alarmSetFragment)
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

}
interface Communicator {
    fun passDataCom(alarmId: Int)
}

