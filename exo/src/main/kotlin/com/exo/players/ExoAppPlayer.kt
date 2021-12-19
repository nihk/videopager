package com.exo.players

import android.content.Context
import com.exo.data.VideoDataUpdater
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.videopager.models.PlayerState
import com.videopager.models.VideoData
import com.videopager.players.AppPlayer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ExoAppPlayer(
    internal val exoPlayer: ExoPlayer,
    private val updater: VideoDataUpdater
) : AppPlayer {
    override val currentPlayerState: PlayerState get() = exoPlayer.toPlayerState()
    private var isPlayerSetUp = false

    override suspend fun setUpWith(videoData: List<VideoData>, playerState: PlayerState?) {
        /** Delegate video insertion, removing, moving, etc. to this [updater] */
        updater.update(exoPlayer = exoPlayer, incoming = videoData)

        // Player should only have saved state restored to it one time per instance of this class.
        if (!isPlayerSetUp) {
            setUpPlayerState(playerState)
            isPlayerSetUp = true
        }

        exoPlayer.prepare()
    }

    private fun setUpPlayerState(playerState: PlayerState?) {
        val currentMediaItems = exoPlayer.currentMediaItems

         // When restoring saved state, the saved media item might be not be in the player's current
         // collection of media items. In that case, the saved media item cannot be restored.
        val canRestoreSavedPlayerState = playerState != null
            && currentMediaItems.any { mediaItem -> mediaItem.mediaId == playerState.currentMediaItemId }

        val reconciledPlayerState = if (canRestoreSavedPlayerState) {
            requireNotNull(playerState)
        } else {
            PlayerState.INITIAL
        }

        val windowIndex = currentMediaItems.indexOfFirst { mediaItem ->
            mediaItem.mediaId == reconciledPlayerState.currentMediaItemId
        }
        if (windowIndex != -1) {
            exoPlayer.seekTo(windowIndex, reconciledPlayerState.seekPositionMillis)
        }
        exoPlayer.playWhenReady = reconciledPlayerState.isPlaying
    }

    // A signal that video content is immediately ready to play; any preview images
    // on top of the video can be hidden to reveal actual video playback underneath.
    override fun onPlayerRendering(): Flow<Unit> = callbackFlow {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                trySend(Unit)
            }
        }

        exoPlayer.addListener(listener)

        awaitClose { exoPlayer.removeListener(listener) }
    }

    override fun errors(): Flow<Throwable> = callbackFlow {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                trySend(error)
            }
        }

        exoPlayer.addListener(listener)

        awaitClose { exoPlayer.removeListener(listener) }
    }

    private fun ExoPlayer.toPlayerState(): PlayerState {
        return PlayerState(
            currentMediaItemId = currentMediaItem?.mediaId,
            currentMediaItemIndex = currentMediaItemIndex,
            seekPositionMillis = currentPosition,
            isPlaying = playWhenReady
        )
    }

    override fun playMediaAt(position: Int) {
        // Already playing media at this position; nothing to do
        if (exoPlayer.currentMediaItemIndex == position && exoPlayer.isPlaying) return

        exoPlayer.seekToDefaultPosition(position)
        exoPlayer.playWhenReady = true
        exoPlayer.prepare() // Recover from any errors that may have happened at previous media positions
    }

    override fun play() {
        exoPlayer.prepare() // Recover from any errors
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun release() {
        exoPlayer.release()
    }

    class Factory(
        context: Context,
        private val updater: com.exo.data.VideoDataUpdater
    ) : AppPlayer.Factory {
        // Use application context to avoid leaking Activity.
        private val appContext = context.applicationContext

        override fun create(config: AppPlayer.Factory.Config): AppPlayer {
            val exoPlayer = ExoPlayer.Builder(appContext)
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
