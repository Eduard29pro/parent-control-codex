package ru.makdigital.parentcontrol.service

import android.app.admin.DeviceAdminService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.makdigital.parentcontrol.data.SettingsRepository
import ru.makdigital.parentcontrol.policy.LimitsEnforcer
import ru.makdigital.parentcontrol.policy.ScreenTimeEngine

/** Visible, OS-bound observer used only while the app is provisioned as Device Owner. */
class EnforcementAdminService : DeviceAdminService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var observer: ContentObserver
    private lateinit var screenReceiver: BroadcastReceiver
    private val screenTimeEngine by lazy { ScreenTimeEngine(applicationContext) }

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

        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                scope.launch {
                    when (intent.action) {
                        Intent.ACTION_SCREEN_ON -> screenTimeEngine.onScreenOn()
                        Intent.ACTION_SCREEN_OFF -> screenTimeEngine.onScreenOff()
                    }
                }
            }
        }
        registerReceiver(screenReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        })

        scope.launch {
            screenTimeEngine.reconcileOnStart()
            while (isActive) {
                delay(15_000)
                screenTimeEngine.tick()
            }
        }
    }

    override fun onDestroy() {
        contentResolver.unregisterContentObserver(observer)
        unregisterReceiver(screenReceiver)
        scope.cancel()
        super.onDestroy()
    }

    private fun applyLimits() {
        scope.launch {
            LimitsEnforcer(applicationContext).applyNow(SettingsRepository(applicationContext).settings.first())
        }
    }
}
