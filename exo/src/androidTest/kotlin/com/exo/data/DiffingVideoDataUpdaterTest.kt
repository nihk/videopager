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

class DiffingVideoDataUpdaterTest {
    @Test
    fun shouldAddMediaItem_whenExoPlayerIsEmpty() = updater {
        update(videoData(1))

        assertMediaItemIdOrder(1)
    }

    @Test
    fun shouldRemoveAllExoPlayerMediaItems_whenIncomingDataIsEmpty() = updater {
        update(videoData(1, 2))

        update(emptyList())

        assertMediaItemIdOrder()
    }

    @Test
    fun shouldDeleteExoPlayerMediaItems() = updater {
        update(videoData(1, 2, 3, 4))

        update(videoData(3))

        assertMediaItemIdOrder(3)
    }

    @Test
    fun shouldInsertMediaItemInMiddle_whenIncomingDataAddsMiddleItem() = updater {
        update(videoData(1, 3))

        update(videoData(1, 2, 3))

        assertMediaItemIdOrder(1, 2, 3)
    }

    @Test
    fun shouldInsertAllExoPlayerMediaItems_whenIncomingDataMeansInsertingBeforeAndAfterCurrentItem() = updater {
        update(videoData(1))

        update(videoData(3, 1, 2))

        assertMediaItemIdOrder(3, 1, 2)
    }

    @Test
    fun shouldInsertAllExoPlayerMediaItems_whenIncomingDataDovetails() = updater {
        update(videoData(1, 3))

        update(videoData(1, 2, 3, 4))

        assertMediaItemIdOrder(1, 2, 3, 4)
    }

    @Test
    fun shouldRemoveExoPlayerMediaItem_whenMiddleItemIsRemoved() = updater {
        update(videoData(1, 2, 3))

        update(videoData(1, 3))

        assertMediaItemIdOrder(1, 3)
    }

    @Test
    fun shouldSwapPositions() = updater {
        update(videoData(1, 2))

        update(videoData(2, 1))

        assertMediaItemIdOrder(2, 1)
    }

    @Test
    fun shouldReverse() = updater {
        update(videoData(1, 2, 3, 4))

        update(videoData(4, 3, 2, 1))

        assertMediaItemIdOrder(4, 3, 2, 1)
    }

    @Test
    fun shouldSwapInner() = updater {
        update(videoData(1, 2, 3, 4))

        update(videoData(1, 3, 2, 4))

        assertMediaItemIdOrder(1, 3, 2, 4)
    }

    @Test
    fun shouldChangeItems() = updater {
        val videoData = listOf(
            VideoData(
                id = "1",
                mediaUri = "abc.net",
                previewImageUri = ""
            ),
            VideoData(
                id = "2",
                mediaUri = "def.net",
                previewImageUri = ""
            ),
        )
        update(videoData)

        val newList = listOf(
            VideoData(
                id = "1",
                mediaUri = "xyz.net",
                previewImageUri = ""
            ),
            VideoData(
                id = "2",
                mediaUri = "tuv.net",
                previewImageUri = ""
            )
        )
        update(newList)

        assertMediaItemIdOrder(1, 2)
        assertMediaItemUriOrder(listOf("xyz.net", "tuv.net"))
    }

    @Test
    fun shouldSwapAndChange() = updater {
        val videoData = listOf(
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
            ),
            VideoData(
                id = "4",
                mediaUri = "4",
                previewImageUri = ""
            )
        )
        update(videoData)

        val newList = listOf(
            VideoData(
                id = "1",
                mediaUri = "1x",
                previewImageUri = ""
            ),
            VideoData(
                id = "3",
                mediaUri = "3",
                previewImageUri = ""
            ),
            VideoData(
                id = "2",
                mediaUri = "2",
                previewImageUri = ""
            ),
            VideoData(
                id = "4",
                mediaUri = "4x",
                previewImageUri = ""
            )
        )
        update(newList)

        assertMediaItemIdOrder(1, 3, 2, 4)
        assertMediaItemUriOrder(listOf("1x", "3", "2", "4x"))
    }

    @Test
    fun shouldKeepListWhenEqual() = updater {
        update(videoData(1, 2, 3, 4))

        update(videoData(1, 2, 3, 4))

        assertMediaItemIdOrder(1, 2, 3, 4)
    }

    private fun updater(block: suspend UpdaterRobot.() -> Unit) = runBlocking {
        withContext(Dispatchers.Main) {
            UpdaterRobot().use { it.block() }
        }
    }

    private fun videoData(vararg ids: Int): List<VideoData> {
        return ids.map { id ->
            VideoData(
                id = id.toString(),
                mediaUri = id.toString(),
                previewImageUri = ""
            )
        }
    }

    class UpdaterRobot : Closeable {
        private val exoPlayer = ExoPlayer.Builder(ApplicationProvider.getApplicationContext())
            .build()
        private val updater = DiffingVideoDataUpdater(diffingContext = Dispatchers.Main)

        suspend fun update(videoData: List<VideoData>) {
            updater.update(exoPlayer, videoData)
        }

        fun assertMediaItemIdOrder(vararg ids: Int) {
            assertEquals(ids.map(Int::toString), exoPlayer.currentMediaItems.map(MediaItem::mediaId))
        }

        fun assertMediaItemUriOrder(uris: List<String>) {
            assertEquals(
                uris,
                exoPlayer.currentMediaItems
                    .map { mediaItem -> mediaItem.localConfiguration!!.uri.toString() }
            )
        }

        override fun close() {
            exoPlayer.release()
        }
    }
}
