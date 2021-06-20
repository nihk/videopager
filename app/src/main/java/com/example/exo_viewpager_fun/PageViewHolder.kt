package com.example.exo_viewpager_fun

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.exo_viewpager_fun.databinding.PageItemBinding
import com.google.android.exoplayer2.ui.PlayerView

class PageViewHolder(private val binding: PageItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun attach(playerView: PlayerView) {
        if (binding.playerContainer == playerView.parent) {
            // Already attached
            return
        }

        playerView.detachFromParent()
        binding.playerContainer.addView(playerView)
    }

    private fun View.detachFromParent() {
        val parent = parent as? ViewGroup ?: return
        parent.removeView(this)
    }
}
