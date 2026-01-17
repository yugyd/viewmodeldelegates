package com.yugyd.viewmodeldelegates

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class ViewModelDelegates<in Event : Any, State : Any>(
    initialState: State,
    mainCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
    private val eventDelegates: Set<ViewModelDelegate<Event, State>>,
    private val logger: ViewModelLogger? = null,
    private val name: String? = null,
    private val initEvents: Set<Event> = emptySet(),
) : ViewModel<Event, State> {

    private val _state = MutableStateFlow(initialState)
    final override val state: StateFlow<State> = _state.asStateFlow()

    private val getState: () -> State = {
        _state.value
    }

    private val isLoggerEnabled: Boolean = name != null && logger != null

    private val viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + mainCoroutineDispatcher,
    )

    init {
        if (eventDelegates.isEmpty() && isLoggerEnabled) {
            logger?.throwIfDebug(
                IllegalStateException(
                    "$name: ViewModel has no event delegates to handle events",
                ),
            )
        }
    }

    final override fun init() {
        if (isLoggerEnabled) {
            logger?.log("$name: initializing")
        }

        initEvents.forEach { event ->
            accept(event)
        }
    }

    final override fun accept(event: Event) {
        if (isLoggerEnabled) {
            logger?.log("$name: event accepted: $event")
        }

        val lastIndex = eventDelegates.size - 1

        eventDelegates.forEachIndexed { index, delegate ->
            val isHandled = delegate.accept(
                event = event,
                viewModel = this,
                scope = viewModelScope,
                getState = getState,
            )

            when {
                isHandled -> return

                index == lastIndex -> {
                    logger?.throwIfDebug(
                        IllegalStateException("$name: event not handled: $event")
                    )
                }

                else -> Unit
            }
        }
    }

    final override fun dispose() {
        viewModelScope.cancel()

        if (isLoggerEnabled) {
            logger?.log("$name: disposed")
        }
    }

    final override fun setState(newState: State) {
        _state.update {
            logNewState(newState)
            newState
        }
    }

    final override fun updateState(transform: (currentState: State) -> State) {
        _state.update { currentState ->
            val newState = transform(currentState)
            logNewState(newState)
            newState
        }
    }

    private fun logNewState(newState: State) {
        if (isLoggerEnabled) {
            logger?.log("$name: state changed: $newState")
        }
    }
}
