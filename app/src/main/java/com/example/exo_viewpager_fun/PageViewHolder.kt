package com.example.exo_viewpager_fun

import android.view.View
import android.view.ViewManager
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.exo_viewpager_fun.databinding.PageItemBinding
import com.google.android.exoplayer2.ui.PlayerView

class PageViewHolder(private val binding: PageItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(videoData: VideoData) {
        binding.previewImage.load(videoData.previewImageUri) {
            crossfade(true)
        }
    }

    fun attach(playerView: PlayerView) {
        if (binding.playerContainer == playerView.parent) {
            // Already attached
            return
        }

        playerView.detachFromParent()
        binding.playerContainer.addView(playerView)
    }

    private fun View.detachFromParent() {
        val parent = parent as? ViewManager ?: return
        parent.removeView(this)
    }

    fun setPreviewImage(isVisible: Boolean) {
        binding.previewImage.isVisible = isVisible
    }
}
