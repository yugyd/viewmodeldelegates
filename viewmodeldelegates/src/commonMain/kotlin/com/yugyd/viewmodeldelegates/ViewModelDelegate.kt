package com.yugyd.viewmodeldelegates

import com.yugyd.viewmodeldelegates.annotation.MainThread
import kotlinx.coroutines.CoroutineScope

interface ViewModelDelegate<Event : Any, State : Any> {

    @MainThread
    fun accept(
        event: Event,
        viewModel: ViewModelDelegates<Event, State>,
        scope: CoroutineScope,
        getState: () -> State,
    ): Boolean
}
