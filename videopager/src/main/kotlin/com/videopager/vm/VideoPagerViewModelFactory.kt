package com.videopager.vm

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import com.videopager.data.VideoDataRepository
import com.player.players.AppPlayer
import com.videopager.ui.extensions.ViewState

class VideoPagerViewModelFactory(
    private val repository: VideoDataRepository,
    private val appPlayerFactory: AppPlayer.Factory
) {
    fun create(owner: SavedStateRegistryOwner): ViewModelProvider.Factory {
        return object : AbstractSavedStateViewModelFactory(owner, null) {
            override fun <T : ViewModel?> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                val playerSavedStateHandle = PlayerSavedStateHandle(handle)

                @Suppress("UNCHECKED_CAST")
                return VideoPagerViewModel(
                    repository = repository,
                    appPlayerFactory = appPlayerFactory,
                    handle = playerSavedStateHandle,
                    initialState = ViewState(playerSavedStateHandle)
                ) as T
            }
        }
    }
}
