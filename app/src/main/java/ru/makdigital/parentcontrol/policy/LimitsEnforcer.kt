package ru.makdigital.parentcontrol.policy

import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import ru.makdigital.parentcontrol.model.LimitSettings

class LimitsEnforcer(private val context: Context) {
    private val audio = context.getSystemService(AudioManager::class.java)
    private val policy = DevicePolicyController(context)
    private val state = context.getSharedPreferences("enforcement_state", Context.MODE_PRIVATE)

    fun applyNow(settings: LimitSettings) {
        if (!settings.enabled) {
            restoreAdaptiveBrightnessMode()
            return
        }
        clampBrightness(settings.maxBrightnessPercent)
        clampMediaVolume(settings.maxMediaVolumePercent)
    }

    fun clampBrightness(percent: Int) {
        rememberAndDisableAdaptiveBrightness()
        val cap = LimitsMath.brightnessValueFromPercent(percent)
        val current = runCatching { Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS) }.getOrDefault(cap)
        if (current <= cap) return
        if (policy.setSystemSetting(Settings.System.SCREEN_BRIGHTNESS, cap.toString())) return
        if (Settings.System.canWrite(context)) {
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, cap)
        }
    }

    fun clampMediaVolume(percent: Int) {
        val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val cap = LimitsMath.volumeStepsFromPercent(percent, max)
        if (audio.getStreamVolume(AudioManager.STREAM_MUSIC) > cap) {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, cap, 0)
        }
    }

    private fun rememberAndDisableAdaptiveBrightness() {
        val mode = runCatching {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
        }.getOrNull() ?: return
        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            state.edit().putBoolean("was_adaptive", true).apply()
            setSystemSetting(Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
        }
    }

    private fun restoreAdaptiveBrightnessMode() {
        if (!state.getBoolean("was_adaptive", false)) return
        setSystemSetting(Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
        state.edit().remove("was_adaptive").apply()
    }

    private fun setSystemSetting(name: String, value: Int): Boolean {
        if (policy.isDeviceOwner() && runCatching {
                policy.setSystemSetting(name, value.toString())
            }.getOrDefault(false)) return true
        return Settings.System.canWrite(context) && runCatching {
            Settings.System.putInt(context.contentResolver, name, value)
        }.getOrDefault(false)
    }
}
