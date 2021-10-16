package com.example.exo_viewpager_fun

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

/**
 * Holds a stateful ExoPlayer instance that will frequently get created and torn down as [getPlayer]
 * and [tearDown] are called alongside Activity lifecycle state changes.
 */
@SuppressLint("StaticFieldLeak")
class MainViewModel(
    private val appContext: Context,
    private val repository: VideoDataRepository,
    private val updater: VideoDataUpdater,
    private val handle: SavedStateHandle
) : ViewModel() {
    private var exoPlayer: ExoPlayer? = null
    private var listening: Job? = null

    // Emissions dictate player visibility in UI.
    private val showPlayer = MutableStateFlow(false)
    fun showPlayer(): Flow<Boolean> = showPlayer

    // Emit video data to the UI (ViewPager2) so it can render video image previews and send
    // page position changes back to this ViewModel.
    val videoData = repository.videoData()
        .onEach { videoData -> exoPlayer?.setUpWith(videoData) }
        .stateIn(
            scope = viewModelScope,
            // Repository video data might be slow to fetch, so start this as early as possible.
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // Returns any active ExoPlayer instance or creates a new one.
    fun getPlayer(): ExoPlayer {
        return exoPlayer ?: SimpleExoPlayer.Builder(appContext)
            .build()
            .apply {
                loopVideos()

                listening = isPlayerRendering()
                    .onEach { isPlayerRendering -> showPlayer.value = isPlayerRendering }
                    .launchIn(viewModelScope)

                setUpWith(videoData.value)
                prepare()
            }
            .also { exoPlayer = it }
    }

    // Videos are a heavy resource, so tear ExoPlayer down when the app is not in the foreground.
    fun tearDown() {
        listening?.cancel()
        listening = null
        exoPlayer?.run {
            // Keep track of player state so that it can be restored across player recreations.
            handle[KEY_PLAYER_STATE] = toPlayerState()
            release()
        }
        exoPlayer = null
    }

    private fun ExoPlayer.setUpWith(videoData: List<VideoData>) {
        // A signal to restore any saved video state.
        val isInitializing = currentMediaItem == null

        /** Delegate video insertion, removing, moving, etc. to this [updater] */
        updater.update(exoPlayer = this, incoming = videoData)
        val currentMediaItems = currentMediaItems

        val playerState = if (isInitializing) {
            val savedPlayerState: PlayerState? = handle[KEY_PLAYER_STATE]
            /**
             * When restoring saved state, the saved media item might be unavailable, e.g. if
             * the saved media item before process death was from a data set different than [videoData].
             */
            val canRestoreSavedPlayerState = savedPlayerState != null
                && currentMediaItems.any { mediaItem -> mediaItem.mediaId == savedPlayerState.currentMediaItemId }

            if (canRestoreSavedPlayerState) {
                requireNotNull(savedPlayerState)
            } else {
                PlayerState.INITIAL
            }
        } else {
            toPlayerState()
        }

        val windowIndex = currentMediaItems.indexOfFirst { mediaItem ->
            mediaItem.mediaId == playerState.currentMediaItemId
        }
        if (windowIndex != -1) {
            seekTo(windowIndex, playerState.seekPositionMillis)
        }
        playWhenReady = playerState.isPlaying
    }

    // A signal that video content is immediately ready to play; any preview images
    // on top of the video can be hidden to reveal actual video playback underneath.
    private fun ExoPlayer.isPlayerRendering() = callbackFlow {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                trySend(true)
            }
        }

        addListener(listener)

        awaitClose { removeListener(listener) }
    }

    fun playMediaAt(position: Int) {
        val exoPlayer = requireNotNull(exoPlayer)
        if (exoPlayer.currentWindowIndex == position) {
            /** Already playing the MediaItem at [position]; no-op. */
            return
        }

        /** Tell UI to hide player while player is loading content at [position]. */
        showPlayer.value = false

        exoPlayer.seekToDefaultPosition(position)
        // In case previous media content was paused by user.
        exoPlayer.playWhenReady = true
    }

    private fun ExoPlayer.toPlayerState(): PlayerState {
        return PlayerState(
            currentMediaItemId = currentMediaItem?.mediaId,
            seekPositionMillis = currentPosition,
            isPlaying = isPlaying
        )
    }

    companion object {
        private const val KEY_PLAYER_STATE = "player_state"
    }

    class Factory(
        private val appContext: Context,
        private val repository: VideoDataRepository,
        private val updater: VideoDataUpdater,
        owner: SavedStateRegistryOwner
    ) : AbstractSavedStateViewModelFactory(owner, null) {
        override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(appContext, repository, updater, handle) as T
        }
    }
}
