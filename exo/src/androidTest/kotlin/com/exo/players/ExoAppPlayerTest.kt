package com.exo.players

import androidx.test.core.app.ApplicationProvider
import com.exo.data.DiffingVideoDataUpdater
import com.exo.data.TEST_VIDEO_DATA
import com.player.models.VideoData
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.player.models.PlayerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.Closeable

class ExoAppPlayerTest {
    @Test
    fun shouldRestorePlayerState_whenPlayerIsInitializing() = exoAppPlayer {
        val playerState = PlayerState(
            currentMediaItemId = "2", // See TEST_VIDEO_DATA
            currentMediaItemIndex = 1,
            seekPositionMillis = 500L,
            isPlaying = true
        )

        setUpWith(TEST_VIDEO_DATA, playerState)

        assertWindowIndex(1)
        assertSeekPosition(500L)
        assertMediaItemIds(TEST_VIDEO_DATA.map(VideoData::id))
    }

    @Test
    fun shouldPlayFirstIndex_whenPlayerStateIsNull() = exoAppPlayer {
        setUpWith(TEST_VIDEO_DATA, null)

        assertWindowIndex(0)
        assertSeekPosition(0L)
        assertMediaItemIds(TEST_VIDEO_DATA.map(VideoData::id))
    }

    @Test
    fun shouldNotRestorePlayerStateCurrentMedia_whenMatchingMediaIsNotFound() = exoAppPlayer {
        val playerState = PlayerState(
            currentMediaItemId = "unknown",
            currentMediaItemIndex = 5,
            seekPositionMillis = 500L,
            isPlaying = true
        )

        setUpWith(TEST_VIDEO_DATA, playerState)

        assertWindowIndex(0)
        assertSeekPosition(0L)
        assertMediaItemIds(TEST_VIDEO_DATA.map(VideoData::id))
    }

    @Test
    fun shouldReturnPlayerStateThatMatchesPlayer_whenRequested() = exoAppPlayer {
        setUpWith(TEST_VIDEO_DATA, null)
        seekTo(100L)

        val expected = PlayerState(
            currentMediaItemIndex = 0,
            currentMediaItemId = "1", // See TEST_VIDEO_DATA
            seekPositionMillis = 100L,
            isPlaying = true
        )
        assertPlayerState(expected)
    }

    fun exoAppPlayer(block: suspend ExoAppPlayerRobot.() -> Unit) = runBlocking {
        // ExoPlayer must be accessed from main thread.
        withContext(Dispatchers.Main) {
            ExoAppPlayerRobot().use { it.block() }
        }
    }

    class ExoAppPlayerRobot : Closeable {
        private val exoAppPlayer = ExoAppPlayer(
            player = ExoPlayer.Builder(ApplicationProvider.getApplicationContext()).build(),
            updater = DiffingVideoDataUpdater(diffingContext = Dispatchers.Main)
        )

        suspend fun setUpWith(videoData: List<VideoData>, playerState: PlayerState?) {
            exoAppPlayer.setUpWith(videoData, playerState)
        }

        fun seekTo(position: Long) {
            exoAppPlayer.player.seekTo(position)
        }

        override fun close() {
            exoAppPlayer.release()
        }

        fun assertWindowIndex(index: Int) {
            assertEquals(index, exoAppPlayer.player.currentMediaItemIndex)
        }

        fun assertSeekPosition(position: Long) {
            assertEquals(position, exoAppPlayer.player.currentPosition)
        }

        fun assertPlayerState(playerState: PlayerState?) {
            assertEquals(playerState, exoAppPlayer.currentPlayerState)
        }

        fun assertMediaItemIds(ids: List<String>) {
            assertEquals(ids, exoAppPlayer.player.currentMediaItems.map(MediaItem::mediaId))
        }
    }
}
