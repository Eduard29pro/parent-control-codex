package ru.makdigital.parentcontrol

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.makdigital.parentcontrol.data.ScreenTimeRepository
import ru.makdigital.parentcontrol.data.SettingsRepository
import ru.makdigital.parentcontrol.model.LimitSettings
import ru.makdigital.parentcontrol.model.ScreenTimeSettings
import ru.makdigital.parentcontrol.policy.DevicePolicyController
import ru.makdigital.parentcontrol.policy.LimitsEnforcer
import ru.makdigital.parentcontrol.policy.ScreenTimeEngine
import ru.makdigital.parentcontrol.security.PinManager

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SettingsRepository(app)
    private val pin = PinManager(app)
    private val enforcer = LimitsEnforcer(app)
    private val policy = DevicePolicyController(app)
    private val screenTimeRepo = ScreenTimeRepository(app)
    private val screenTimeEngine = ScreenTimeEngine(app)

    val settings = repo.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LimitSettings())
    val screenTimeSettings = screenTimeRepo.config.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenTimeSettings())
    fun hasPin() = pin.hasPin()
    fun setPin(value: String) = pin.setPin(value.toCharArray())
    fun changePin(value: String) = pin.setPin(value.toCharArray())
    fun verifyPin(value: String) = pin.verify(value.toCharArray())
    fun isDeviceOwner() = policy.isDeviceOwner()
    fun canWriteSettings() = Settings.System.canWrite(getApplication()) || isDeviceOwner()

    fun writeSettingsIntent(): Intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
        data = Uri.parse("package:${getApplication<Application>().packageName}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun save(value: LimitSettings) = viewModelScope.launch {
        repo.save(value)
        enforcer.applyNow(value)
    }

    fun saveScreenTime(value: ScreenTimeSettings) = viewModelScope.launch {
        screenTimeRepo.saveConfig(value)
        if (value.enabled) screenTimeEngine.tick() else screenTimeEngine.forceDisable()
    }
}
