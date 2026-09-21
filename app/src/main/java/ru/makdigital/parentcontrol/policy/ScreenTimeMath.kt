package ru.makdigital.parentcontrol.policy

import ru.makdigital.parentcontrol.model.ScreenTimeCycleState
import ru.makdigital.parentcontrol.model.ScreenTimePhase
import ru.makdigital.parentcontrol.model.ScreenTimeSettings

/** Pure phase-transition helpers for the screen-time cycle. No Android dependencies. */
object ScreenTimeMath {
    private const val MINUTE_MS = 60_000L
    private const val SECOND_MS = 1_000L

    fun activeUsedMillis(state: ScreenTimeCycleState, nowMillis: Long): Long {
        val openSegment = state.screenOnSinceEpochMillis?.let { (nowMillis - it).coerceAtLeast(0) } ?: 0L
        return state.activeAccumulatedMillis + openSegment
    }

    fun isActiveBudgetExhausted(state: ScreenTimeCycleState, settings: ScreenTimeSettings, nowMillis: Long): Boolean =
        activeUsedMillis(state, nowMillis) >= settings.activeMinutes * MINUTE_MS

    fun hardLockEndsAtMillis(state: ScreenTimeCycleState, settings: ScreenTimeSettings): Long =
        state.lockStartedAtEpochMillis + settings.hardLockMinutes * MINUTE_MS

    fun isHardLockElapsed(state: ScreenTimeCycleState, settings: ScreenTimeSettings, nowMillis: Long): Boolean =
        nowMillis >= hardLockEndsAtMillis(state, settings)

    fun isBreakOver(state: ScreenTimeCycleState, nowMillis: Long): Boolean =
        nowMillis >= state.breakEndsAtEpochMillis

    fun remainingBreakMillis(state: ScreenTimeCycleState, nowMillis: Long): Long =
        (state.breakEndsAtEpochMillis - nowMillis).coerceAtLeast(0)

    fun isChallengeOnCooldown(state: ScreenTimeCycleState, settings: ScreenTimeSettings, nowMillis: Long): Boolean =
        (nowMillis - state.lastChallengeAttemptAtEpochMillis) < settings.challengeCooldownSeconds * SECOND_MS

    fun applyCorrectAnswerReduction(
        state: ScreenTimeCycleState,
        settings: ScreenTimeSettings,
        nowMillis: Long,
    ): ScreenTimeCycleState = state.copy(
        breakEndsAtEpochMillis = (state.breakEndsAtEpochMillis - settings.reduceBreakMinutesPerCorrectAnswer * MINUTE_MS)
            .coerceAtLeast(nowMillis),
        lastChallengeAttemptAtEpochMillis = nowMillis,
    )

    fun recordAttempt(state: ScreenTimeCycleState, nowMillis: Long): ScreenTimeCycleState =
        state.copy(lastChallengeAttemptAtEpochMillis = nowMillis)

    fun startLockedPhase(settings: ScreenTimeSettings, nowMillis: Long): ScreenTimeCycleState = ScreenTimeCycleState(
        phase = ScreenTimePhase.LOCKED,
        activeAccumulatedMillis = 0L,
        screenOnSinceEpochMillis = null,
        lockStartedAtEpochMillis = nowMillis,
        breakEndsAtEpochMillis = nowMillis + settings.breakMinutes * MINUTE_MS,
        lastChallengeAttemptAtEpochMillis = 0L,
    )

    fun startActivePhase(nowMillis: Long, screenCurrentlyOn: Boolean): ScreenTimeCycleState = ScreenTimeCycleState(
        phase = ScreenTimePhase.ACTIVE,
        activeAccumulatedMillis = 0L,
        screenOnSinceEpochMillis = if (screenCurrentlyOn) nowMillis else null,
    )
}
