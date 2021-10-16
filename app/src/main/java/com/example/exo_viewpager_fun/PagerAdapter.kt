package com.example.exo_viewpager_fun

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.exo_viewpager_fun.databinding.PageItemBinding

/**
 * The two main functions of interest here are [attachPlayer] and [showPlayerFor].
 *
 * [attachPlayer] attaches a [AppPlayerView] instance to a ViewGroup owned by a ViewHolder.
 *
 * [showPlayerFor] hides the video image preview of a given ViewHolder so that video playback can
 * be visible.
 *
 * These are two distinct functions because a PlayerView has to first be attached to a View hierarchy
 * before an ExoPlayer instance will callback listeners that it has started rendering frames.
 */
class PagerAdapter : ListAdapter<VideoData, PageViewHolder>(VideoDataDiffCallback) {
    private var recyclerView: RecyclerView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        return LayoutInflater.from(parent.context)
            .let { inflater -> PageItemBinding.inflate(inflater, parent, false) }
            .let(::PageViewHolder)
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
     * */
    fun attachPlayer(appPlayerView: AppPlayerView, position: Int) {
        val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position)
            as? PageViewHolder

        if (viewHolder == null) {
            if (currentList.isNotEmpty()) {
                recyclerView?.doOnLayout {
                    attachPlayer(appPlayerView, position)
                }
            } else {
                // Nothing to do here.
            }
        } else {
            viewHolder.attach(appPlayerView)
        }
    }

    // Hides the video preview image when the player is ready to be shown.
    fun showPlayerFor(position: Int) {
        val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position)
            as? PageViewHolder

        if (viewHolder == null) {
            if (currentList.isNotEmpty()) {
                recyclerView?.doOnLayout {
                    showPlayerFor(position)
                }
            } else {
                // Nothing to do here.
            }
        } else {
            viewHolder.setPreviewImage(isVisible = false)
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
