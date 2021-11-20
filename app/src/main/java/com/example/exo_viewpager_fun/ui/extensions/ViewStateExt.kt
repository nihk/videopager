package com.example.exo_viewpager_fun.ui.extensions

import com.example.exo_viewpager_fun.models.ViewState
import com.example.exo_viewpager_fun.vm.PlayerSavedStateHandle

fun ViewState(handle: PlayerSavedStateHandle): ViewState {
    return ViewState(
        page = handle.get()?.currentMediaIndex ?: 0
    )
}
