package com.example.angryalarm

import androidx.recyclerview.widget.RecyclerView
import com.example.angryalarm.databinding.ItemAlarmBinding

class AlarmViewHolder(private val binding: ItemAlarmBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: MyElement) {
        // 아이템의 데이터를 뷰에 바인딩하는 로직 작성
        binding.alarmTime.text = item.getFormattedTime()
        binding.APM.text = item.getAMPM(item.hour)
        binding.alarmDays.text = item.alarm_days
        binding.alarmTitle.text = item.title
        binding.alarmOnoff.isChecked = item.isSwitchOn
    }
}
