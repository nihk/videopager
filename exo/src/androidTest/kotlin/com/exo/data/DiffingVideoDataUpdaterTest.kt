package com.exo.data

import androidx.test.core.app.ApplicationProvider
import com.exo.players.currentMediaItems
import com.google.android.exoplayer2.ExoPlayer
import com.player.models.VideoData
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

        assertMediaItems(videoData(1))
    }

    @Test
    fun shouldRemoveAllExoPlayerMediaItems_whenIncomingDataIsEmpty() = updater {
        update(videoData(1, 2))

        update(emptyList())

        assertMediaItems(emptyList())
    }

    @Test
    fun shouldDeleteExoPlayerMediaItems() = updater {
        update(videoData(1, 2, 3, 4))

        update(videoData(3))

        assertMediaItems(videoData(3))
    }

    @Test
    fun shouldInsertMediaItemInMiddle_whenIncomingDataAddsMiddleItem() = updater {
        update(videoData(1, 3))

        update(videoData(1, 2, 3))

        assertMediaItems(videoData(1, 2, 3))
    }

    @Test
    fun shouldInsertAllExoPlayerMediaItems_whenIncomingDataMeansInsertingBeforeAndAfterCurrentItem() = updater {
        update(videoData(1))

        update(videoData(3, 1, 2))

        assertMediaItems(videoData(3, 1, 2))
    }

    @Test
    fun shouldInsertAllExoPlayerMediaItems_whenIncomingDataDovetails() = updater {
        update(videoData(1, 3))

        update(videoData(1, 2, 3, 4))

        assertMediaItems(videoData(1, 2, 3, 4))
    }

    @Test
    fun shouldRemoveExoPlayerMediaItem_whenMiddleItemIsRemoved() = updater {
        update(videoData(1, 2, 3))

        update(videoData(1, 3))

        assertMediaItems(videoData(1, 3))
    }

    @Test
    fun shouldSwapPositions() = updater {
        update(videoData(1, 2))

        update(videoData(2, 1))

        assertMediaItems(videoData(2, 1))
    }

    @Test
    fun shouldReverse() = updater {
        update(videoData(1, 2, 3, 4))

        update(videoData(4, 3, 2, 1))

        assertMediaItems(videoData(4, 3, 2, 1))
    }

    @Test
    fun shouldSwapInner() = updater {
        update(videoData(1, 2, 3, 4))

        update(videoData(1, 3, 2, 4))

        assertMediaItems(videoData(1, 3, 2, 4))
    }

    @Test
    fun shouldUpdateMediaUris() = updater {
        update(videoData(1, 2))

        val newList = videoData(1, 2).map { videoData ->
            videoData.copy(mediaUri = "${videoData.mediaUri}.net")
        }
        update(newList)

        assertMediaItems(newList)
    }

    @Test
    fun shouldSwapAndChange() = updater {
        update(videoData(1, 2, 3, 4))

        val newList = videoData(1, 2, 3, 4).map { videoData ->
            when (videoData.id) {
                "1", "4" -> videoData.copy(mediaUri = "${videoData.mediaUri}.org")
                else -> videoData
            }
        }.reversed()
        update(newList)

        assertMediaItems(newList)
    }

    @Test
    fun shouldKeepListWhenEqual() = updater {
        update(videoData(1, 2, 3, 4))

        update(videoData(1, 2, 3, 4))

        assertMediaItems(videoData(1, 2, 3, 4))
    }

    @Test
    fun shouldReplaceWithEntirelyNewList() = updater {
        update(videoData(1, 2, 3, 4))

        update(videoData(5, 6, 7))

        assertMediaItems(videoData(5, 6, 7))
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

        fun assertMediaItems(videoData: List<VideoData>) {
            assertEquals(videoData.size, exoPlayer.mediaItemCount)
            videoData.zip(exoPlayer.currentMediaItems) { videoData, mediaItem ->
                assertEquals(videoData.id, mediaItem.mediaId)
                assertEquals(videoData.mediaUri, mediaItem.localConfiguration?.uri.toString())
            }
        }

        override fun close() {
            exoPlayer.release()
        }
    }
}
