package com.example.angry_alarm

import android.app.Activity
import android.os.Bundle
import com.example.angry_alarm.R

class FullScreenActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_run) // fullscreen.xml 파일의 레이아웃을 설정합니다.
    }
}
