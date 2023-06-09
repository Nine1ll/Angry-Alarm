package com.example.angry_alarm

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.angry_alarm.databinding.FragmentAlarmSetBinding
import java.util.Calendar

class AlarmSetFragment : Fragment(), AlarmSetListener {
    private var alarmSet: AlarmSetListener? = null
    private var binding: FragmentAlarmSetBinding? = null
    lateinit var dbHelper: AlarmDatabase.MyDbHelper
    private var alarm: MyElement? = null
    private var alarmId: Int? = -1;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlarmSetBinding.inflate(inflater, container, false)
        val view = binding?.root

        return view ?: throw IllegalStateException("View binding not available")
    }

    override fun onStart() {
        super.onStart()
        if(alarmId != -1){
            loadAlarmFromDatabase(alarmId);
        }else{
            clearAlarmInfo();

            binding?.timePicker?.hour = Calendar.getInstance().get(Calendar.HOUR)
            binding?.timePicker?.minute = Calendar.getInstance().get(Calendar.MINUTE)

            alarm = null;
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = AlarmDatabase.MyDbHelper(requireContext())

        binding?.scheduleAlarm?.setOnClickListener {
            if (alarm == null) {
                // 새로운 알람 저장
                saveAlarmToDatabase()
            } else {
                // 기존 알람 수정
                updateAlarmInDatabase()
            }
        }
    }

    // fragment가 activity에 붙을 때 activity가 MyClickListener를 구현하고 있는지 확인
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 구현되어 있다면 clickListener 변수를 설정
        if (context is AlarmSetListener) {
            alarmSet = context
        } else {
            throw IllegalArgumentException("Activity must implement MyClickListener")
        }
    }

    // clickListener 변수를 해제
    override fun onDetach() {
        super.onDetach()
        alarmSet = null
    }

    override fun onAlarmSet(
        title: String,
        hour: Int,
        minute: Int,
//        alarmDays: String,
//        repeatCount: Int,
//        repeatInterval: Int
    ) {
        (activity as? AlarmSetListener)?.onAlarmSet(title, hour, minute)
    }

    fun setAlarmId(alarmId: Int?){
        this.alarmId = alarmId;
    }

    fun loadAlarmFromDatabase(alarmId: Int?) {
        binding?.scheduleAlarm?.text = "변경"
        // 기존 알람 수정 모드
        val db = dbHelper.readableDatabase
        val myentry = AlarmDatabase.MyDBContract.MyEntry
        val selection = "${myentry.alarm_id} = ?"
        val selectionArgs = arrayOf(alarmId.toString())
        val cursor = db.query(
            myentry.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        cursor.moveToFirst()
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(myentry.alarm_id))
        val title = cursor.getString(cursor.getColumnIndexOrThrow(myentry.title))
        val hour = cursor.getInt(cursor.getColumnIndexOrThrow(myentry.hour))
        val minute = cursor.getInt(cursor.getColumnIndexOrThrow(myentry.minute))
        val alarmDays = cursor.getString(cursor.getColumnIndexOrThrow(myentry.alarm_days))
        val repeatCount = cursor.getInt(cursor.getColumnIndexOrThrow(myentry.repeat_count))
        val repeatInterval = cursor.getLong(cursor.getColumnIndexOrThrow(myentry.repeat_interval))
        val isVibrator = cursor.getInt(cursor.getColumnIndexOrThrow(myentry.isVibrator)) == 1
        val isSwitchOn = cursor.getInt(cursor.getColumnIndexOrThrow(myentry.isSwitchOn)) == 1
        cursor.close()
        db.close()

        alarm = MyElement(
            id,
            title,
            hour,
            minute,
            alarmDays,
            repeatCount,
            repeatInterval,
            isVibrator,
            isSwitchOn
        )

        // 알람 정보 설정
        binding?.alarmTitle?.setText(title)
        binding?.timePicker?.hour = hour
        binding?.timePicker?.minute = minute
        setSelectedDays(alarmDays)
        binding?.repeatCount?.setText(repeatCount.toString())
        binding?.interval?.setText(repeatInterval.toString())

        // 변경 버튼으로 텍스트 변경
        binding?.scheduleAlarm?.setOnClickListener {
            updateAlarmInDatabase()
        }
    }

    private fun saveAlarmToDatabase() {
        val binding = binding ?: return

        val title = binding.alarmTitle.text.toString()
        val hour = binding.timePicker.hour
        val minute = binding.timePicker.minute
        val alarmDays = getSelectedDays()
        val repeatCount = binding.repeatCount.text.toString() // Repeat count 초기값 설정
        val repeatInterval = binding.interval.text.toString()// Repeat interval 초기값 설정

        // 데이터베이스에 알람 정보 저장
        val db = dbHelper.writableDatabase
        val myentry = AlarmDatabase.MyDBContract.MyEntry
        val values = ContentValues().apply {
            put(myentry.title, title)
            put(myentry.hour, hour)
            put(myentry.minute, minute)
            put(myentry.alarm_days, alarmDays)
            put(myentry.repeat_count, repeatCount.toInt())
            put(myentry.repeat_interval, repeatInterval.toInt())
            put(myentry.isVibrator, 0) // 진동 설정 여부 저장
            put(myentry.isSwitchOn, 0) // 스위치 상태 저장
        }
        val newRowId = db.insert(myentry.TABLE_NAME, null, values)
        db.close()

        onAlarmSet(title, hour, minute)

        // 알람 저장 완료 메시지 출력
        Toast.makeText(requireContext(), "알람이 저장되었습니다.", Toast.LENGTH_SHORT).show()
        clearAlarmInfo()
    }

    private fun updateAlarmInDatabase() {
        val binding = binding ?: return
        val alarmToUpdate = alarm ?: return

        val title = binding.alarmTitle.text.toString()
        val hour = binding.timePicker.hour
        val minute = binding.timePicker.minute
        val alarmDays = getSelectedDays()

        // 데이터베이스에서 알람 정보 업데이트
        val db = dbHelper.writableDatabase
        val myentry = AlarmDatabase.MyDBContract.MyEntry
        val values = ContentValues().apply {
            put(myentry.title, title)
            put(myentry.hour, hour)
            put(myentry.minute, minute)
            put(myentry.alarm_days, alarmDays)
        }
        val selection = "${myentry.alarm_id} = ?"
        val selectionArgs = arrayOf(alarmToUpdate.alarm_id.toString())
        db.update(myentry.TABLE_NAME, values, selection, selectionArgs)
        db.close()

        // 기존 알람 취소

        // 알림 변경 완료 메시지 출력
        Toast.makeText(requireContext(), "알람이 변경되었습니다.", Toast.LENGTH_SHORT).show()

        clearAlarmInfo()
    }

    private fun getSelectedDays(): String {
        val binding = binding ?: return "" // Null 체크

        val selectedDays = mutableListOf<String>()
        if (binding.Mon.isChecked) selectedDays.add("Mon")
        if (binding.Tue.isChecked) selectedDays.add("Tue")
        if (binding.Wed.isChecked) selectedDays.add("Wed")
        if (binding.Thu.isChecked) selectedDays.add("Thu")
        if (binding.Fri.isChecked) selectedDays.add("Fri")
        if (binding.Sat.isChecked) selectedDays.add("Sat")
        if (binding.kSun.isChecked) selectedDays.add("Sun")

        return selectedDays.joinToString(", ")
    }

    private fun setSelectedDays(days: String) {
        val binding = binding ?: return // Null 체크

        val selectedDays = days.split(", ")
        binding.Mon.isChecked = selectedDays.contains("Mon")
        binding.Tue.isChecked = selectedDays.contains("Tue")
        binding.Wed.isChecked = selectedDays.contains("Wed")
        binding.Thu.isChecked = selectedDays.contains("Thu")
        binding.Fri.isChecked = selectedDays.contains("Fri")
        binding.Sat.isChecked = selectedDays.contains("Sat")
        binding.kSun.isChecked = selectedDays.contains("Sun")
    }

    private fun clearAlarmInfo() {
        // 알람 정보 초기화
        binding?.alarmTitle?.text?.clear()
        binding?.timePicker?.hour = 0
        binding?.timePicker?.minute = 0
        setSelectedDays("")
        binding?.repeatCount?.text?.clear()
        binding?.interval?.text?.clear()
        binding?.scheduleAlarm?.text = "저장" // 버튼 텍스트 변경
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}


