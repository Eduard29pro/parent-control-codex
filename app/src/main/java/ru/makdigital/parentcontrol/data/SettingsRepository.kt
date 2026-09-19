package ru.makdigital.parentcontrol.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.makdigital.parentcontrol.model.LimitSettings

private val Context.dataStore by preferencesDataStore("parent_control")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val enabled = booleanPreferencesKey("limits_enabled")
        val brightness = intPreferencesKey("max_brightness")
        val volume = intPreferencesKey("max_media_volume")
    }

    val settings: Flow<LimitSettings> = context.dataStore.data.map { p ->
        LimitSettings(
            enabled = p[Keys.enabled] ?: true,
            maxBrightnessPercent = (p[Keys.brightness] ?: 40).coerceIn(1, 100),
            maxMediaVolumePercent = (p[Keys.volume] ?: 30).coerceIn(0, 100),
        )
    }

    suspend fun save(value: LimitSettings) = context.dataStore.edit { p ->
        p[Keys.enabled] = value.enabled
        p[Keys.brightness] = value.maxBrightnessPercent.coerceIn(1, 100)
        p[Keys.volume] = value.maxMediaVolumePercent.coerceIn(0, 100)
    }
}
