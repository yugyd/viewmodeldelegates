package com.yugyd.viewmodeldelegates.sample.ui.home.di

import com.yugyd.viewmodeldelegates.DefaultViewModelFactory
import com.yugyd.viewmodeldelegates.ViewModel
import com.yugyd.viewmodeldelegates.ViewModelLogger
import com.yugyd.viewmodeldelegates.sample.ui.home.domain.HomeViewModel
import com.yugyd.viewmodeldelegates.sample.ui.home.domain.HomeViewModel.Event
import com.yugyd.viewmodeldelegates.sample.ui.home.domain.HomeViewModel.State
import com.yugyd.viewmodeldelegates.sample.ui.home.domain.LoadDataViewModelDelegate
import com.yugyd.viewmodeldelegates.sample.ui.home.domain.OnActionClickedViewModelDelegate
import com.yugyd.viewmodeldelegates.sample.ui.home.domain.OnNavigationHandledViewModelDelegate
import com.yugyd.viewmodeldelegates.sample.ui.home.domain.OnSnackbarDismissedViewModelDelegate
import com.yugyd.viewmodeldelegates.sample.ui.home.domain.data.HomeRepositoryImpl
import com.yugyd.viewmodeldelegates.sample.ui.home.ui.DispatchersProviderImpl
import com.yugyd.viewmodeldelegates.sample.ui.home.ui.HomeBinder
import com.yugyd.viewmodeldelegates.sample.ui.home.ui.HomeMapper

private fun buildLogger() = object : ViewModelLogger {
    override fun log(message: String) {
        println(message)
    }

    override fun throwIfDebug(error: Throwable) {
        throw error
    }
}

fun buildHomeBinder(): HomeBinder {
    val arguments = State.Arguments(
        userName = "Default User",
    )

    val dispatchersProvider = DispatchersProviderImpl()

    val repository = HomeRepositoryImpl()

    val delegates = setOf(
        LoadDataViewModelDelegate(repository = repository),
        OnActionClickedViewModelDelegate(),
        OnNavigationHandledViewModelDelegate(),
        OnSnackbarDismissedViewModelDelegate(),
    )

    val viewModel = object : HomeViewModel,
        ViewModel<Event, State> by DefaultViewModelFactory().create(
            initialState = State(arguments = arguments),
            viewModelDelegates = delegates,
            initEvents = setOf(Event.LoadData),
            logger = buildLogger(),
            name = "HomeViewModel",
            mainImmediateCoroutineDispatcher = dispatchersProvider.main,
        ) {}
    val mapper = HomeMapper()
    return HomeBinder(
        viewModel = viewModel,
        mapper = mapper,
        dispatchersProvider = dispatchersProvider,
    )
}
