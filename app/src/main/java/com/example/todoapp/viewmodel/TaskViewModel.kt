package com.example.todoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.todoapp.alarm_manager.AlarmItem
import com.example.todoapp.database.TaskDatabase
import com.example.todoapp.models.TaskModel
import com.example.todoapp.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskViewModel(application: Application): AndroidViewModel(application) {

    val readAllData: LiveData<List<TaskModel>>
    private val repository: TaskRepository

    init {
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)
        readAllData = repository.readAllData
    }

    fun insertTask(task: TaskModel): LiveData<Long>{
        val idLiveData = MutableLiveData<Long>()
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.addUser(task)
            idLiveData.postValue(id)
        }
        return idLiveData
    }

    fun updateTask(task: TaskModel){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(task)
        }
    }

    fun deleteTask(task: TaskModel){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUser(task)
        }
    }

    fun deleteAllTasks(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllUsers()
        }
    }

    fun updateTaskStatus(taskId: Int, isDone: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTaskStatus(taskId, isDone)
        }
    }

    fun updateAlarmTime(taskId: Int, alarmItem: AlarmItem){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAlarmTime(taskId, alarmItem)
        }
    }
}