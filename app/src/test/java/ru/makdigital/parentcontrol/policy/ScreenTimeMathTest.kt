package ru.makdigital.parentcontrol.policy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.makdigital.parentcontrol.model.ScreenTimeCycleState
import ru.makdigital.parentcontrol.model.ScreenTimePhase
import ru.makdigital.parentcontrol.model.ScreenTimeSettings

class ScreenTimeMathTest {
    private val settings = ScreenTimeSettings(
        enabled = true,
        activeMinutes = 60,
        breakMinutes = 20,
        reduceBreakMinutesPerCorrectAnswer = 5,
        hardLockMinutes = 2,
        challengeCooldownSeconds = 4,
    )

    @Test fun activeUsedMillisSumsAccumulatedAndOpenSegment() {
        val state = ScreenTimeCycleState(activeAccumulatedMillis = 10_000L, screenOnSinceEpochMillis = 1_000L)
        assertEquals(14_000L, ScreenTimeMath.activeUsedMillis(state, nowMillis = 5_000L))
    }

    @Test fun activeUsedMillisIgnoresClosedSegment() {
        val state = ScreenTimeCycleState(activeAccumulatedMillis = 10_000L, screenOnSinceEpochMillis = null)
        assertEquals(10_000L, ScreenTimeMath.activeUsedMillis(state, nowMillis = 999_999L))
    }

    @Test fun activeBudgetExhaustedBoundary() {
        val exactBudget = settings.activeMinutes * 60_000L
        val notExhausted = ScreenTimeCycleState(activeAccumulatedMillis = exactBudget - 1)
        val exhausted = ScreenTimeCycleState(activeAccumulatedMillis = exactBudget)
        assertFalse(ScreenTimeMath.isActiveBudgetExhausted(notExhausted, settings, nowMillis = 0L))
        assertTrue(ScreenTimeMath.isActiveBudgetExhausted(exhausted, settings, nowMillis = 0L))
    }

    @Test fun hardLockBoundary() {
        val state = ScreenTimeCycleState(lockStartedAtEpochMillis = 1_000L)
        val endsAt = 1_000L + settings.hardLockMinutes * 60_000L
        assertFalse(ScreenTimeMath.isHardLockElapsed(state, settings, nowMillis = endsAt - 1))
        assertTrue(ScreenTimeMath.isHardLockElapsed(state, settings, nowMillis = endsAt))
    }

    @Test fun breakOverBoundary() {
        val state = ScreenTimeCycleState(breakEndsAtEpochMillis = 10_000L)
        assertFalse(ScreenTimeMath.isBreakOver(state, nowMillis = 9_999L))
        assertTrue(ScreenTimeMath.isBreakOver(state, nowMillis = 10_000L))
        assertEquals(1L, ScreenTimeMath.remainingBreakMillis(state, nowMillis = 9_999L))
        assertEquals(0L, ScreenTimeMath.remainingBreakMillis(state, nowMillis = 10_000L))
    }

    @Test fun correctAnswerReductionNeverGoesNegative() {
        val state = ScreenTimeCycleState(breakEndsAtEpochMillis = 2_000L)
        val reduced = ScreenTimeMath.applyCorrectAnswerReduction(state, settings, nowMillis = 1_000L)
        assertEquals(1_000L, reduced.breakEndsAtEpochMillis)
        assertEquals(1_000L, reduced.lastChallengeAttemptAtEpochMillis)
    }

    @Test fun correctAnswerReductionSubtractsWhenEnoughRemains() {
        val smallReduce = settings.copy(reduceBreakMinutesPerCorrectAnswer = 1)
        val state = ScreenTimeCycleState(breakEndsAtEpochMillis = 1_000_000L)
        val reduced = ScreenTimeMath.applyCorrectAnswerReduction(state, smallReduce, nowMillis = 0L)
        assertEquals(1_000_000L - 60_000L, reduced.breakEndsAtEpochMillis)
    }

    @Test fun cooldownBoundary() {
        val state = ScreenTimeCycleState(lastChallengeAttemptAtEpochMillis = 1_000L)
        val cooldownEnd = 1_000L + settings.challengeCooldownSeconds * 1_000L
        assertTrue(ScreenTimeMath.isChallengeOnCooldown(state, settings, nowMillis = cooldownEnd - 1))
        assertFalse(ScreenTimeMath.isChallengeOnCooldown(state, settings, nowMillis = cooldownEnd))
    }

    @Test fun startLockedPhaseSetsExpectedFields() {
        val state = ScreenTimeMath.startLockedPhase(settings, nowMillis = 5_000L)
        assertEquals(ScreenTimePhase.LOCKED, state.phase)
        assertEquals(5_000L, state.lockStartedAtEpochMillis)
        assertEquals(5_000L + settings.breakMinutes * 60_000L, state.breakEndsAtEpochMillis)
        assertEquals(0L, state.activeAccumulatedMillis)
    }

    @Test fun startActivePhaseTracksCurrentScreenState() {
        val screenOn = ScreenTimeMath.startActivePhase(nowMillis = 5_000L, screenCurrentlyOn = true)
        assertEquals(ScreenTimePhase.ACTIVE, screenOn.phase)
        assertEquals(5_000L, screenOn.screenOnSinceEpochMillis)

        val screenOff = ScreenTimeMath.startActivePhase(nowMillis = 5_000L, screenCurrentlyOn = false)
        assertNull(screenOff.screenOnSinceEpochMillis)
    }
}
