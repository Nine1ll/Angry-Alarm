package com.example.angry_alarm.textdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TextTable(
    val alarmId: Int,
    @PrimaryKey val message: String,
    val phoneNumber: String
)