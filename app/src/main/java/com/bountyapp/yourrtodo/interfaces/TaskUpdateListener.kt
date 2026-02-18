package com.bountyapp.yourrtodo.interfaces

import com.bountyapp.yourrtodo.model.Task

interface TaskUpdateListener {
    fun onTaskUpdated(updatedTask: Task)
}