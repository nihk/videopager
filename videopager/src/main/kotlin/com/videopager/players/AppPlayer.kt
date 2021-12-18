package com.videopager.players

import com.videopager.models.PlayerState
import com.videopager.models.VideoData
import kotlinx.coroutines.flow.Flow

// Abstract the underlying player to facilitate testing and hide player implementation details.
interface AppPlayer {
    val currentPlayerState: PlayerState

    suspend fun setUpWith(videoData: List<VideoData>, playerState: PlayerState?)
    fun onPlayerRendering(): Flow<Unit>
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
