package ru.makdigital.parentcontrol.policy

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import kotlinx.coroutines.flow.first
import ru.makdigital.parentcontrol.LockActivity
import ru.makdigital.parentcontrol.data.ScreenTimeRepository
import ru.makdigital.parentcontrol.model.ScreenTimeCycleState
import ru.makdigital.parentcontrol.model.ScreenTimePhase
import ru.makdigital.parentcontrol.model.ScreenTimeSettings

/** Android-facing orchestrator for the screen-time cycle. The only place that mutates [ScreenTimeCycleState]. */
class ScreenTimeEngine(private val context: Context) {
    private val repo = ScreenTimeRepository(context)
    private val policy = DevicePolicyController(context)
    private val powerManager = context.getSystemService(PowerManager::class.java)

    private fun isScreenOn(): Boolean = powerManager?.isInteractive ?: true

    suspend fun reconcileOnStart() {
        val settings = repo.config.first()
        if (!settings.enabled || !policy.isDeviceOwner()) return
        val state = repo.cycleState.first()
        val now = System.currentTimeMillis()
        when (state.phase) {
            ScreenTimePhase.LOCKED -> {
                if (ScreenTimeMath.isBreakOver(state, now)) transitionToActive(now) else launchLockScreen()
            }
            ScreenTimePhase.ACTIVE -> {
                // Never trust a stale open screen-on segment across a cold start: don't back-credit downtime as usage.
                val resumed = state.copy(screenOnSinceEpochMillis = if (isScreenOn()) now else null)
                repo.saveState(resumed)
                if (ScreenTimeMath.isActiveBudgetExhausted(resumed, settings, now)) transitionToLocked(settings, now)
            }
        }
    }

    suspend fun onScreenOn() {
        val settings = repo.config.first()
        if (!settings.enabled || !policy.isDeviceOwner()) return
        val state = repo.cycleState.first()
        if (state.phase != ScreenTimePhase.ACTIVE || state.screenOnSinceEpochMillis != null) return
        repo.saveState(state.copy(screenOnSinceEpochMillis = System.currentTimeMillis()))
    }

    suspend fun onScreenOff() {
        val settings = repo.config.first()
        if (!settings.enabled || !policy.isDeviceOwner()) return
        val state = repo.cycleState.first()
        if (state.phase != ScreenTimePhase.ACTIVE) return
        val since = state.screenOnSinceEpochMillis ?: return
        val now = System.currentTimeMillis()
        repo.saveState(
            state.copy(
                activeAccumulatedMillis = state.activeAccumulatedMillis + (now - since).coerceAtLeast(0),
                screenOnSinceEpochMillis = null,
            )
        )
    }

    suspend fun tick(nowMillis: Long = System.currentTimeMillis()) {
        val settings = repo.config.first()
        if (!settings.enabled || !policy.isDeviceOwner()) return
        var state = repo.cycleState.first()
        // Self-heal a missing screen-on anchor: covers the feature being enabled (or the process
        // restarting) while the screen is already on, when no SCREEN_ON broadcast will ever fire again.
        if (state.phase == ScreenTimePhase.ACTIVE && state.screenOnSinceEpochMillis == null && isScreenOn()) {
            state = state.copy(screenOnSinceEpochMillis = nowMillis)
            repo.saveState(state)
        }
        when (state.phase) {
            ScreenTimePhase.ACTIVE ->
                if (ScreenTimeMath.isActiveBudgetExhausted(state, settings, nowMillis)) transitionToLocked(settings, nowMillis)
            ScreenTimePhase.LOCKED ->
                if (ScreenTimeMath.isBreakOver(state, nowMillis)) transitionToActive(nowMillis)
        }
    }

    /** Returns true if the submitted answer was correct. No-ops (returns false) outside LOCKED, during the hard lock, or on cooldown. */
    suspend fun submitAnswer(problem: MathChallenge.Problem, answer: Int): Boolean {
        val settings = repo.config.first()
        val state = repo.cycleState.first()
        val now = System.currentTimeMillis()
        if (state.phase != ScreenTimePhase.LOCKED) return false
        if (!ScreenTimeMath.isHardLockElapsed(state, settings, now)) return false
        if (ScreenTimeMath.isChallengeOnCooldown(state, settings, now)) return false
        val correct = MathChallenge.isCorrect(problem, answer)
        val newState = if (correct) {
            ScreenTimeMath.applyCorrectAnswerReduction(state, settings, now)
        } else {
            ScreenTimeMath.recordAttempt(state, now)
        }
        repo.saveState(newState)
        if (ScreenTimeMath.isBreakOver(newState, now)) transitionToActive(now)
        return correct
    }

    /** Parent PIN override from the lock screen: ends the current lock only, feature stays enabled. */
    suspend fun parentOverrideEndLock() {
        transitionToActive(System.currentTimeMillis())
    }

    /** Called when the parent turns the whole feature off. */
    suspend fun forceDisable() {
        policy.setLockTaskPackages(emptyArray())
        repo.saveState(ScreenTimeMath.startActivePhase(System.currentTimeMillis(), isScreenOn()))
    }

    private suspend fun transitionToLocked(settings: ScreenTimeSettings, now: Long) {
        repo.saveState(ScreenTimeMath.startLockedPhase(settings, now))
        launchLockScreen()
    }

    private suspend fun transitionToActive(now: Long) {
        repo.saveState(ScreenTimeMath.startActivePhase(now, isScreenOn()))
    }

    private fun launchLockScreen() {
        policy.setLockTaskPackages(arrayOf(context.packageName))
        val intent = Intent(context, LockActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        context.startActivity(intent)
    }
}
