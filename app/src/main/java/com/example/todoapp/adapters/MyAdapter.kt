package com.example.todoapp.adapters

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.alarm_manager.AlarmItem
import com.example.todoapp.alarm_manager.AndroidAlarmScheduler
import com.example.todoapp.databinding.TaskLayoutBinding
import com.example.todoapp.databinding.UpdateTaskDialogLayoutBinding
import com.example.todoapp.models.TaskModel
import com.example.todoapp.viewmodel.TaskViewModel
import java.time.LocalDateTime
import java.util.Calendar

class MyAdapter(private val context: Context, private val taskViewModel: TaskViewModel): RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
    private var myList: List<TaskModel> = emptyList()
    private lateinit var scheduler: AndroidAlarmScheduler
    private var alarmItem: AlarmItem? = null
    private val priorityMap = mapOf(
        "What ever" to R.color.Green,
        "If there is time" to R.color.Yellow,
        "When there is time" to R.color.Orange,
        "Urgent" to R.color.Red
    )

    inner class MyViewHolder(private val binding: TaskLayoutBinding): RecyclerView.ViewHolder(binding.root){
        fun bindData(task: TaskModel, holder: MyViewHolder){
            binding.taskName.text = task.taskName
            binding.priorityText.text = task.priority
            binding.notesText.text = task.notes
            binding.doneButton.isChecked = task.isDone
            binding.reminderTimeText.text = if(task.alarmItem != null) String.format("Reminder At: %02d:%02d", task.alarmItem?.time?.hour, task.alarmItem?.time?.minute) else "No Reminder Setted"

            if(binding.notesText.text.isEmpty()) binding.dropDownUpButton.visibility = View.GONE else binding.dropDownUpButton.visibility = View.VISIBLE

            val backgroundDrawable = ContextCompat.getDrawable(binding.root.context, R.drawable.rounded_card)?.mutate() as? GradientDrawable
            backgroundDrawable?.setColor(ContextCompat.getColor(binding.root.context, priorityMap[binding.priorityText.text.toString()] ?: R.color.white))
            binding.priorityField.background = backgroundDrawable

            binding.doneButton.setOnClickListener{
                taskViewModel.updateTaskStatus(task.id, binding.doneButton.isChecked)
                if (binding.doneButton.isChecked && task.alarmItem != null) {
                    scheduler = AndroidAlarmScheduler(context)
                    scheduler.cancel(task.alarmItem, task.id)
                    taskViewModel.updateTask(TaskModel(task.id, task.taskName, task.isDone, task.notes, task.priority, null))
                }
            }

            binding.dropDownUpButton.setOnClickListener {
                if(binding.notesLayout.visibility == View.GONE){
                    binding.dropDownUpButton.setImageResource(R.drawable.baseline_arrow_drop_up_24)
                    val dropDownAnimation = AnimationUtils.loadAnimation(context, R.anim.notes_expand)
                    binding.notesLayout.startAnimation(dropDownAnimation)
                    dropDownAnimation.setAnimationListener(object : AnimationListener{
                        override fun onAnimationStart(p0: Animation?) {
                            binding.notesLayout.visibility = View.VISIBLE
                        }

                        override fun onAnimationEnd(p0: Animation?) {}

                        override fun onAnimationRepeat(p0: Animation?) {}

                    })
                }else if(binding.notesLayout.visibility == View.VISIBLE){
                    binding.dropDownUpButton.setImageResource(R.drawable.baseline_arrow_drop_down_24)
                    val dropUpAnimation = AnimationUtils.loadAnimation(context, R.anim.notes_shrink)
                    binding.notesLayout.startAnimation(dropUpAnimation)
                    dropUpAnimation.setAnimationListener(object : AnimationListener{
                        override fun onAnimationStart(p0: Animation?) {}

                        override fun onAnimationEnd(p0: Animation?) {
                            binding.notesLayout.visibility = View.GONE
                        }

                        override fun onAnimationRepeat(p0: Animation?) {}

                    })
                }
            }

            binding.editButton.setOnClickListener {
                showUpdateDialog(task)
            }

            binding.deleteButton.setOnClickListener {
                val builder = AlertDialog.Builder(binding.root.context)
                builder.setPositiveButton("Yes"){_,_ ->
                    val deleteAnimation = AnimationUtils.loadAnimation(context, R.anim.task_delete)
                    holder.itemView.startAnimation(deleteAnimation)
                    deleteAnimation.setAnimationListener(object : AnimationListener{
                        override fun onAnimationStart(p0: Animation?) {}

                        override fun onAnimationEnd(p0: Animation?) {
                            taskViewModel.deleteTask(task)
                        }

                        override fun onAnimationRepeat(p0: Animation?) {}

                    })
                }
                builder.setNegativeButton("No"){_,_ -> }
                builder.setTitle("Delete this task?")
                builder.setMessage("Are you sure you want to delete this task?")
                builder.create().show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = TaskLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = myList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(myList[position], holder)
    }

    fun setData(newList: List<TaskModel>){
        myList = newList
        notifyDataSetChanged()
    }

    private fun showUpdateDialog(task: TaskModel){
        val layoutInflater = LayoutInflater.from(context)
        val dialogBinding = UpdateTaskDialogLayoutBinding.inflate(layoutInflater)
        val dialog = Dialog(dialogBinding.root.context)
        dialog.setContentView(dialogBinding.root)

        var alarmTime: LocalDateTime? = null
        var timeChanged = false

        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_shape)

        val scaleExpandAnimation = AnimationUtils.loadAnimation(context, R.anim.dialog_expand)
        dialogBinding.root.startAnimation(scaleExpandAnimation)

        val spinnerAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, listOf("What ever", "If there is time", "When there is time", "Urgent"))
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.prioritySpinner.adapter = spinnerAdapter

        dialogBinding.taskNameEt.setText(task.taskName)
        dialogBinding.notesTextEt.setText(task.notes)
        dialogBinding.prioritySpinner.setSelection(spinnerAdapter.getPosition(task.priority))
        dialogBinding.selectedTimeText.text = if(task.alarmItem != null) String.format("%02d:%02d", task.alarmItem?.time?.hour, task.alarmItem?.time?.minute) else "No Time Selected"
        dialogBinding.reminderSwitch.isChecked = task.alarmItem != null

        dialogBinding.cancelButton.setOnClickListener {
            hideDialog(dialogBinding.root, dialog)
        }

        dialogBinding.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                val calendar = Calendar.getInstance()
                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = calendar.get(Calendar.MINUTE)

                val timeDialog = TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                        dialogBinding.selectedTimeText.text = selectedTime
                        alarmTime = LocalDateTime.now()
                            .withHour(hourOfDay)
                            .withMinute(minute)
                        timeChanged = true
                    },
                    currentHour,
                    currentMinute,
                    true
                )
                timeDialog.setTitle("Set Reminder")

                timeDialog.show()
            }
        }

        dialogBinding.prioritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                dialogBinding.spinnerCard.background = priorityMap[dialogBinding.prioritySpinner.selectedItem.toString()]?.let {
                    ContextCompat.getDrawable(context, it)
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        dialogBinding.updateButton.setOnClickListener {
            val updatedTaskName = dialogBinding.taskNameEt.text.toString().trim()
            val updatedNotes = dialogBinding.notesTextEt.text.toString().trim()
            val updatedPriority = dialogBinding.prioritySpinner.selectedItem.toString()
            if(updatedTaskName.isNotEmpty()){

                if(timeChanged || dialogBinding.reminderSwitch.isChecked){
                    scheduler = AndroidAlarmScheduler(context)
                    alarmItem = alarmTime?.let { it1 ->
                        AlarmItem(
                            it1,
                            dialogBinding.taskNameEt.text.toString()
                        )
                    }
                    taskViewModel.updateAlarmTime(task.id, alarmItem!!)
                }
                val updatedTask = TaskModel(task.id, updatedTaskName, false, updatedNotes, updatedPriority, task.alarmItem)
                taskViewModel.updateTask(updatedTask)
                scheduler = AndroidAlarmScheduler(context)
                task.alarmItem?.let { it1 -> scheduler.schedule(it1, task.id) }

                if(!dialogBinding.reminderSwitch.isChecked){
                    scheduler = AndroidAlarmScheduler(context)
                    task.alarmItem?.let { it1 -> scheduler.cancel(it1, task.id) }
                    taskViewModel.updateTask(TaskModel(task.id, task.taskName, task.isDone, task.notes, task.priority, null))
                }

                hideDialog(dialogBinding.root, dialog)
            }else{
                Toast.makeText(context, "You forgot to tell what you're gonna do!", Toast.LENGTH_LONG).show()
            }
        }

        dialog.show()
    }

    private fun hideDialog(view: View, dialog: Dialog) {
        val scaleAnimationShrink = AnimationUtils.loadAnimation(context, R.anim.dialog_shrink)
        view.startAnimation(scaleAnimationShrink)
        scaleAnimationShrink.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                dialog.dismiss()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
}