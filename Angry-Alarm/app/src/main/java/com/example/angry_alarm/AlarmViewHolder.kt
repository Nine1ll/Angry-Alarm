package com.example.angry_alarm

import androidx.recyclerview.widget.RecyclerView
import com.example.angry_alarm.databinding.ItemAlarmBinding

class AlarmViewHolder(private val binding: ItemAlarmBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: MyElement) {
        // 아이템의 데이터를 뷰에 바인딩하는 로직 작성
        binding.alarmTime.text = item.getFormattedTime()
        binding.alarmDays.text = item.alarm_days
        binding.alarmTitle.text = item.title
        binding.alarmOnoff.isChecked = item.isSwitchOn
    }
}
