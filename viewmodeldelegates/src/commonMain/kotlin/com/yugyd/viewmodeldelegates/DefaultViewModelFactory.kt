package com.yugyd.viewmodeldelegates

import kotlinx.coroutines.CoroutineDispatcher

class DefaultViewModelFactory : ViewModelFactory {

    override fun <Event : Any, State : Any> create(
        initialState: State,
        viewModelDelegates: Set<ViewModelDelegate<Event, State>>,
        initEvents: Set<Event>,
        autoInit: Boolean,
        mainImmediateCoroutineDispatcher: CoroutineDispatcher,
        logger: ViewModelLogger?,
        name: String?,
    ): ViewModel<Event, State> = DefaultViewModelDelegatesImpl(
        initialState = initialState,
        viewModelDelegates = viewModelDelegates,
        initEvents = initEvents,
        mainImmediateCoroutineDispatcher = mainImmediateCoroutineDispatcher,
        logger = logger,
        name = name,
    ).apply {
        if (autoInit) {
            init()
        }
    }
}
