package com.bountyapp.yourrtodo.utils

object GamificationUtils {
    /**
     * Расчет уровня пользователя на основе рейтинга
     */
    fun calculateLevel(rating: Int): Int {
        return when {
            rating < 100 -> 1
            rating < 300 -> 2
            rating < 600 -> 3
            rating < 1000 -> 4
            rating < 1500 -> 5
            rating < 2100 -> 6
            rating < 2800 -> 7
            rating < 3600 -> 8
            rating < 4500 -> 9
            else -> 10
        }
    }

    /**
     * Расчет очков до следующего уровня
     */
    fun pointsToNextLevel(currentRating: Int): Int {
        val currentLevel = calculateLevel(currentRating)
        val nextLevelThreshold = when (currentLevel) {
            1 -> 100
            2 -> 300
            3 -> 600
            4 -> 1000
            5 -> 1500
            6 -> 2100
            7 -> 2800
            8 -> 3600
            9 -> 4500
            else -> Int.MAX_VALUE
        }
        return nextLevelThreshold - currentRating
    }

    /**
     * Получение названия статуса по уровню
     */
    fun getStatusTitle(level: Int): String {
        return when (level) {
            1 -> "Новичок"
            2 -> "Стажер"
            3 -> "Опытный"
            4 -> "Профессионал"
            5 -> "Эксперт"
            6 -> "Мастер"
            7 -> "Гуру"
            8 -> "Легенда"
            9 -> "Миф"
            else -> "Бессмертный"
        }
    }
}