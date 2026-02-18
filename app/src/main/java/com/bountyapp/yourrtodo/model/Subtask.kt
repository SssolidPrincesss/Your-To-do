package com.bountyapp.yourrtodo.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Subtask(
    val id: String,
    var title: String,
    var isCompleted: Boolean = false,
    val taskId: String
) : Parcelable