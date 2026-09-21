package ru.makdigital.parentcontrol.model

data class ScreenTimeSettings(
    val enabled: Boolean = false,
    val activeMinutes: Int = 60,
    val breakMinutes: Int = 20,
    val reduceBreakMinutesPerCorrectAnswer: Int = 5,
    val hardLockMinutes: Int = 2,
    val challengeCooldownSeconds: Int = 4,
)
