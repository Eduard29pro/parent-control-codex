package ru.makdigital.parentcontrol.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.makdigital.parentcontrol.model.ScreenTimeCycleState
import ru.makdigital.parentcontrol.model.ScreenTimePhase
import ru.makdigital.parentcontrol.model.ScreenTimeSettings

private val Context.screenTimeDataStore by preferencesDataStore("screen_time")

class ScreenTimeRepository(private val context: Context) {
    private object ConfigKeys {
        val enabled = booleanPreferencesKey("st_enabled")
        val activeMinutes = intPreferencesKey("st_active_minutes")
        val breakMinutes = intPreferencesKey("st_break_minutes")
        val reduceBreakMinutesPerCorrectAnswer = intPreferencesKey("st_reduce_minutes")
        val hardLockMinutes = intPreferencesKey("st_hard_lock_minutes")
        val challengeCooldownSeconds = intPreferencesKey("st_cooldown_seconds")
    }

    private object StateKeys {
        val phase = intPreferencesKey("st_phase")
        val activeAccumulatedMillis = longPreferencesKey("st_active_accumulated_ms")
        val screenOnSinceEpochMillis = longPreferencesKey("st_screen_on_since")
        val lockStartedAtEpochMillis = longPreferencesKey("st_lock_started_at")
        val breakEndsAtEpochMillis = longPreferencesKey("st_break_ends_at")
        val lastChallengeAttemptAtEpochMillis = longPreferencesKey("st_last_attempt_at")
    }

    val config: Flow<ScreenTimeSettings> = context.screenTimeDataStore.data.map { p ->
        val breakMinutes = (p[ConfigKeys.breakMinutes] ?: 20).coerceIn(5, 60)
        ScreenTimeSettings(
            enabled = p[ConfigKeys.enabled] ?: false,
            activeMinutes = (p[ConfigKeys.activeMinutes] ?: 60).coerceIn(15, 180),
            breakMinutes = breakMinutes,
            reduceBreakMinutesPerCorrectAnswer = (p[ConfigKeys.reduceBreakMinutesPerCorrectAnswer] ?: 5).coerceIn(1, 15),
            hardLockMinutes = (p[ConfigKeys.hardLockMinutes] ?: 2).coerceIn(1, (breakMinutes - 1).coerceAtLeast(1)),
            challengeCooldownSeconds = (p[ConfigKeys.challengeCooldownSeconds] ?: 4).coerceIn(3, 10),
        )
    }

    val cycleState: Flow<ScreenTimeCycleState> = context.screenTimeDataStore.data.map { p ->
        ScreenTimeCycleState(
            phase = if ((p[StateKeys.phase] ?: 0) == 1) ScreenTimePhase.LOCKED else ScreenTimePhase.ACTIVE,
            activeAccumulatedMillis = p[StateKeys.activeAccumulatedMillis] ?: 0L,
            screenOnSinceEpochMillis = p[StateKeys.screenOnSinceEpochMillis],
            lockStartedAtEpochMillis = p[StateKeys.lockStartedAtEpochMillis] ?: 0L,
            breakEndsAtEpochMillis = p[StateKeys.breakEndsAtEpochMillis] ?: 0L,
            lastChallengeAttemptAtEpochMillis = p[StateKeys.lastChallengeAttemptAtEpochMillis] ?: 0L,
        )
    }

    suspend fun saveConfig(value: ScreenTimeSettings) = context.screenTimeDataStore.edit { p ->
        val breakMinutes = value.breakMinutes.coerceIn(5, 60)
        p[ConfigKeys.enabled] = value.enabled
        p[ConfigKeys.activeMinutes] = value.activeMinutes.coerceIn(15, 180)
        p[ConfigKeys.breakMinutes] = breakMinutes
        p[ConfigKeys.reduceBreakMinutesPerCorrectAnswer] = value.reduceBreakMinutesPerCorrectAnswer.coerceIn(1, 15)
        p[ConfigKeys.hardLockMinutes] = value.hardLockMinutes.coerceIn(1, (breakMinutes - 1).coerceAtLeast(1))
        p[ConfigKeys.challengeCooldownSeconds] = value.challengeCooldownSeconds.coerceIn(3, 10)
    }

    suspend fun saveState(value: ScreenTimeCycleState) = context.screenTimeDataStore.edit { p ->
        p[StateKeys.phase] = if (value.phase == ScreenTimePhase.LOCKED) 1 else 0
        p[StateKeys.activeAccumulatedMillis] = value.activeAccumulatedMillis
        value.screenOnSinceEpochMillis?.let { p[StateKeys.screenOnSinceEpochMillis] = it }
            ?: p.remove(StateKeys.screenOnSinceEpochMillis)
        p[StateKeys.lockStartedAtEpochMillis] = value.lockStartedAtEpochMillis
        p[StateKeys.breakEndsAtEpochMillis] = value.breakEndsAtEpochMillis
        p[StateKeys.lastChallengeAttemptAtEpochMillis] = value.lastChallengeAttemptAtEpochMillis
    }
}
