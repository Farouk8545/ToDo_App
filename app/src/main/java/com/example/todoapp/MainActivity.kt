package com.example.todoapp

import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.adapters.MyAdapter
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.databinding.AddTaskDialogLayoutBinding
import com.example.todoapp.models.TaskModel
import com.example.todoapp.viewmodel.TaskViewModel
import android.view.animation.AnimationUtils
import android.Manifest
import android.app.TimePickerDialog
import androidx.core.app.ActivityCompat
import com.example.todoapp.alarm_manager.AlarmItem
import com.example.todoapp.alarm_manager.AndroidAlarmScheduler
import java.time.LocalDateTime
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: MyAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var scheduler: AndroidAlarmScheduler
    private var alarmItem: AlarmItem? = null
    private var isNotificationPermissionGranted: Boolean = false
    private val priorityMap = mapOf(
        "What ever" to R.color.Green,
        "If there is time" to R.color.Yellow,
        "When there is time" to R.color.Orange,
        "Urgent" to R.color.Red
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkNotificationPermission()

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setPadding(0, 0, 0, 0)
        toolbar.setContentInsetsAbsolute(0, 0)

        val layoutParams = androidx.appcompat.widget.Toolbar.LayoutParams(
            androidx.appcompat.widget.Toolbar.LayoutParams.MATCH_PARENT,
            androidx.appcompat.widget.Toolbar.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = android.view.Gravity.CENTER  // Center the custom view in the Toolbar

        val customActionBar = layoutInflater.inflate(R.layout.custom_action_bar_layout, null)
        toolbar.addView(customActionBar, layoutParams)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        adapter = MyAdapter(this, taskViewModel)
        binding.taskRv.adapter = adapter
        binding.taskRv.layoutManager = LinearLayoutManager(this)

        taskViewModel.readAllData.observe(this) { task ->
            adapter.setData(task)
        }

        binding.addButton.setOnClickListener {
            showAddDialog()
        }

    }
    private fun showAddDialog(){
        val dialogBinding = AddTaskDialogLayoutBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)

        var alarmTime: LocalDateTime? = null

        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_shape)

        val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.dialog_expand)
        dialogBinding.root.startAnimation(scaleAnimation)

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("What ever", "If there is time", "When there is time", "Urgent"))
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.prioritySpinner.adapter = spinnerAdapter

        dialogBinding.cancelButton.setOnClickListener {
            hideDialog(dialogBinding.root, dialog)
        }

        dialogBinding.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                val calendar = Calendar.getInstance()
                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = calendar.get(Calendar.MINUTE)

                val timeDialog = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                        dialogBinding.selectedTimeText.text = selectedTime
                        alarmTime = LocalDateTime.now()
                            .withHour(hourOfDay)
                            .withMinute(minute)
                    },
                    currentHour,
                    currentMinute,
                    true
                )
                timeDialog.setTitle("Set Reminder")

                timeDialog.show()
            }else{
                dialogBinding.reminderSwitch.isChecked = false
                dialogBinding.reminderText.text = "No Time Selected"
                alarmTime = null
            }
        }

        dialogBinding.prioritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                dialogBinding.spinnerCard.background = priorityMap[dialogBinding.prioritySpinner.selectedItem.toString()]?.let {
                    ContextCompat.getDrawable(binding.root.context, it)
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        dialogBinding.saveButton.setOnClickListener {
            val taskName = dialogBinding.taskNameEt.text.toString()
            val taskNotes = dialogBinding.notesTextEt.text.toString()
            val priority = dialogBinding.prioritySpinner.selectedItem.toString()

            if(taskName.isNotEmpty()){
                scheduler = AndroidAlarmScheduler(this)
                alarmItem = alarmTime?.let { it1 ->
                    AlarmItem(
                        it1,
                        taskName
                    )
                }
                val task = TaskModel(0, taskName, false, taskNotes, priority, alarmItem)
                taskViewModel.insertTask(task).observe(this) { taskId ->
                    if (isNotificationPermissionGranted && dialogBinding.reminderSwitch.isChecked) {
                        // Schedule the alarm using the auto-generated task ID
                        alarmItem?.let { scheduler.schedule(it, taskId.toInt()) }
                    }
                }
                hideDialog(dialogBinding.root, dialog)
            }else{
                Toast.makeText(this, "You forgot to tell what you're gonna do!", Toast.LENGTH_LONG).show()
            }
        }

        dialog.show()
    }

    private fun hideDialog(view: View, dialog: Dialog) {
        val scaleAnimationShrink = AnimationUtils.loadAnimation(this, R.anim.dialog_shrink)
        view.startAnimation(scaleAnimationShrink)
        scaleAnimationShrink.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                dialog.dismiss()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                isNotificationPermissionGranted = true
            } else {
                // Permission is not granted, request it
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            // For Android versions below 13, permission is automatically granted
            isNotificationPermissionGranted = true
        }
    }

    // Callback to handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty
                isNotificationPermissionGranted = if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted
                    true
                } else {
                    // Permission denied
                    false
                }
            }
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 101
    }
}