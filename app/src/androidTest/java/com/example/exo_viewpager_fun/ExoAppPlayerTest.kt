package com.example.exo_viewpager_fun

import androidx.test.core.app.ApplicationProvider
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
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
            currentMediaItemId = "2. mediaUri", // See TEST_VIDEO_DATA
            currentMediaIndex = 1,
            seekPositionMillis = 500L,
            isPlaying = true
        )

        setUpWith(TEST_VIDEO_DATA, playerState)

        assertWindowIndex(1)
        assertSeekPosition(500L)
        assertMediaItemIds(TEST_VIDEO_DATA.map(VideoData::mediaUri))
    }

    @Test
    fun shouldPlayFirstIndex_whenPlayerStateIsNull() = exoAppPlayer {
        setUpWith(TEST_VIDEO_DATA, null)

        assertWindowIndex(0)
        assertSeekPosition(0L)
        assertMediaItemIds(TEST_VIDEO_DATA.map(VideoData::mediaUri))
    }

    @Test
    fun shouldNotRestorePlayerStateCurrentMedia_whenMatchingMediaIsNotFound() = exoAppPlayer {
        val playerState = PlayerState(
            currentMediaItemId = "unknown",
            currentMediaIndex = 5,
            seekPositionMillis = 500L,
            isPlaying = true
        )

        setUpWith(TEST_VIDEO_DATA, playerState)

        assertWindowIndex(0)
        assertSeekPosition(0L)
        assertMediaItemIds(TEST_VIDEO_DATA.map(VideoData::mediaUri))
    }

    @Test
    fun shouldReturnPlayerStateThatMatchesPlayer_whenRequested() = exoAppPlayer {
        setUpWith(TEST_VIDEO_DATA, null)
        seekTo(100L)

        val expected = PlayerState(
            currentMediaIndex = 0,
            currentMediaItemId = "1. mediaUri", // See TEST_VIDEO_DATA
            seekPositionMillis = 100L,
            isPlaying = false // even tho playWhenReady is true, there's no actual media to play for this test
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
            exoPlayer = SimpleExoPlayer.Builder(ApplicationProvider.getApplicationContext()).build(),
            updater = RecyclerViewVideoDataUpdater()
        )

        fun setUpWith(videoData: List<VideoData>, playerState: PlayerState?) {
            exoAppPlayer.setUpWith(videoData, playerState)
        }

        fun seekTo(position: Long) {
            exoAppPlayer.exoPlayer.seekTo(position)
        }

        override fun close() {
            exoAppPlayer.release()
        }

        fun assertWindowIndex(index: Int) {
            assertEquals(index, exoAppPlayer.exoPlayer.currentWindowIndex)
        }

        fun assertSeekPosition(position: Long) {
            assertEquals(position, exoAppPlayer.exoPlayer.currentPosition)
        }

        fun assertPlayerState(playerState: PlayerState?) {
            assertEquals(playerState, exoAppPlayer.currentPlayerState)
        }

        fun assertMediaItemIds(ids: List<String>) {
            assertEquals(ids, exoAppPlayer.exoPlayer.currentMediaItems.map(MediaItem::mediaId))
        }
    }
}
