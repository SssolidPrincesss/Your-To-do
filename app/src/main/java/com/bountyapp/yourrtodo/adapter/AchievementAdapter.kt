package com.bountyapp.yourrtodo.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.data.entities.AchievementEntity
import java.text.SimpleDateFormat
import java.util.*

class AchievementAdapter(
    private val context: Context,
    private val achievements: List<AchievementEntity>,
    private val progressMap: Map<Long, Int>? = null,
    private val onItemClick: ((AchievementEntity) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_LOCKED = 0
    private val VIEW_TYPE_UNLOCKED = 1

    override fun getItemViewType(position: Int): Int {
        return if (achievements[position].isUnlocked) VIEW_TYPE_UNLOCKED else VIEW_TYPE_LOCKED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_UNLOCKED -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_achievement_unlocked, parent, false)
                UnlockedViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_achievement_locked, parent, false)
                LockedViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val achievement = achievements[position]
        when (holder) {
            is UnlockedViewHolder -> holder.bind(achievement)
            is LockedViewHolder -> holder.bind(achievement, progressMap?.get(achievement.id) ?: 0)
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(achievement)
        }
    }

    override fun getItemCount() = achievements.size

    inner class UnlockedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.achievement_icon)
        private val name: TextView = itemView.findViewById(R.id.achievement_name)
        private val description: TextView = itemView.findViewById(R.id.achievement_description)
        private val points: TextView = itemView.findViewById(R.id.achievement_points)
        private val date: TextView = itemView.findViewById(R.id.achievement_date)

        fun bind(achievement: AchievementEntity) {
            name.text = achievement.name
            description.text = achievement.description
            points.text = "+${achievement.pointsReward} ★"

            icon.setImageResource(R.drawable.ic_achievement_unlocked)
            icon.setColorFilter(Color.parseColor("#FFC107"), PorterDuff.Mode.SRC_IN)

            achievement.unlockedDate?.let {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                date.text = "Получено: ${dateFormat.format(Date(it))}"
                date.visibility = View.VISIBLE
            } ?: run {
                date.visibility = View.GONE
            }
        }
    }

    inner class LockedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.achievement_icon_locked)
        private val name: TextView = itemView.findViewById(R.id.achievement_name_locked)
        private val description: TextView = itemView.findViewById(R.id.achievement_description_locked)
        private val points: TextView = itemView.findViewById(R.id.achievement_points_locked)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.achievement_progress)
        private val progressText: TextView = itemView.findViewById(R.id.achievement_progress_text)

        fun bind(achievement: AchievementEntity, currentProgress: Int) {
            name.text = achievement.name
            description.text = achievement.description
            points.text = "+${achievement.pointsReward} ★"

            icon.setImageResource(R.drawable.ic_achievement_locked)
            icon.alpha = 0.5f

            val progress = minOf(currentProgress, achievement.requirement)
            progressBar.max = achievement.requirement
            progressBar.progress = progress

            progressText.text = "$progress/${achievement.requirement}"
        }
    }
}