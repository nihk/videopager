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
            ),
            VideoData(
                id = "2",
                mediaUri = "1",
                previewImageUri = ""
            )
        )
        update(videoData)

        update(emptyList())

        assertMediaItemIdOrder(emptyList())
    }

    @Test
    fun shouldDeleteExoPlayerMediaItems() = updater {
        val videoData = listOf(
            VideoData(
                id = "1",
                mediaUri = "1",
                previewImageUri = ""
            ),
            VideoData(
                id = "2",
                mediaUri = "1",
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
                id = "3",
                mediaUri = "3",
                previewImageUri = ""
            )
        )
        update(newList)

        assertMediaItemIdOrder(listOf("3"))
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

    @Test
    fun shouldInsertAllExoPlayerMediaItems_whenIncomingDataMeansInsertingBeforeAndAfterCurrentItem() = updater {
        val videoData = listOf(
            VideoData(
                id = "1",
                mediaUri = "1",
                previewImageUri = ""
            )
        )
        update(videoData)

        val newList = listOf(
            VideoData(
                id = "3",
                mediaUri = "3",
                previewImageUri = ""
            ),
            VideoData(
                id = "1",
                mediaUri = "1",
                previewImageUri = ""
            ),
            VideoData(
                id = "2",
                mediaUri = "2",
                previewImageUri = ""
            )
        )
        update(newList)

        assertMediaItemIdOrder(listOf("3", "1", "2"))
    }

    @Test
    fun shouldInsertAllExoPlayerMediaItems_whenIncomingDataDovetail() = updater {
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
            ),
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
            ),
            VideoData(
                id = "4",
                mediaUri = "4",
                previewImageUri = ""
            )
        )
        update(newList)

        assertMediaItemIdOrder(listOf("1", "2", "3", "4"))
    }

    @Test
    fun shouldRemoveExoPlayerMediaItem_whenMiddleItemIsRemoved() = updater {
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
        )
        update(videoData)

        val newList = listOf(
            VideoData(
                id = "1",
                mediaUri = "1",
                previewImageUri = ""
            ),
            VideoData(
                id = "3",
                mediaUri = "3",
                previewImageUri = ""
            ),
        )
        update(newList)

        assertMediaItemIdOrder(listOf("1", "3"))
    }

    @Test
    fun shouldSwapPositions() = updater {
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
        )
        update(videoData)

        val newList = listOf(
            VideoData(
                id = "2",
                mediaUri = "2",
                previewImageUri = ""
            ),
            VideoData(
                id = "1",
                mediaUri = "1",
                previewImageUri = ""
            )
        )
        update(newList)

        assertMediaItemIdOrder(listOf("2", "1"))
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

        assertMediaItemIdOrder(listOf("1", "2"))
        assertMediaItemUriOrder(listOf("xyz.net", "tuv.net"))
    }

    @Test
    fun shouldKeepListWhenEqual() = updater {
        val videoData = listOf(
            VideoData(
                id = "1",
                mediaUri = "1",
                previewImageUri = ""
            )
        )
        update(videoData)

        val newList = listOf(
            VideoData(
                id = "1",
                mediaUri = "1",
                previewImageUri = ""
            )
        )
        update(newList)

        assertMediaItemIdOrder(listOf("1"))
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
            DiffingVideoDataUpdater(diffingContext = Dispatchers.Main)

        suspend fun update(videoData: List<VideoData>) {
            updater.update(exoPlayer, videoData)
        }

        fun assertMediaItemIdOrder(ids: List<String>) {
            assertEquals(ids, exoPlayer.currentMediaItems.map(MediaItem::mediaId))
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
