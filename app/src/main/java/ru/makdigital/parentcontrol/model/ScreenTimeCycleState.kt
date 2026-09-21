package ru.makdigital.parentcontrol.model

enum class ScreenTimePhase { ACTIVE, LOCKED }

data class ScreenTimeCycleState(
    val phase: ScreenTimePhase = ScreenTimePhase.ACTIVE,
    val activeAccumulatedMillis: Long = 0L,
    val screenOnSinceEpochMillis: Long? = null,
    val lockStartedAtEpochMillis: Long = 0L,
    val breakEndsAtEpochMillis: Long = 0L,
    val lastChallengeAttemptAtEpochMillis: Long = 0L,
)
