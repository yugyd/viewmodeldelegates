package com.yugyd.viewmodeldelegates.ui

import androidx.annotation.CallSuper
import com.yugyd.viewmodeldelegates.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

abstract class ModelViewModelBinder<Event : Any, State : Any, Model : Any>(
    private val viewModel: ViewModel<Event, State>,
    stateToModelMapper: StateToModelMapper<State, Model>,
    initialModel: Model,
    mainImmediateCoroutineDispatcher: CoroutineDispatcher,
) : ViewModelBinder(mainImmediateCoroutineDispatcher = mainImmediateCoroutineDispatcher) {

    val model: StateFlow<Model> = viewModel.state
        .map(stateToModelMapper::map)
        .stateIn(
            scope = binderScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = CHANGE_CONFIGURATION_TIMEOUT_MILLS,
            ),
            initialValue = initialModel,
        )

    @CallSuper
    override fun unbind() {
        viewModel.dispose()
    }

    companion object {
        private const val CHANGE_CONFIGURATION_TIMEOUT_MILLS = 5000L
    }
}
