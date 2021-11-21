package com.example.exo_viewpager_fun.data

import androidx.test.core.app.ApplicationProvider
import com.example.exo_viewpager_fun.players.currentMediaItems
import com.example.exo_viewpager_fun.models.VideoData
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
                mediaUri = "1",
                previewImageUri = ""
            ),
            VideoData(
                mediaUri = "3",
                previewImageUri = ""
            )
        )
        update(videoData)

        val newList = listOf(
            VideoData(
                mediaUri = "1",
                previewImageUri = ""
            ),
            VideoData(
                mediaUri = "2",
                previewImageUri = ""
            ),
            VideoData(
                mediaUri = "3",
                previewImageUri = ""
            )
        )
        update(newList)

        assertMediaItemIdOrder(listOf("1", "2", "3"))
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

        fun assertMediaItemIdOrder(ids: List<String>) {
            assertEquals(ids, exoPlayer.currentMediaItems.map(MediaItem::mediaId))
        }

        override fun close() {
            exoPlayer.release()
        }
    }
}
