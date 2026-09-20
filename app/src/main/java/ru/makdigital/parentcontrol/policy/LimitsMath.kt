package ru.makdigital.parentcontrol.policy

import kotlin.math.roundToInt

object LimitsMath {
    fun brightnessValueFromPercent(percent: Int, platformMax: Int = 255): Int =
        (percent.coerceIn(0, 100) / 100f * platformMax).roundToInt().coerceIn(1, platformMax)

    fun volumeStepsFromPercent(percent: Int, streamMax: Int): Int =
        (streamMax * percent.coerceIn(0, 100) / 100f).roundToInt().coerceIn(0, streamMax)
}
