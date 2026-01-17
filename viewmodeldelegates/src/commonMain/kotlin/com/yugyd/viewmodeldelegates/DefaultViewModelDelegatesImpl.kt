package com.yugyd.viewmodeldelegates

import kotlinx.coroutines.CoroutineDispatcher

internal class DefaultViewModelDelegatesImpl<Event : Any, State : Any>(
    initialState: State,
    viewModelDelegates: Set<ViewModelDelegate<Event, State>>,
    mainImmediateCoroutineDispatcher: CoroutineDispatcher,
    logger: ViewModelLogger? = null,
    name: String? = null,
    initEvents: Set<Event> = emptySet(),
) : ViewModelDelegates<Event, State>(
    initialState = initialState,
    eventDelegates = viewModelDelegates,
    mainImmediateCoroutineDispatcher = mainImmediateCoroutineDispatcher,
    logger = logger,
    name = name,
    initEvents = initEvents,
)
