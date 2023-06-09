package com.example.angry_alarm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.angry_alarm.databinding.ItemAlarmBinding


class AlarmAdaptor(private var dataSet: MutableList<MyElement>) :
    RecyclerView.Adapter<AlarmAdaptor.MyViewHolder>() {

    private var onItemClickListener: ((Int) -> Unit)? = null

    private var OnLongClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnLongClickListener(listener: (Int) -> Unit) {
        OnLongClickListener = listener
    }



    fun getItemSelectionKey(position: Int) = dataSet.getOrNull(position)?.alarm_id

    override fun getItemCount(): Int = dataSet.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val element = dataSet[position]
        holder.bind(element)

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(position)
        }
        holder.itemView.setOnLongClickListener OnLongClickListener@{
            OnLongClickListener?.invoke(position)
            return@OnLongClickListener true
        }
    }

    fun setList(newList: MutableList<MyElement>) {
        dataSet = newList
        notifyDataSetChanged()
    }

    fun getElement(position: Int): MyElement = dataSet[position]

    inner class MyViewHolder(private val binding: ItemAlarmBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MyElement) {
            binding.alarmTime.text = item.getFormattedTime()
            binding.APM.text = item.getAMPM(item.hour)
            binding.alarmDays.text = item.alarm_days
            binding.alarmTitle.text = item.title
            binding.alarmOnoff.isChecked = item.isSwitchOn
        }
    }
}

