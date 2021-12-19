package com.videopager.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal abstract class MviViewModel<Event, Result, State, Effect>(initialState: State) : ViewModel() {
    val states: StateFlow<State>
    val effects: Flow<Effect>
    private val events = MutableSharedFlow<Event>()

    init {
        events
            .onSubscription {
                check(events.subscriptionCount.value == 1)
                onStart()
            }
            .share() // Share emissions to individual Flows within toResults()
            .toResults()
            .share() // Share emissions to states and effects
            .also { results ->
                states = results.toStates(initialState)
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.Lazily,
                        initialValue = initialState
                    )
                effects = results.toEffects()
            }
    }

    fun processEvent(event: Event) {
        viewModelScope.launch {
            events.emit(event)
        }
    }

    private fun <T> Flow<T>.share(): Flow<T> {
        return shareIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily
        )
    }

    protected open fun onStart() = Unit
    protected abstract fun Flow<Event>.toResults(): Flow<Result>
    protected abstract fun Result.reduce(state: State): State
    protected open fun Flow<Result>.toEffects(): Flow<Effect> = emptyFlow()

    private fun Flow<Result>.toStates(initialState: State): Flow<State> {
        return scan(initialState) { state, result -> result.reduce(state) }
    }
}
