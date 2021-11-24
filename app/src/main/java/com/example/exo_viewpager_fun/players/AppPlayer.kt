package com.example.exo_viewpager_fun.players

import com.example.exo_viewpager_fun.models.PlayerState
import com.example.exo_viewpager_fun.models.VideoData
import kotlinx.coroutines.flow.Flow

// Abstract the underlying player to facilitate testing and hide player implementation details.
interface AppPlayer {
    val currentPlayerState: PlayerState

    fun setUpWith(videoData: List<VideoData>, playerState: PlayerState?)
    fun isPlayerRendering(): Flow<Boolean>
    fun errors(): Flow<Throwable>
    fun playMediaAt(position: Int)
    fun play()
    fun pause()
    fun release()

    interface Factory {
        fun create(config: Config): AppPlayer

        data class Config(
            val loopVideos: Boolean = false
        )
    }
}
