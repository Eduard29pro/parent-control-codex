package ru.makdigital.parentcontrol.service

import android.app.admin.DeviceAdminService
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.makdigital.parentcontrol.data.SettingsRepository
import ru.makdigital.parentcontrol.policy.LimitsEnforcer

/** Visible, OS-bound observer used only while the app is provisioned as Device Owner. */
class EnforcementAdminService : DeviceAdminService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var observer: ContentObserver

    override fun onCreate() {
        super.onCreate()
        observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                applyLimits()
            }
        }
        contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), false, observer
        )
        contentResolver.registerContentObserver(
            Settings.System.getUriFor("volume_music"), false, observer
        )
        applyLimits()
    }

    override fun onDestroy() {
        contentResolver.unregisterContentObserver(observer)
        scope.cancel()
        super.onDestroy()
    }

    private fun applyLimits() {
        scope.launch {
            LimitsEnforcer(applicationContext).applyNow(SettingsRepository(applicationContext).settings.first())
        }
    }
}
