package com.example.angry_alarm

import com.example.angry_alarm.AlarmDatabase.MyDBContract.MyEntry.hour
import com.example.angry_alarm.AlarmDatabase.MyDBContract.MyEntry.minute

data class MyElement(
    val alarm_id: Int,
    val title: String,
    val hour: Int,
    val minute: Int,
    val alarm_days: String,
    val repeatCount: Int,
    val repeatInterval: Long,
    val isVibrator: Boolean,
    val isSwitchOn: Boolean
) {
    override fun toString(): String {
        return "MyElement(alarm_id=$alarm_id, title='$title', hour=$hour, minute=$minute, alarm_days='$alarm_days', repeatCount=$repeatCount, repeatInterval=$repeatInterval, isVibrator=$isVibrator, isSwitchOn=$isSwitchOn)"
    }

    fun getFormattedTime(): String {
        val hourFormatted = hour.toString().padStart(2, '0')
        val minuteFormatted = minute.toString().padStart(2, '0')
        return "$hourFormatted:$minuteFormatted"
    }

    fun getAMPM(hour: Int): String {
        return if (hour >= 12) "PM" else "AM"
    }
}
