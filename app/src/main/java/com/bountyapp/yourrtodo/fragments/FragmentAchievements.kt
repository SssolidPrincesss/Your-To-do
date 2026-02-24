package com.bountyapp.yourrtodo.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.adapter.AchievementAdapter
import com.bountyapp.yourrtodo.data.entities.AchievementEntity
import com.bountyapp.yourrtodo.viewmodel.AchievementsViewModel
import com.google.android.material.tabs.TabLayout

class FragmentAchievements : Fragment() {

    private lateinit var btnInfo: ImageButton
    private lateinit var viewModel: AchievementsViewModel
    private lateinit var achievementAdapter: AchievementAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tabLayout: TabLayout

    private lateinit var currentStatusTitle: TextView
    private lateinit var currentStatusDescription: TextView
    private lateinit var currentPoints: TextView

    private lateinit var pointsToNext: TextView
    private lateinit var statusProgress: ProgressBar
    private lateinit var totalPoints: TextView

    private var currentTabPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_achievements, container, false)

        // Инициализируем кнопку
        btnInfo = view.findViewById(R.id.btn_info)

        // Устанавливаем обработчик нажатия
        btnInfo.setOnClickListener {
            showInfoDialog()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[AchievementsViewModel::class.java]

        initViews(view)
        setupTabs()
        setupRecyclerView()
        observeViewModel()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.achievements_recycler_view)
        tabLayout = view.findViewById(R.id.tab_layout)

        currentStatusTitle = view.findViewById(R.id.current_status_title)
        currentStatusDescription = view.findViewById(R.id.current_status_description)
        currentPoints = view.findViewById(R.id.current_points)

        pointsToNext = view.findViewById(R.id.points_to_next)
        statusProgress = view.findViewById(R.id.status_progress)
        totalPoints = view.findViewById(R.id.total_points)
    }

    private fun showInfoDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Информация")
            .setMessage("Зарабатывай очки достижений, чтобы получить новый статус и привилегии")
            .setPositiveButton("Понятно") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(R.drawable.ic_info_outline)
            .show()
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTabPosition = tab?.position ?: 0
                updateAdapterData()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        achievementAdapter = AchievementAdapter(
            context = requireContext(),
            achievements = emptyList(),
            progressMap = emptyMap(),
            onItemClick = { achievement ->
                showAchievementDetails(achievement)
            }
        )
        recyclerView.adapter = achievementAdapter
    }

    private fun observeViewModel() {
        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                Log.d("FragmentAchievements", "Stats updated: totalPoints=${it.totalPoints}")
                totalPoints.text = "${it.totalPoints} ★"
                currentPoints.text = "${it.totalPoints} ★"
                updateStatusProgress()
            } ?: run {
                Log.d("FragmentAchievements", "Stats is null")
            }
        }


        viewModel.currentStatus.observe(viewLifecycleOwner) { status ->
            currentStatusTitle.text = status.title
            currentStatusDescription.text = status.description
        }


        viewModel.pointsToNextStatus.observe(viewLifecycleOwner) { points ->
            pointsToNext.text = "$points ★ до следующего статуса"
        }

        // Обновляем прогресс бар
        viewModel.getProgressToNextStatus().also { progress ->
            statusProgress.progress = (progress * 100).toInt()
        }

        viewModel.achievements.observe(viewLifecycleOwner) { achievements ->
            updateAdapterData()
        }

        viewModel.achievementProgress.observe(viewLifecycleOwner) { progress ->
            updateAdapterData()
        }

        viewModel.unlockedAchievementMessage.observe(viewLifecycleOwner) { achievement ->
            achievement?.let {
                showUnlockedMessage(it)
                viewModel.clearUnlockedMessage()

                // Принудительно обновляем статистику после показа сообщения
                viewModel.userStats.value?.let { stats ->
                    totalPoints.text = "${stats.totalPoints} ★"
                    currentPoints.text = "${stats.totalPoints} ★"
                }
            }
        }
    }

    private fun updateStatusProgress() {
        val progress = viewModel.getProgressToNextStatus()
        statusProgress.progress = (progress * 100).toInt()

        // Обновляем текст "до следующего статуса"
        viewModel.pointsToNextStatus.value?.let { points ->
            pointsToNext.text = "$points ★ до следующего статуса"
        }
    }
    private fun updateAdapterData() {
        val achievements = when (currentTabPosition) {
            0 -> viewModel.achievements.value ?: emptyList()
            1 -> viewModel.unlockedAchievements.value ?: emptyList()
            else -> viewModel.lockedAchievements.value ?: emptyList()
        }

        achievementAdapter = AchievementAdapter(
            context = requireContext(),
            achievements = achievements,
            progressMap = viewModel.achievementProgress.value ?: emptyMap(),
            onItemClick = { achievement ->
                showAchievementDetails(achievement)
            }
        )
        recyclerView.adapter = achievementAdapter
    }

    private fun showAchievementDetails(achievement: AchievementEntity) {
        val message = if (achievement.isUnlocked) {
            "Достижение получено!\nНаграда: ${achievement.pointsReward} ★"
        } else {
            val progress = viewModel.achievementProgress.value?.get(achievement.id) ?: 0
            "Прогресс: $progress/${achievement.requirement}\nНаграда: ${achievement.pointsReward} ★"
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle(achievement.name)
            .setMessage(message)
            .setPositiveButton("ОК", null)
            .show()
    }

    private fun showUnlockedMessage(achievement: AchievementEntity) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🎉 Достижение получено!")
            .setMessage("${achievement.name}\n+${achievement.pointsReward} ★")
            .setPositiveButton("Ура!", null)
            .show()
    }

    fun onTaskCompleted() {
        viewModel.onTaskCompleted()
    }

    override fun onResume() {
        super.onResume()
        // При возвращении на фрагмент принудительно обновляем данные
        viewModel.loadData() // Если такого метода нет, создайте его
    }

}