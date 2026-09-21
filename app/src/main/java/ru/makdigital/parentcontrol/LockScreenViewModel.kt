package ru.makdigital.parentcontrol

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.makdigital.parentcontrol.data.ScreenTimeRepository
import ru.makdigital.parentcontrol.model.ScreenTimeCycleState
import ru.makdigital.parentcontrol.model.ScreenTimeSettings
import ru.makdigital.parentcontrol.policy.MathChallenge
import ru.makdigital.parentcontrol.policy.ScreenTimeEngine
import ru.makdigital.parentcontrol.security.PinManager

class LockScreenViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ScreenTimeRepository(app)
    private val engine = ScreenTimeEngine(app)
    private val pin = PinManager(app)

    // Seeded null (not a default ScreenTimeCycleState()) so the UI never treats an unloaded
    // flow's placeholder phase as real: acting on it before the persisted LOCKED state arrives
    // would let the lock screen close itself immediately after launch.
    val cycleState: StateFlow<ScreenTimeCycleState?> =
        repo.cycleState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val config = repo.config.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenTimeSettings())

    fun submitAnswer(problem: MathChallenge.Problem, answer: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch { onResult(engine.submitAnswer(problem, answer)) }
    }

    /** Parent emergency exit: correct PIN ends the current lock only, feature stays enabled. */
    fun tryParentOverride(pinValue: String): Boolean {
        val ok = pin.verify(pinValue.toCharArray())
        if (ok) viewModelScope.launch { engine.parentOverrideEndLock() }
        return ok
    }
}
