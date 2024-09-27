package com.example.todoapp.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.todoapp.alarm_manager.AlarmItem
import com.example.todoapp.models.TaskModel

@Dao
interface TaskDao {

    @Insert
    suspend fun insertTask(task: TaskModel): Long

    @Update
    suspend fun updateTask(task: TaskModel)

    @Delete
    suspend fun deleteTask(task: TaskModel)

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): LiveData<List<TaskModel>>

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("UPDATE tasks SET isDone = :isDone WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Int, isDone: Boolean)

    @Query("UPDATE tasks SET alarmItem = :alarmItem WHERE id = :taskId")
    suspend fun updateAlarmTime(taskId: Int, alarmItem: AlarmItem)

}