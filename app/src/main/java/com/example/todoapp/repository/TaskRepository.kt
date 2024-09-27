package com.example.todoapp.repository

import androidx.lifecycle.LiveData
import com.example.todoapp.alarm_manager.AlarmItem
import com.example.todoapp.dao.TaskDao
import com.example.todoapp.models.TaskModel

class TaskRepository(private val taskDao: TaskDao) {

    val readAllData: LiveData<List<TaskModel>> = taskDao.getAllTasks()

    suspend fun addUser(user: TaskModel): Long{
        return taskDao.insertTask(user)
    }

    suspend fun updateUser(user: TaskModel){
        taskDao.updateTask(user)
    }

    suspend fun deleteUser(user: TaskModel){
        taskDao.deleteTask(user)
    }

    suspend fun deleteAllUsers(){
        taskDao.deleteAllTasks()
    }

    suspend fun updateTaskStatus(taskId: Int, isDone: Boolean) {
        taskDao.updateTaskStatus(taskId, isDone)
    }

    suspend fun updateAlarmTime(taskId: Int, alarmItem: AlarmItem){
        taskDao.updateAlarmTime(taskId, alarmItem)
    }

}