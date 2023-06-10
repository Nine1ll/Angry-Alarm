package com.example.angry_alarm

// AlarmSetFragment에서 MainActivity로 클릭 이벤트를 전달하기 위한 인터페이스
interface AlarmSetListener {
    fun onAlarmSet(
        alarmId: Int,
        title: String,
        hour: Int,
        minute: Int,
    )
}