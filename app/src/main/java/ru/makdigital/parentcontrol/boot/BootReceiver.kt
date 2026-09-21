package ru.makdigital.parentcontrol.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.makdigital.parentcontrol.data.SettingsRepository
import ru.makdigital.parentcontrol.policy.LimitsEnforcer
import ru.makdigital.parentcontrol.policy.ScreenTimeEngine

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                LimitsEnforcer(context).applyNow(SettingsRepository(context).settings.first())
                ScreenTimeEngine(context).reconcileOnStart()
            } finally { pending.finish() }
        }
    }
}
