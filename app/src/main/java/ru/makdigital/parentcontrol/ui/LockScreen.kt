package ru.makdigital.parentcontrol.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import ru.makdigital.parentcontrol.LockScreenViewModel
import ru.makdigital.parentcontrol.R
import ru.makdigital.parentcontrol.model.ScreenTimePhase
import ru.makdigital.parentcontrol.policy.MathChallenge
import ru.makdigital.parentcontrol.policy.ScreenTimeMath

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LockScreen(vm: LockScreenViewModel, onUnlocked: () -> Unit) {
    val loadedState by vm.cycleState.collectAsStateWithLifecycle()
    val settings by vm.config.collectAsStateWithLifecycle()
    // The flow starts as null until the persisted state loads; never act on a phase we haven't
    // actually read yet (see the comment on LockScreenViewModel.cycleState).
    val state = loadedState ?: return
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }
    LaunchedEffect(state.phase) {
        if (state.phase == ScreenTimePhase.ACTIVE) onUnlocked()
    }

    var problem by remember { mutableStateOf(MathChallenge.generate()) }
    var answer by remember { mutableStateOf("") }
    var wrongAnswer by remember { mutableStateOf(false) }
    var showPinField by remember { mutableStateOf(false) }
    var pinValue by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val hardLockElapsed = ScreenTimeMath.isHardLockElapsed(state, settings, now)
    val onCooldown = ScreenTimeMath.isChallengeOnCooldown(state, settings, now)
    val remainingMs = ScreenTimeMath.remainingBreakMillis(state, now)
    val remainingMinutes = (remainingMs / 60_000L).toInt()
    val remainingSeconds = ((remainingMs / 1_000L) % 60).toInt()

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(R.string.lock_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.combinedClickable(onClick = {}, onLongClick = { showPinField = true }),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.lock_break_remaining, remainingMinutes, remainingSeconds),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(24.dp))

            if (!hardLockElapsed) {
                Text(stringResource(R.string.lock_hard_lock_message))
            } else {
                ChallengeSection(
                    problem = problem,
                    answer = answer,
                    onAnswerChange = { answer = it },
                    wrongAnswer = wrongAnswer,
                    onCooldown = onCooldown,
                    onSubmit = {
                        val submitted = answer.toIntOrNull()
                        if (submitted != null) {
                            vm.submitAnswer(problem, submitted) { correct ->
                                wrongAnswer = !correct
                                problem = MathChallenge.generate()
                                answer = ""
                            }
                        }
                    },
                )
            }

            if (showPinField) {
                Spacer(Modifier.height(32.dp))
                ParentOverrideSection(
                    pinValue = pinValue,
                    onPinChange = { pinValue = it; pinError = false },
                    pinError = pinError,
                    onSubmit = {
                        if (!vm.tryParentOverride(pinValue)) pinError = true
                        pinValue = ""
                    },
                )
            }
        }
    }
}

@Composable
private fun ChallengeSection(
    problem: MathChallenge.Problem,
    answer: String,
    onAnswerChange: (String) -> Unit,
    wrongAnswer: Boolean,
    onCooldown: Boolean,
    onSubmit: () -> Unit,
) {
    val wrongAnswerMessage = stringResource(R.string.lock_answer_wrong)
    Text(stringResource(R.string.lock_challenge_prompt, problem.a, problem.b))
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = answer,
        onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) onAnswerChange(it) },
        label = { Text(stringResource(R.string.lock_answer_label)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
    if (wrongAnswer) {
        Spacer(Modifier.height(4.dp))
        Text(wrongAnswerMessage, color = MaterialTheme.colorScheme.error)
    }
    Spacer(Modifier.height(8.dp))
    Button(onClick = onSubmit, enabled = !onCooldown && answer.isNotEmpty()) {
        Text(stringResource(R.string.lock_submit))
    }
    if (onCooldown) {
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.lock_cooldown_message), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ParentOverrideSection(
    pinValue: String,
    onPinChange: (String) -> Unit,
    pinError: Boolean,
    onSubmit: () -> Unit,
) {
    OutlinedTextField(
        value = pinValue,
        onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) onPinChange(it) },
        label = { Text(stringResource(R.string.pin_label)) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
    )
    if (pinError) {
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.pin_wrong), color = MaterialTheme.colorScheme.error)
    }
    Spacer(Modifier.height(8.dp))
    Button(onClick = onSubmit, enabled = pinValue.isNotEmpty()) {
        Text(stringResource(R.string.continue_label))
    }
}
