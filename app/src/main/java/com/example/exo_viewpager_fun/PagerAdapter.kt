package com.example.exo_viewpager_fun

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.exo_viewpager_fun.databinding.PageItemBinding
import com.google.android.exoplayer2.ui.PlayerView

class PagerAdapter(
    private val playerView: PlayerView,
    private val videoUris: List<String>
) : RecyclerView.Adapter<PageViewHolder>() {
    private var recyclerView: RecyclerView? = null

    override fun getItemCount(): Int = videoUris.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        return LayoutInflater.from(parent.context)
            .let { inflater -> PageItemBinding.inflate(inflater, parent, false) }
            .let { binding -> PageViewHolder(binding) }
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        // Nothing to do here.
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
    }

    fun onPageSettled(position: Int) {
        val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position)
            as? PageViewHolder
        viewHolder?.attach(playerView)
    }
}
