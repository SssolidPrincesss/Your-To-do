package com.bountyapp.yourrtodo.model

enum class UserStatus(
    val level: Int,
    val title: String,
    val description: String,
    val minPoints: Int,
    val color: String
) {
    BEGINNER(1, "Beginner", "Начинающий пользователь. Только начал свой путь к продуктивности.", 0, "#9E9E9E"),
    AVERAGE(2, "Average", "Средний уровень. Уже достигает базовых целей.", 100, "#4CAF50"),
    ADVANCED(3, "Advanced", "Продвинутый пользователь. Постоянно достигает сложных задач.", 300, "#2196F3"),
    GENIUS(4, "Genius", "Гений продуктивности. Мастер управления временем и задачами.", 600, "#FF9800"),
    INSANE(5, "Insane", "Безумец! Невероятная продуктивность и невероятное количество достижений.", 1000, "#F44336");

    companion object {
        fun getStatusByPoints(points: Int): UserStatus {
            return values().lastOrNull { points >= it.minPoints } ?: BEGINNER
        }
    }
}