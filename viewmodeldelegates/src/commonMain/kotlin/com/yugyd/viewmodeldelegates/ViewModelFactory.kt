package com.yugyd.viewmodeldelegates

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface ViewModelFactory {

    fun <Event : Any, State : Any> create(
        initialState: State,
        viewModelDelegates: Set<ViewModelDelegate<Event, State>>,
        initEvents: Set<Event> = emptySet(),
        autoInit: Boolean = true,
        mainCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
        logger: ViewModelLogger? = null,
        name: String? = null,
    ): ViewModel<Event, State>
}
