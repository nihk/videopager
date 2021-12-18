package com.videopager.ui.extensions

import com.videopager.models.ViewState
import com.videopager.vm.PlayerSavedStateHandle

fun ViewState(handle: PlayerSavedStateHandle): ViewState {
    return ViewState(
        page = handle.get()?.currentMediaItemIndex ?: 0
    )
}
