package com.exo.data

import com.exo.players.currentMediaItems
import com.github.difflib.DiffUtils
import com.github.difflib.patch.AbstractDelta
import com.github.difflib.patch.DeltaType
import com.github.difflib.patch.Patch
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.videopager.models.VideoData
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class DiffingVideoDataUpdater(
    private val diffingContext: CoroutineContext
) : VideoDataUpdater {
    override suspend fun update(exoPlayer: ExoPlayer, incoming: List<VideoData>) {
        val oldMediaItems = exoPlayer.currentMediaItems
        val newMediaItems = incoming.toMediaItems()

        val patch: Patch<MediaItem> = withContext(diffingContext) {
            DiffUtils.diff(oldMediaItems, newMediaItems)
        }
        patch.deltas.forEach { delta: AbstractDelta<MediaItem> ->
            when (delta.type) {
                DeltaType.CHANGE -> {
                    exoPlayer.removeMediaItems(delta.target.position, delta.target.position + delta.source.lines.size)
                    exoPlayer.addMediaItems(delta.target.position, delta.target.lines)
                }
                DeltaType.DELETE -> exoPlayer.removeMediaItems(delta.target.position, delta.target.position + delta.source.lines.size)
                DeltaType.INSERT -> exoPlayer.addMediaItems(delta.target.position, delta.target.lines)
                DeltaType.EQUAL -> {} // Nothing to do here
                null -> error("Delta type was null")
            }
        }
    }

    private fun List<VideoData>.toMediaItems(): List<MediaItem> {
        return map { videoData ->
            MediaItem.Builder()
                .setMediaId(videoData.id)
                .setUri(videoData.mediaUri)
                .build()
        }
    }
}
