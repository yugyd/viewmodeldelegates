package com.yugyd.viewmodeldelegates.ui

fun interface StateToModelMapper<in State, out Model> {
    fun map(state: State): Model
}
