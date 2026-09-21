package ru.makdigital.parentcontrol.policy

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context

class DevicePolicyController(private val context: Context) {
    private val dpm = context.getSystemService(DevicePolicyManager::class.java)
    private val admin = ComponentName(context, AdminReceiver::class.java)

    fun isDeviceOwner(): Boolean = dpm.isDeviceOwnerApp(context.packageName)
    fun isAdminActive(): Boolean = dpm.isAdminActive(admin)

    fun setSystemSetting(name: String, value: String): Boolean {
        if (!isDeviceOwner()) return false
        return runCatching {
            dpm.setSystemSetting(admin, name, value)
            true
        }.getOrDefault(false)
    }

    fun setLockTaskPackages(packages: Array<String>): Boolean {
        if (!isDeviceOwner()) return false
        return runCatching {
            dpm.setLockTaskPackages(admin, packages)
            true
        }.getOrDefault(false)
    }

    fun setLockTaskFeatures(features: Int): Boolean {
        if (!isDeviceOwner()) return false
        return runCatching {
            dpm.setLockTaskFeatures(admin, features)
            true
        }.getOrDefault(false)
    }
}
