package com.example.todoapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.todoapp.alarm_manager.AlarmItem
import java.time.LocalDateTime

@Entity(tableName = "tasks")
data class TaskModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val taskName: String,
    val isDone: Boolean,
    val notes: String?,
    val priority: String?,
    val alarmItem: AlarmItem?
)
