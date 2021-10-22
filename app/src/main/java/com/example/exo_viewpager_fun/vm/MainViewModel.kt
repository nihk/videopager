package com.example.exo_viewpager_fun.vm

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.exo_viewpager_fun.models.ShowPauseAnimation
import com.example.exo_viewpager_fun.models.ShowPlayAnimation
import com.example.exo_viewpager_fun.data.VideoDataRepository
import com.example.exo_viewpager_fun.models.AttachPlayerViewToPage
import com.example.exo_viewpager_fun.models.ResetAnyPlayPauseAnimations
import com.example.exo_viewpager_fun.models.SettledOnPage
import com.example.exo_viewpager_fun.models.TappedPlayer
import com.example.exo_viewpager_fun.models.ViewEffect
import com.example.exo_viewpager_fun.models.ViewEvent
import com.example.exo_viewpager_fun.models.ViewState
import com.example.exo_viewpager_fun.players.AppPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds a stateful [AppPlayer] instance that will frequently get created and torn down as [getPlayer]
 * and [tearDown] are invoked in parallel to Activity lifecycle state changes.
 */
class MainViewModel(
    private val repository: VideoDataRepository,
    private val appPlayerFactory: AppPlayer.Factory,
    private val handle: PlayerSavedStateHandle
) : ViewModel() {
    private var appPlayer: AppPlayer? = null
    private var listening: Job? = null

    // State that's persisted in-memory.
    private val viewStates = MutableStateFlow(ViewState())
    fun viewStates(): StateFlow<ViewState> = viewStates

    // One-shot side-effects.
    private val viewEffects = MutableSharedFlow<ViewEffect>()
    fun viewEffects(): SharedFlow<ViewEffect> = viewEffects

    init {
        repository.videoData()
            .onEach { videoData ->
                appPlayer?.setUpWith(videoData, handle.get())
                viewStates.update { it.copy(videoData = videoData) }
            }
            .launchIn(viewModelScope)
    }

    // Returns any active player instance or creates a new one.
    fun getPlayer(): AppPlayer {
        return appPlayer ?: appPlayerFactory.create(
            config = AppPlayer.Factory.Config(loopVideos = true)
        ).apply {
            listening = isPlayerRendering()
                .onEach { isPlayerRendering -> viewStates.update { it.copy(showPlayer = isPlayerRendering) } }
                .launchIn(viewModelScope)
            viewStates.value.videoData?.let { setUpWith(it, handle.get()) }
        }.also {
            appPlayer = it
        }
    }

    // Videos are a heavy resource, so tear player down when the app is not in the foreground.
    fun tearDown() {
        listening?.cancel()
        listening = null
        appPlayer?.run {
            // Keep track of player state so that it can be restored across player recreations.
            handle.set(currentPlayerState)
            release()
        }
        appPlayer = null
    }

    fun processEvent(viewEvent: ViewEvent) {
        when (viewEvent) {
            is TappedPlayer -> onPlayerTapped()
            is SettledOnPage -> onPageSettled(viewEvent.page)
        }
    }

    private fun onPlayerTapped() {
        val appPlayer = requireNotNull(appPlayer)
        val viewEffect = if (appPlayer.currentPlayerState.isPlaying) {
            appPlayer.pause()
            ShowPauseAnimation
        } else {
            appPlayer.play()
            ShowPlayAnimation
        }
        viewModelScope.launch {
            viewEffects.emit(viewEffect)
        }
    }

    private fun onPageSettled(page: Int) {
        val didChangeMedia = playMediaAt(page)
        viewModelScope.launch {
            if (didChangeMedia) {
                viewEffects.emit(ResetAnyPlayPauseAnimations)
            }
            viewEffects.emit(AttachPlayerViewToPage(page))
        }
    }

    /**
     * @return Whether the media position was actually changed.
     */
    private fun playMediaAt(position: Int): Boolean {
        val appPlayer = requireNotNull(appPlayer)
        if (appPlayer.currentPlayerState.currentMediaIndex == position) {
            /** Already playing the media at [position] */
            return false
        }

        /** Tell UI to hide player while player is loading content at [position]. */
        viewStates.update { it.copy(showPlayer = false) }

        appPlayer.playMediaAt(position)
        return true
    }

    class Factory(
        private val repository: VideoDataRepository,
        private val appPlayerFactory: AppPlayer.Factory,
        savedStateRegistryOwner: SavedStateRegistryOwner
    ) : AbstractSavedStateViewModelFactory(savedStateRegistryOwner, null) {
        override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                repository = repository,
                appPlayerFactory = appPlayerFactory,
                handle = PlayerSavedStateHandle(handle)
            ) as T
        }
    }
}
