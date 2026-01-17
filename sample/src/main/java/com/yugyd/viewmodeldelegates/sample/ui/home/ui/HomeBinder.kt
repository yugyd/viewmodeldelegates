package com.yugyd.viewmodeldelegates.sample.ui.home.ui

import com.yugyd.viewmodeldelegates.sample.ui.home.domain.HomeViewModel
import com.yugyd.viewmodeldelegates.sample.ui.home.domain.HomeViewModel.Event
import com.yugyd.viewmodeldelegates.sample.ui.home.domain.HomeViewModel.State
import com.yugyd.viewmodeldelegates.sample.ui.home.ui.HomeBinder.Model
import com.yugyd.viewmodeldelegates.ui.ModelViewModelBinder

class HomeBinder(
    private val viewModel: HomeViewModel,
    mapper: HomeMapper,
    dispatchersProvider: DispatchersProvider,
) : ModelViewModelBinder<Event, State, Model>(
    viewModel = viewModel,
    initialModel = Model(),
    stateToModelMapper = mapper,
    mainImmediateCoroutineDispatcher = dispatchersProvider.main,
) {

    fun onActionClicked() {
        viewModel.accept(Event.OnActionClicked)
    }

    fun onSnackbarDismissed() {
        viewModel.accept(Event.OnSnackbarDismissed)
    }

    fun onNavigationHandled() {
        viewModel.accept(Event.OnNavigationHandled)
    }

    data class Model(
        val isLoading: Boolean = false,
        val isWarning: Boolean = false,
        val message: String = "",
        val showErrorMessage: Boolean = false,
        val navigationState: NavigationUiState? = null,
    ) {

        sealed interface NavigationUiState {
            object NavigateToFavourites : NavigationUiState
        }
    }
}
