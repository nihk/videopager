package com.exo.data

import androidx.test.core.app.ApplicationProvider
import com.exo.players.currentMediaItems
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.videopager.models.VideoData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.Closeable

class RecyclerViewVideoDataUpdaterTest {
    @Test
    fun shouldAddMediaItem_whenExoPlayerIsEmpty() = updater {
        val videoData = listOf(
            VideoData(
                id = "1",
                mediaUri = "1",
                previewImageUri = ""
            )
        )

        update(videoData)

        assertMediaItemIdOrder(listOf("1"))
    }

    @Test
    fun shouldRemoveAllExoPlayerMediaItems_whenIncomingDataIsEmpty() = updater {
        val videoData = listOf(
            VideoData(
                id = "1",
                mediaUri = "1",
                previewImageUri = ""
            )
        )
        update(videoData)

        update(emptyList())

        assertMediaItemIdOrder(emptyList())
    }

    @Test
    fun shouldInsertMediaItemInMiddle_whenIncomingDataAddsMiddleItem() = updater {
        val videoData = listOf(
            VideoData(
                id = "1",
                mediaUri = "1",
                previewImageUri = ""
            ),
            VideoData(
                id = "3",
                mediaUri = "3",
                previewImageUri = ""
            )
        )
        update(videoData)

        val newList = listOf(
            VideoData(
                id = "1",
                mediaUri = "1",
                previewImageUri = ""
            ),
            VideoData(
                id = "2",
                mediaUri = "2",
                previewImageUri = ""
            ),
            VideoData(
                id = "3",
                mediaUri = "3",
                previewImageUri = ""
            )
        )
        update(newList)

        assertMediaItemIdOrder(listOf("1", "2", "3"))
    }

    private fun updater(block: suspend UpdaterRobot.() -> Unit) = runBlocking {
        withContext(Dispatchers.Main) {
            UpdaterRobot().use { it.block() }
        }
    }

    class UpdaterRobot : Closeable {
        private val exoPlayer = ExoPlayer.Builder(ApplicationProvider.getApplicationContext())
            .build()
        private val updater =
            com.exo.data.RecyclerViewVideoDataUpdater(diffingContext = Dispatchers.Main)

        suspend fun update(videoData: List<VideoData>) {
            updater.update(exoPlayer, videoData)
        }

        fun assertMediaItemIdOrder(ids: List<String>) {
            assertEquals(ids, exoPlayer.currentMediaItems.map(MediaItem::mediaId))
        }

        override fun close() {
            exoPlayer.release()
        }
    }
}
