package com.example.exo_viewpager_fun

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

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

    // Emissions dictate player visibility in UI.
    private val showPlayer = MutableStateFlow(false)
    fun showPlayer(): Flow<Boolean> = showPlayer

    // Emit video data to the UI (ViewPager2) so it can render video image previews and send
    // page position changes back to this ViewModel.
    val videoData = repository.videoData()
        .onEach { videoData -> appPlayer?.setUpWith(videoData, handle.get()) }
        .stateIn(
            scope = viewModelScope,
            // Repository video data might be slow to fetch, so start this as early as possible.
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // Returns any active player instance or creates a new one.
    fun getPlayer(): AppPlayer {
        return appPlayer ?: appPlayerFactory.create(
            config = AppPlayer.Factory.Config(loopVideos = true)
        ).apply {
            listening = isPlayerRendering()
                .onEach { isPlayerRendering -> showPlayer.value = isPlayerRendering }
                .launchIn(viewModelScope)
            setUpWith(videoData.value, handle.get())
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

    fun playMediaAt(position: Int) {
        val appPlayer = requireNotNull(appPlayer)
        if (appPlayer.currentMediaIndex == position) {
            /** Already playing the media at [position]; no-op. */
            return
        }

        /** Tell UI to hide player while player is loading content at [position]. */
        showPlayer.value = false

        appPlayer.playMediaAt(position)
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
