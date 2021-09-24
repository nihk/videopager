package com.example.exo_viewpager_fun

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

interface VideoDataUpdater {
    fun update(exoPlayer: ExoPlayer, incoming: List<VideoData>)
}

// androidx.recyclerview has out-of-the-box diffing algorithms that are useful for updating/diffing
// any incoming VideoData compared to current VideoData. This enables modifying the ViewPager2
// data set of videos without necessarily stopping active video content playing -- essential for
// pagination.
class RecyclerViewVideoDataUpdater : VideoDataUpdater {
    override fun update(exoPlayer: ExoPlayer, incoming: List<VideoData>) {
        val newMediaItems = incoming.toMediaItems()
        val diffCallback = MediaItemDiffCallback(exoPlayer.mediaItems, newMediaItems)
        val updateCallback = ExoPlayerUpdateCallback(exoPlayer, newMediaItems)
        val result = DiffUtil.calculateDiff(diffCallback)
        result.dispatchUpdatesTo(updateCallback)
    }

    private val ExoPlayer.mediaItems: List<MediaItem> get() {
        val mediaItems = mutableListOf<MediaItem>()

        for (i in 0 until mediaItemCount) {
            mediaItems += getMediaItemAt(i)
        }

        return mediaItems
    }

    private fun List<VideoData>.toMediaItems(): List<MediaItem> {
        return map { videoData ->
            MediaItem.Builder()
                .setMediaId(videoData.mediaUri)
                .setUri(videoData.mediaUri)
                .build()
        }
    }

    private class MediaItemDiffCallback(
        private val current: List<MediaItem>,
        private val incoming: List<MediaItem>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = current.size
        override fun getNewListSize() = incoming.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return current[oldItemPosition].mediaId == incoming[newItemPosition].mediaId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return current[oldItemPosition] == incoming[newItemPosition]
        }
    }

    private class ExoPlayerUpdateCallback(
        private val exoPlayer: ExoPlayer,
        private val incoming: List<MediaItem>
    ) : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {
            exoPlayer.addMediaItems(position, incoming.subList(position, position + count))
        }

        override fun onRemoved(position: Int, count: Int) {
            exoPlayer.removeMediaItems(position, position + count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            exoPlayer.moveMediaItem(fromPosition, toPosition)
        }

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            exoPlayer.removeMediaItems(position, position + count)
            exoPlayer.addMediaItems(position, incoming.subList(position, position + count))
        }
    }
}
