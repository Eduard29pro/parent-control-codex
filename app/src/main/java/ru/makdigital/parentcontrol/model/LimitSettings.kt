package ru.makdigital.parentcontrol.model

data class LimitSettings(
    val enabled: Boolean = true,
    val maxBrightnessPercent: Int = 40,
    val maxMediaVolumePercent: Int = 30,
)
