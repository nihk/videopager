package com.example.exo_viewpager_fun

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.exo_viewpager_fun.databinding.PageItemBinding
import com.google.android.exoplayer2.ui.PlayerView

class PagerAdapter(
    private val playerView: PlayerView
) : ListAdapter<VideoData, PageViewHolder>(VideoDataDiffCallback) {
    private var recyclerView: RecyclerView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        return LayoutInflater.from(parent.context)
            .let { inflater -> PageItemBinding.inflate(inflater, parent, false) }
            .let { binding -> PageViewHolder(binding) }
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
    }

    // There's no good RecyclerView.Adapter callback for when a ViewPager page is settled on.
    // onBindViewHolder and other misc. callbacks like onViewAttached/DetachedFromWindow happen
    // outside that event.
    fun onPageSettled(position: Int) {
        val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position)
            as? PageViewHolder
        viewHolder?.attach(playerView)
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
