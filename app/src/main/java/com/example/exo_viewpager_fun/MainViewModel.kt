package com.example.exo_viewpager_fun

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer

@SuppressLint("StaticFieldLeak")
class MainViewModel(
    private val appContext: Context,
    private val handle: SavedStateHandle
) : ViewModel() {
    private var exoPlayer: ExoPlayer? = null

    fun getPlayer(): ExoPlayer {
        return exoPlayer ?: SimpleExoPlayer.Builder(appContext)
            .build()
            .apply {
                val mediaItems = videoUris.map { videoUri ->
                    MediaItem.fromUri(videoUri)
                }
                addMediaItems(mediaItems)
                prepare()
                repeatMode = Player.REPEAT_MODE_ONE

                val initialState = handle[KEY_PLAYER_STATE] ?: PlayerState.INITIAL
                seekTo(initialState.playlistPosition, initialState.seekPositionMillis)
                playWhenReady = initialState.isPlaying
            }
            .also { exoPlayer = it }
    }

    fun playMediaAt(position: Int) {
        val exoPlayer = requireNotNull(exoPlayer)
        if (exoPlayer.currentWindowIndex == position) {
            // Already playing the MediaItem at $position
            return
        }

        exoPlayer.seekToDefaultPosition(position)
        // In case content was paused by user.
        exoPlayer.playWhenReady = true
    }

    fun tearDown() {
        val exoPlayer = requireNotNull(exoPlayer)
        handle[KEY_PLAYER_STATE] = exoPlayer.toPlayerState()
        exoPlayer.release()
        this.exoPlayer = null
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
        owner: SavedStateRegistryOwner
    ) : AbstractSavedStateViewModelFactory(owner, null) {
        override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(appContext, handle) as T
        }
    }
}
