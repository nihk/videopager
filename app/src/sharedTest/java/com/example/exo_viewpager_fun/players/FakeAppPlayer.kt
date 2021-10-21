package com.example.exo_viewpager_fun.players

import com.example.exo_viewpager_fun.models.PlayerState
import com.example.exo_viewpager_fun.models.VideoData
import kotlinx.coroutines.flow.Flow

class FakeAppPlayer(private val isPlayerRendering: Flow<Boolean>) : AppPlayer {
    override var currentPlayerState: PlayerState = PlayerState.INITIAL
    val setups = mutableListOf<List<VideoData>>()
    var playingMediaAt: Int = -1
    var didRelease: Boolean = false

    override fun setUpWith(videoData: List<VideoData>, playerState: PlayerState?) {
        setups += videoData
    }

    override fun isPlayerRendering() = isPlayerRendering

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
