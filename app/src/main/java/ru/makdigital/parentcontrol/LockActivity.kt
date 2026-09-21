package ru.makdigital.parentcontrol

import android.app.ActivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import ru.makdigital.parentcontrol.policy.DevicePolicyController
import ru.makdigital.parentcontrol.ui.LockScreen
import ru.makdigital.parentcontrol.ui.theme.ParentControlTheme

class LockActivity : ComponentActivity() {
    private val vm: LockScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { /* consumed: the lock cannot be dismissed via back */ }
        })
        setContent {
            ParentControlTheme {
                LockScreen(vm = vm, onUnlocked = ::finishLock)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enterLockTaskIfNeeded()
    }

    private fun enterLockTaskIfNeeded() {
        val am = getSystemService(ActivityManager::class.java)
        if (am?.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE) return
        val policy = DevicePolicyController(this)
        if (!policy.isDeviceOwner()) return
        policy.setLockTaskPackages(arrayOf(packageName))
        policy.setLockTaskFeatures(android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_NONE)
        startLockTask()
    }

    private fun finishLock() {
        val am = getSystemService(ActivityManager::class.java)
        if (am?.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE) stopLockTask()
        finish()
    }
}
