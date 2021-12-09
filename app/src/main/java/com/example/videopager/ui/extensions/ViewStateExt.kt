package com.example.videopager.ui.extensions

import com.example.videopager.models.ViewState
import com.example.videopager.vm.PlayerSavedStateHandle

fun ViewState(handle: PlayerSavedStateHandle): ViewState {
    return ViewState(
        page = handle.get()?.currentMediaItemIndex ?: 0
    )
}
