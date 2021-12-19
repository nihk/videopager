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

/**
 * When new videos come in from VideoDataRepository or the UI's state has been updated without a
 * change to video data, there can't simply be a clearing of all current videos on ExoPlayer and then
 * adding the latest list of videos. That would disrupt the active video and be a janky UX. Instead,
 * use diffing to figure out what changed and only insert/delete/update those difference.
 */
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
                    delta.delete(exoPlayer)
                    delta.insert(exoPlayer)
                }
                DeltaType.DELETE -> delta.delete(exoPlayer)
                DeltaType.INSERT -> delta.insert(exoPlayer)
                DeltaType.EQUAL -> {} // Nothing to do here
                null -> error("Delta type was null")
            }
        }
    }

    private fun AbstractDelta<MediaItem>.delete(exoPlayer: ExoPlayer) {
        exoPlayer.removeMediaItems(target.position, target.position + source.lines.size)
    }

    private fun AbstractDelta<MediaItem>.insert(exoPlayer: ExoPlayer) {
        exoPlayer.addMediaItems(target.position, target.lines)
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
