package com.videopager.players

import com.player.models.PlayerState
import com.player.players.AppPlayer
import com.player.models.VideoData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class FakeAppPlayer(
    private val onPlayerRendering: Flow<Unit>,
    private val errors: Flow<Throwable>
) : AppPlayer {
    override var currentPlayerState: PlayerState = PlayerState.INITIAL
    val setups = mutableListOf<List<VideoData>>()
    var playingMediaAt: Int = -1
    var didRelease: Boolean = false
    var didCancelOnPlayerRenderingFlow = false

    override suspend fun setUpWith(videoData: List<VideoData>, playerState: PlayerState?) {
        setups += videoData
    }

    override fun onPlayerRendering() = flow {
        try {
            emitAll(onPlayerRendering)
        } catch (ce: CancellationException) {
            didCancelOnPlayerRenderingFlow = true
        }
    }

    override fun errors(): Flow<Throwable> = errors

    override fun playMediaAt(position: Int) {
        playingMediaAt = position
    }

    override fun pause() {
        currentPlayerState = currentPlayerState.copy(isPlaying = false)
    }

    override fun play() {
        currentPlayerState = currentPlayerState.copy(isPlaying = true)
    }

    override fun release() {
        didRelease = true
    }

    class Factory(private val appPlayer: AppPlayer) : AppPlayer.Factory {
        var createCount = 0

        override fun create(config: AppPlayer.Factory.Config): AppPlayer {
            ++createCount
            return appPlayer
        }
    }
}
