package com.yugyd.viewmodeldelegates.ui

import androidx.annotation.CallSuper
import com.yugyd.viewmodeldelegates.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow

abstract class StateViewModelBinder<Event : Any, State : Any>(
    private val viewModel: ViewModel<Event, State>,
    mainCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
) : ViewModelBinder(mainCoroutineDispatcher = mainCoroutineDispatcher) {

    val model: StateFlow<State> = viewModel.state

    @CallSuper
    override fun unbind() {
        viewModel.dispose()
    }
}
