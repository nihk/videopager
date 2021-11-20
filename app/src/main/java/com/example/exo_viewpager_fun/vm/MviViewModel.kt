package com.example.exo_viewpager_fun.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class MviViewModel<Event, Result, State, Effect>(
    initialState: State
) : ViewModel() {
    val states: StateFlow<State>
    val effects: Flow<Effect>
    private val events = MutableSharedFlow<Event>()

    init {
        events.toResults()
            .shareIn( // Share emissions to states and effects
                scope = viewModelScope,
                replay = Int.MAX_VALUE, // Carry forward any events emitted before states/effects collection
                started = SharingStarted.Eagerly // Allow event processing immediately
            )
            .also { results ->
                // Delay consuming results replay cache until the time of subscription
                val started = SharingStarted.Lazily

                states = results.toStates(initialState)
                    .stateIn(
                        scope = viewModelScope,
                        started = started,
                        initialValue = initialState
                    )
                effects = results.toEffects()
                    .shareIn(
                        scope = viewModelScope,
                        started = started
                    )
            }
    }

    fun processEvent(event: Event) {
        viewModelScope.launch {
            events.emit(event)
        }
    }

    protected abstract fun Flow<Event>.toResults(): Flow<Result>
    protected abstract fun Result.reduce(state: State): State
    protected open fun Flow<Result>.toEffects(): Flow<Effect> = emptyFlow()

    private fun Flow<Result>.toStates(initialState: State): Flow<State> {
        return scan(initialState) { state, result -> result.reduce(state) }
    }
}
