package com.example.todoapp.alarm_manager

interface AlarmScheduler {
    fun schedule(item: AlarmItem, taskId: Int)
    fun cancel(item: AlarmItem, taskId: Int)
}