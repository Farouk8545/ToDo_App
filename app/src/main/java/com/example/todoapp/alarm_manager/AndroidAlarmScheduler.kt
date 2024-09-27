package com.example.todoapp.alarm_manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDateTime
import java.time.ZoneId

class AndroidAlarmScheduler(private val context: Context): AlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(item: AlarmItem, taskId: Int) {

        val currentDateTime = LocalDateTime.now()
        val alarmTime = currentDateTime.withHour(item.time.hour).withMinute(item.time.minute)

        val pendingIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("message", item.message)
        }

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            alarmTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000,
            PendingIntent.getBroadcast(
                context,
                taskId,
                pendingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    override fun cancel(item: AlarmItem, taskId: Int) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                taskId,
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}