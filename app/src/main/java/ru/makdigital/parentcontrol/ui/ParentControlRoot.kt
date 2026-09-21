package ru.makdigital.parentcontrol.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.makdigital.parentcontrol.MainViewModel
import ru.makdigital.parentcontrol.R
import ru.makdigital.parentcontrol.model.LimitSettings
import ru.makdigital.parentcontrol.model.ScreenTimeSettings
import ru.makdigital.parentcontrol.security.PinManager

@Composable
fun ParentControlRoot(vm: MainViewModel) {
    var unlocked by remember { mutableStateOf(false) }
    var hasPin by remember { mutableStateOf(vm.hasPin()) }
    var changingPin by remember { mutableStateOf(false) }
    when {
        !hasPin -> PinScreen(PinMode.Setup) { vm.setPin(it); hasPin = true; unlocked = true; true }
        !unlocked -> PinScreen(PinMode.Unlock) { vm.verifyPin(it).also { unlocked = it } }
        changingPin -> PinScreen(PinMode.Change) { vm.changePin(it); changingPin = false; true }
        else -> SettingsScreen(vm) { changingPin = true }
    }
}

private enum class PinMode { Setup, Unlock, Change }

@Composable
private fun PinScreen(mode: PinMode, onSuccess: (String) -> Boolean) {
    var pin by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var failedAttempts by remember { mutableIntStateOf(0) }
    var lockedUntil by remember { mutableLongStateOf(0L) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(lockedUntil) {
        if (lockedUntil > 0) {
            kotlinx.coroutines.delay((lockedUntil - System.currentTimeMillis()).coerceAtLeast(0L))
            now = System.currentTimeMillis()
        }
    }
    val locked = now < lockedUntil
    val title = when (mode) {
        PinMode.Setup -> stringResource(R.string.pin_setup_title)
        PinMode.Unlock -> stringResource(R.string.pin_unlock_title)
        PinMode.Change -> stringResource(R.string.pin_change_title)
    }
    val invalidMessage = stringResource(R.string.pin_invalid)
    val mismatchMessage = stringResource(R.string.pin_mismatch)
    val wrongMessage = stringResource(R.string.pin_wrong)
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp)); Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(24.dp))
            PinField(stringResource(R.string.pin_label), pin) { pin = it; error = null }
            if (mode != PinMode.Unlock) {
                Spacer(Modifier.height(12.dp))
                PinField(stringResource(R.string.pin_confirm_label), confirmation) { confirmation = it; error = null }
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                val valid = when {
                    locked -> false
                    !PinManager.isValidPin(pin.toCharArray()) -> { error = invalidMessage; false }
                    mode != PinMode.Unlock && pin != confirmation -> { error = mismatchMessage; false }
                    else -> true
                }
                if (valid) {
                    if (!onSuccess(pin)) { error = wrongMessage; failedAttempts++ }
                }
                if (mode == PinMode.Unlock && error != null && failedAttempts >= 3) lockedUntil = System.currentTimeMillis() + 30_000L
            }, enabled = !locked && pin.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                Text(if (locked) stringResource(R.string.pin_wait) else stringResource(R.string.continue_label))
            }
        }
    }
}

@Composable
private fun PinField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value, { if (it.length <= 8 && it.all(Char::isDigit)) onValueChange(it) },
        label = { Text(label) }, singleLine = true, visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), modifier = Modifier.fillMaxWidth())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(vm: MainViewModel, onChangePin: () -> Unit) {
    val saved by vm.settings.collectAsStateWithLifecycle()
    var enabled by remember(saved) { mutableStateOf(saved.enabled) }
    var brightness by remember(saved) { mutableFloatStateOf(saved.maxBrightnessPercent.toFloat()) }
    var volume by remember(saved) { mutableFloatStateOf(saved.maxMediaVolumePercent.toFloat()) }
    var savedMessage by remember { mutableStateOf(false) }
    val owner = vm.isDeviceOwner(); val canWrite = vm.canWriteSettings(); val context = LocalContext.current
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.limits_title)) }) }) { pad ->
        Column(
            Modifier.padding(pad).padding(20.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                Text(stringResource(if (owner) R.string.owner_active else R.string.owner_inactive), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(if (owner) R.string.owner_active_help else R.string.owner_inactive_help))
            }}
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.limits_enabled), Modifier.weight(1f)); Switch(enabled, { enabled = it })
            }
            LimitSlider(stringResource(R.string.brightness_max), brightness, 1f..100f) { brightness = it }
            LimitSlider(stringResource(R.string.volume_max), volume, 0f..100f) { volume = it }
            if (!canWrite && !owner) {
                Text(stringResource(R.string.write_settings_required))
                OutlinedButton(onClick = { context.startActivity(vm.writeSettingsIntent()) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.open_write_settings)) }
            }
            Button(onClick = { vm.save(LimitSettings(enabled, brightness.toInt(), volume.toInt())); savedMessage = true }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.save_apply)) }
            if (savedMessage) Text(stringResource(R.string.saved), color = MaterialTheme.colorScheme.primary)
            ScreenTimeCard(vm, owner)
            TextButton(onClick = onChangePin) { Text(stringResource(R.string.change_pin)) }
            Text(stringResource(R.string.device_setup_hint), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun LimitSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column { Row { Text(label, Modifier.weight(1f)); Text(stringResource(R.string.percent_value, value.toInt())) }
        Slider(value, onChange, valueRange = range, modifier = Modifier.fillMaxWidth()) }
}

@Composable
private fun ScreenTimeCard(vm: MainViewModel, owner: Boolean) {
    val saved by vm.screenTimeSettings.collectAsStateWithLifecycle()
    var enabled by remember(saved) { mutableStateOf(saved.enabled) }
    var activeMinutes by remember(saved) { mutableFloatStateOf(saved.activeMinutes.toFloat()) }
    var breakMinutes by remember(saved) { mutableFloatStateOf(saved.breakMinutes.toFloat()) }
    var reduceMinutes by remember(saved) { mutableFloatStateOf(saved.reduceBreakMinutesPerCorrectAnswer.toFloat()) }
    var hardLockMinutes by remember(saved) { mutableFloatStateOf(saved.hardLockMinutes.toFloat()) }
    var cooldownSeconds by remember(saved) { mutableFloatStateOf(saved.challengeCooldownSeconds.toFloat()) }
    var savedMessage by remember { mutableStateOf(false) }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.screen_time_title), style = MaterialTheme.typography.titleMedium)
            if (!owner) {
                Text(stringResource(R.string.screen_time_requires_owner))
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.screen_time_enabled), Modifier.weight(1f)); Switch(enabled, { enabled = it })
                }
                MinutesSlider(stringResource(R.string.screen_time_active_minutes), activeMinutes, 15f..180f) { activeMinutes = it }
                MinutesSlider(stringResource(R.string.screen_time_break_minutes), breakMinutes, 5f..60f) { breakMinutes = it }
                MinutesSlider(stringResource(R.string.screen_time_reduce_minutes), reduceMinutes, 1f..15f) { reduceMinutes = it }
                val maxHardLock = (breakMinutes.toInt() - 1).coerceAtLeast(1).toFloat()
                MinutesSlider(stringResource(R.string.screen_time_hard_lock_minutes), hardLockMinutes.coerceAtMost(maxHardLock), 1f..maxHardLock) { hardLockMinutes = it }
                SecondsSlider(stringResource(R.string.screen_time_cooldown_seconds), cooldownSeconds, 3f..10f) { cooldownSeconds = it }
                Button(onClick = {
                    vm.saveScreenTime(
                        ScreenTimeSettings(
                            enabled = enabled,
                            activeMinutes = activeMinutes.toInt(),
                            breakMinutes = breakMinutes.toInt(),
                            reduceBreakMinutesPerCorrectAnswer = reduceMinutes.toInt(),
                            hardLockMinutes = hardLockMinutes.toInt(),
                            challengeCooldownSeconds = cooldownSeconds.toInt(),
                        )
                    )
                    savedMessage = true
                }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.save_apply)) }
                if (savedMessage) Text(stringResource(R.string.saved), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun MinutesSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column { Row { Text(label, Modifier.weight(1f)); Text(stringResource(R.string.minutes_value, value.toInt())) }
        Slider(value, onChange, valueRange = range, modifier = Modifier.fillMaxWidth()) }
}

@Composable
private fun SecondsSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column { Row { Text(label, Modifier.weight(1f)); Text(stringResource(R.string.seconds_value, value.toInt())) }
        Slider(value, onChange, valueRange = range, modifier = Modifier.fillMaxWidth()) }
}
