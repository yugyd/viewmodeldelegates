package com.yugyd.viewmodeldelegates.sample.ui.home.domain

import com.yugyd.viewmodeldelegates.ViewModel
import com.yugyd.viewmodeldelegates.ViewModelDelegate
import com.yugyd.viewmodeldelegates.sample.ui.home.domain.HomeViewModel.Event
import com.yugyd.viewmodeldelegates.sample.ui.home.domain.HomeViewModel.State

interface HomeViewModel : ViewModel<Event, State> {

    sealed interface Event {
        object LoadData : Event
        object OnActionClicked : Event
        object OnSnackbarDismissed : Event
        object OnNavigationHandled : Event
    }

    data class State(
        val arguments: Arguments = Arguments(),
        val isLoading: Boolean = false,
        val isWarning: Boolean = false,
        val message: String = "",
        val showErrorMessage: Boolean = false,
        val navigationState: NavigationState? = null,
    ) {

        data class Arguments(
            val userName: String = "",
        )

        sealed interface NavigationState {
            object NavigateToFavourites :
                NavigationState
        }
    }
}

typealias HomeViewModelDelegate = ViewModelDelegate<Event, State>
