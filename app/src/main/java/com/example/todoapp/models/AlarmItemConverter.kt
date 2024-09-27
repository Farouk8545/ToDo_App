package com.example.todoapp.models

import androidx.room.TypeConverter
import com.example.todoapp.alarm_manager.AlarmItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AlarmItemConverter {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromAlarmItem(alarmItem: AlarmItem?): String? {
        return alarmItem?.let {
            "${it.time.format(formatter)},${it.message}"
        }
    }

    @TypeConverter
    fun toAlarmItem(data: String?): AlarmItem? {
        return data?.let {
            val parts = it.split(",")
            AlarmItem(LocalDateTime.parse(parts[0], formatter), parts[1])
        }
    }
}