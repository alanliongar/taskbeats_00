package com.devspace.taskbeats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class TaskListAdapter :
    ListAdapter<TaskUiData, TaskListAdapter.TaskViewHolder>(TaskListAdapter) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category)
    }

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvCategory = view.findViewById<TextView>(R.id.tv_category_name)
        private val tvTask = view.findViewById<TextView>(R.id.tv_task_name)

        fun bind(task: TaskUiData) {
            tvCategory.text = task.category
            tvTask.text = task.name
        }
    }

    companion object : DiffUtil.ItemCallback<TaskUiData>() {
        override fun areItemsTheSame(oldItem: TaskUiData, newItem: TaskUiData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: TaskUiData, newItem: TaskUiData): Boolean {
            return oldItem.name == newItem.name
        }

    }


}