package com.bountyapp.yourrtodo.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.model.Subtask

class SubtaskAdapter(
    private var subtasks: MutableList<Subtask>,
    private val onSubtaskChecked: (Subtask) -> Unit,
    private val onSubtaskClick: (Subtask) -> Unit,
    private val onDeleteClick: (Subtask) -> Unit   // новый параметр
) : RecyclerView.Adapter<SubtaskAdapter.SubtaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subtask, parent, false)
        return SubtaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubtaskViewHolder, position: Int) {
        holder.bind(subtasks[position])
    }

    override fun getItemCount(): Int = subtasks.size

    fun updateSubtasks(newSubtasks: MutableList<Subtask>) {
        subtasks = newSubtasks
        notifyDataSetChanged()
    }

    fun addSubtask(subtask: Subtask) {
        subtasks.add(subtask)
        notifyItemInserted(subtasks.size - 1)
    }

    fun removeSubtask(subtask: Subtask) {
        val index = subtasks.indexOfFirst { it.id == subtask.id }
        if (index != -1) {
            subtasks.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    inner class SubtaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkbox: CheckBox = itemView.findViewById(R.id.subtask_checkbox)
        private val title: TextView = itemView.findViewById(R.id.subtask_title)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_subtask)

        init {
            checkbox.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val subtask = subtasks[position]
                    subtask.isCompleted = checkbox.isChecked
                    onSubtaskChecked(subtask)
                    updateTextStyle(subtask.isCompleted)
                }
            }

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSubtaskClick(subtasks[position])
                }
            }

            btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(subtasks[position])
                }
            }
        }

        fun bind(subtask: Subtask) {
            title.text = subtask.title
            checkbox.isChecked = subtask.isCompleted
            updateTextStyle(subtask.isCompleted)
        }

        private fun updateTextStyle(isCompleted: Boolean) {
            if (isCompleted) {
                title.paintFlags = title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                title.alpha = 0.6f
            } else {
                title.paintFlags = title.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                title.alpha = 1.0f
            }
        }
    }
}