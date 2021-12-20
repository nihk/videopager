package com.exo.data

import com.exo.players.currentMediaItems
import com.github.difflib.DiffUtils
import com.github.difflib.patch.AbstractDelta
import com.github.difflib.patch.DeltaType
import com.github.difflib.patch.Patch
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.player.models.VideoData
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * When new videos come in from VideoDataRepository or the UI's state has been updated without a
 * change to video data, there can't simply be a clearing of all current videos on ExoPlayer and then
 * adding the latest list of videos. That would disrupt the active video and be a janky UX. Instead,
 * use diffing to figure out what changed and only insert/delete/update those differences.
 */
internal class DiffingVideoDataUpdater(
    private val diffingContext: CoroutineContext
) : VideoDataUpdater {
    override suspend fun update(player: Player, incoming: List<VideoData>) {
        val oldMediaItems = player.currentMediaItems
        val newMediaItems = incoming.toMediaItems()

        val patch: Patch<MediaItem> = withContext(diffingContext) {
            DiffUtils.diff(oldMediaItems, newMediaItems)
        }
        patch.deltas.forEach { delta: AbstractDelta<MediaItem> ->
            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
            when (delta.type) {
                DeltaType.CHANGE -> {
                    delta.delete(player)
                    delta.insert(player)
                }
                DeltaType.DELETE -> delta.delete(player)
                DeltaType.INSERT -> delta.insert(player)
                DeltaType.EQUAL -> {} // Nothing to do here
            }
        }
    }

    private fun AbstractDelta<MediaItem>.delete(player: Player) {
        player.removeMediaItems(target.position, target.position + source.lines.size)
    }

    private fun AbstractDelta<MediaItem>.insert(player: Player) {
        player.addMediaItems(target.position, target.lines)
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
