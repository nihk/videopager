package com.example.exo_viewpager_fun

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// Abstract the underlying player to facilitate testing and hide player implementation details.
interface AppPlayer {
    val currentPlayerState: PlayerState

    fun setUpWith(videoData: List<VideoData>, playerState: PlayerState?)
    fun isPlayerRendering(): Flow<Boolean>
    fun playMediaAt(position: Int)
    fun release()

    interface Factory {
        fun create(config: Config): AppPlayer

        data class Config(
            val loopVideos: Boolean = false
        )
    }
}

class ExoAppPlayer(
    val exoPlayer: ExoPlayer,
    private val updater: VideoDataUpdater
) : AppPlayer {
    override val currentPlayerState: PlayerState get() = exoPlayer.toPlayerState()

    override fun setUpWith(videoData: List<VideoData>, playerState: PlayerState?) {
        // A signal to restore any saved video state.
        val isInitializing = exoPlayer.currentMediaItem == null

        /** Delegate video insertion, removing, moving, etc. to this [updater] */
        updater.update(exoPlayer = exoPlayer, incoming = videoData)
        val currentMediaItems = exoPlayer.currentMediaItems

        val reconciledPlayerState = if (isInitializing) {
            /**
             * When restoring saved state, the saved media item might be unavailable, e.g. if
             * the saved media item before process death was from a data set different than [videoData].
             */
            val canRestoreSavedPlayerState = playerState != null
                && currentMediaItems.any { mediaItem -> mediaItem.mediaId == playerState.currentMediaItemId }

            if (canRestoreSavedPlayerState) {
                requireNotNull(playerState)
            } else {
                PlayerState.INITIAL
            }
        } else {
            exoPlayer.toPlayerState()
        }

        val windowIndex = currentMediaItems.indexOfFirst { mediaItem ->
            mediaItem.mediaId == reconciledPlayerState.currentMediaItemId
        }
        if (windowIndex != -1) {
            exoPlayer.seekTo(windowIndex, reconciledPlayerState.seekPositionMillis)
        }
        exoPlayer.playWhenReady = reconciledPlayerState.isPlaying
        exoPlayer.prepare()
    }

    // A signal that video content is immediately ready to play; any preview images
    // on top of the video can be hidden to reveal actual video playback underneath.
    override fun isPlayerRendering(): Flow<Boolean> = callbackFlow {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                trySend(true)
            }
        }

        exoPlayer.addListener(listener)

        awaitClose { exoPlayer.removeListener(listener) }
    }

    private fun ExoPlayer.toPlayerState(): PlayerState {
        return PlayerState(
            currentMediaItemId = currentMediaItem?.mediaId,
            currentMediaIndex = currentWindowIndex,
            seekPositionMillis = currentPosition,
            isPlaying = isPlaying
        )
    }

    override fun playMediaAt(position: Int) {
        exoPlayer.seekToDefaultPosition(position)
        exoPlayer.playWhenReady = true
    }

    override fun release() {
        exoPlayer.release()
    }

    class Factory(
        context: Context,
        private val updater: VideoDataUpdater
    ) : AppPlayer.Factory {
        // Use application context to avoid leaking Activity.
        private val appContext = context.applicationContext

        override fun create(config: AppPlayer.Factory.Config): AppPlayer {
            val exoPlayer = SimpleExoPlayer.Builder(appContext)
                .build()
                .apply {
                    if (config.loopVideos) {
                        loopVideos()
                    }
                }
            return ExoAppPlayer(exoPlayer, updater)
        }
    }
}
