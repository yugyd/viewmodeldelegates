package com.yugyd.viewmodeldelegates

import com.yugyd.viewmodeldelegates.annotation.MainThread
import kotlinx.coroutines.flow.StateFlow

interface ViewModel<in Event : Any, State : Any> {

    val state: StateFlow<State>

    @MainThread
    fun accept(event: Event)

    @MainThread
    fun init()

    @MainThread
    fun dispose()

    @MainThread
    fun setState(newState: State)

    @MainThread
    fun updateState(transform: (currentState: State) -> State)
}
