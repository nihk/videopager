package com.videopager.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import com.player.ui.AppPlayerView
import com.videopager.databinding.PageItemBinding
import com.videopager.models.PageEffect
import com.player.models.VideoData
import com.videopager.ui.extensions.awaitNextLayout
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive

/**
 * The two main functions of interest here are [attachPlayerView] and [showPlayerFor].
 *
 * [attachPlayerView] attaches a [AppPlayerView] instance to a page's View hierarchy. Once it
 * is on-screen, its ExoPlayer instance will be eligible to start rendering frames.
 *
 * [showPlayerFor] hides the video image preview of a given ViewHolder so that video playback can
 * be visible. It's called when the ExoPlayer instance has started rendering its first frame.
 */
internal class PagerAdapter(
    private val imageLoader: ImageLoader
) : ListAdapter<VideoData, PageViewHolder>(VideoDataDiffCallback) {
    private var recyclerView: RecyclerView? = null
    // Extra buffer capacity so that emissions can be sent outside a coroutine
    private val clicks = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun clicks() = clicks.asSharedFlow()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        return LayoutInflater.from(parent.context)
            .let { inflater -> PageItemBinding.inflate(inflater, parent, false) }
            .let { binding ->
                PageViewHolder(binding, imageLoader) { clicks.tryEmit(Unit) }
            }
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        getItem(position).let(holder::bind)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
    }

    /**
     * Attach [appPlayerView] to the ViewHolder at [position]. The player won't actually be visible in
     * the UI until [showPlayerFor] is also called.
     */
    suspend fun attachPlayerView(appPlayerView: AppPlayerView, position: Int) {
        awaitViewHolder(position).attach(appPlayerView)
    }

    // Hides the video preview image when the player is ready to be shown.
    suspend fun showPlayerFor(position: Int) {
        awaitViewHolder(position).hidePreviewImage()
    }

    suspend fun renderEffect(position: Int, effect: PageEffect) {
        awaitViewHolder(position).renderEffect(effect)
    }

    /**
     * The ViewHolder at [position] isn't always immediately available. In those cases, wait for
     * the RecyclerView to be laid out and re-query that ViewHolder.
     */
    private suspend fun awaitViewHolder(position: Int): PageViewHolder {
        if (currentList.isEmpty()) error("Tried to get ViewHolder at position $position, but the list was empty")

        var viewHolder: PageViewHolder?

        do {
            viewHolder = recyclerView?.findViewHolderForAdapterPosition(position) as? PageViewHolder
        } while (currentCoroutineContext().isActive && viewHolder == null && recyclerView?.awaitNextLayout() == Unit)

        return requireNotNull(viewHolder)
    }

    private object VideoDataDiffCallback : DiffUtil.ItemCallback<VideoData>() {
        override fun areItemsTheSame(oldItem: VideoData, newItem: VideoData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: VideoData, newItem: VideoData): Boolean {
            return oldItem == newItem
        }
    }
}
