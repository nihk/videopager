package com.example.exo_viewpager_fun.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import com.example.exo_viewpager_fun.models.VideoData
import com.example.exo_viewpager_fun.databinding.PageItemBinding
import com.example.exo_viewpager_fun.models.PageEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * The two main functions of interest here are [attachPlayerView] and [showPlayerFor].
 *
 * [attachPlayerView] attaches a [AppPlayerView] instance to a ViewGroup owned by a ViewHolder.
 *
 * [showPlayerFor] hides the video image preview of a given ViewHolder so that video playback can
 * be visible.
 *
 * These are two distinct functions because a PlayerView has to first be attached to a View hierarchy
 * before an ExoPlayer instance will notify listeners that it has started rendering frames.
 */
class PagerAdapter(
    private val imageLoader: ImageLoader
) : ListAdapter<VideoData, PageViewHolder>(VideoDataDiffCallback) {
    private var recyclerView: RecyclerView? = null
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
    fun attachPlayerView(appPlayerView: AppPlayerView, position: Int) {
        onViewHolder(position) { viewHolder ->
            viewHolder.attach(appPlayerView)
        }
    }

    // Hides the video preview image when the player is ready to be shown.
    fun showPlayerFor(position: Int) {
        onViewHolder(position) { viewHolder ->
            viewHolder.setPreviewImage(isVisible = false)
        }
    }

    fun renderEffect(position: Int, effect: PageEffect) {
        onViewHolder(position) { viewHolder ->
            viewHolder.renderEffect(effect)
        }
    }

    /**
     * The ViewHolder at [position] isn't always immediately available. In those cases, wait for
     * the RecyclerView to be laid out and re-query that ViewHolder.
     */
    private fun onViewHolder(position: Int, block: (PageViewHolder) -> Unit) {
        if (currentList.isEmpty()) return // Nothing to do here

        val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position)
            as? PageViewHolder

        if (viewHolder == null) {
            recyclerView?.doOnLayout {
                onViewHolder(position, block)
            }
        } else {
            block(viewHolder)
        }
    }

    override fun onViewDetachedFromWindow(holder: PageViewHolder) {
        // Reset state
        holder.setPreviewImage(isVisible = true)
    }

    private object VideoDataDiffCallback : DiffUtil.ItemCallback<VideoData>() {
        override fun areItemsTheSame(oldItem: VideoData, newItem: VideoData): Boolean {
            return oldItem.mediaUri == newItem.mediaUri
        }

        override fun areContentsTheSame(oldItem: VideoData, newItem: VideoData): Boolean {
            return oldItem == newItem
        }
    }
}
