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

@SuppressLint("StaticFieldLeak")
class MainViewModel(
    private val appContext: Context,
    private val repository: VideoDataRepository,
    private val updater: VideoDataUpdater,
    private val handle: SavedStateHandle
) : ViewModel() {
    private var exoPlayer: ExoPlayer? = null
    private var listening: Job? = null

    private val isPlayerRendering = MutableStateFlow(false)
    fun isPlayerRendering(): Flow<Boolean> = isPlayerRendering

    val videoData = repository.videoData()
        .onEach { videoData -> exoPlayer?.setUpWith(videoData) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun getPlayer(): ExoPlayer {
        return exoPlayer ?: SimpleExoPlayer.Builder(appContext)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE // Loop the active video

                listening = isPlayerRendering()
                    .onEach { isPlayerRendering -> this@MainViewModel.isPlayerRendering.value = isPlayerRendering }
                    .launchIn(viewModelScope)

                setUpWith(videoData.value)
                prepare()
            }
            .also { exoPlayer = it }
    }

    private fun ExoPlayer.setUpWith(videoData: List<VideoData>) {
        if (videoData.isEmpty()) return
        val isInitializing = currentMediaItem == null

        updater.update(this, videoData)

        val playerState = if (isInitializing) {
            val savedPlayerState: PlayerState? = handle[KEY_PLAYER_STATE]
            // When restoring saved state, the saved window index might be unavailable, e.g. if
            // the saved index before process death was from a data set not known immediately after
            // process restoration.
            if (savedPlayerState == null || savedPlayerState.playlistPosition >= mediaItemCount) {
                PlayerState.INITIAL
            } else {
                savedPlayerState
            }
        } else {
            toPlayerState()
        }

        seekTo(playerState.playlistPosition, playerState.seekPositionMillis)
        playWhenReady = playerState.isPlaying
    }

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
            // Already playing the MediaItem at $position
            return
        }

        isPlayerRendering.value = false

        exoPlayer.seekToDefaultPosition(position)
        // In case previous media content was paused by user.
        exoPlayer.playWhenReady = true
    }

    fun tearDown() {
        listening?.cancel()
        listening = null
        exoPlayer?.run {
            handle[KEY_PLAYER_STATE] = toPlayerState()
            release()
        }
        exoPlayer = null
    }

    private fun ExoPlayer.toPlayerState(): PlayerState {
        return PlayerState(
            playlistPosition = currentWindowIndex,
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
