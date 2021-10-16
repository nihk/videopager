package com.example.exo_viewpager_fun

import androidx.test.core.app.ApplicationProvider
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Test
import java.io.Closeable

class RecyclerViewVideoDataUpdaterTest {
    @Test
    fun shouldAddMediaItem_whenExoPlayerIsEmpty() = updater {
        val videoData = listOf(
            VideoData(
                mediaUri = "1",
                previewImageUri = "1"
            )
        )

        update(videoData)

        assertMediaIdOrder(videoData.map(VideoData::mediaUri))
    }

    @Test
    fun shouldRemoveAllExoPlayerMediaItems_whenIncomingDataIsEmpty() = updater {
        val videoData = listOf(
            VideoData(
                mediaUri = "1",
                previewImageUri = "1"
            )
        )
        update(videoData)

        update(emptyList())

        assertMediaIdOrder(emptyList())
    }

    @Test
    fun shouldInsertMediaItemInMiddle_whenIncomingDataAddsMiddleItem() = updater {
        val videoData = listOf(
            VideoData(
                mediaUri = "1",
                previewImageUri = "1"
            ),
            VideoData(
                mediaUri = "3",
                previewImageUri = "3"
            )
        )
        update(videoData)

        val newList = listOf(
            VideoData(
                mediaUri = "1",
                previewImageUri = "1"
            ),
            VideoData(
                mediaUri = "2",
                previewImageUri = "2"
            ),
            VideoData(
                mediaUri = "3",
                previewImageUri = "3"
            )
        )
        update(newList)

        assertMediaIdOrder(newList.map(VideoData::mediaUri))
    }

    fun updater(block: suspend UpdaterRobot.() -> Unit) = runBlocking {
        withContext(Dispatchers.Main) {
            UpdaterRobot().use { it.block() }
        }
    }

    class UpdaterRobot : Closeable {
        private val exoPlayer = SimpleExoPlayer.Builder(ApplicationProvider.getApplicationContext())
            .build()
        private val updater = RecyclerViewVideoDataUpdater()

        fun update(videoData: List<VideoData>) {
            updater.update(exoPlayer, videoData)
        }

        fun assertMediaIdOrder(ids: List<String>) {
            assertEquals(ids, exoPlayer.currentMediaItems.map(MediaItem::mediaId))
        }

        override fun close() {
            exoPlayer.release()
        }
    }
}
