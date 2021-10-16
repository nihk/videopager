package com.example.exo_viewpager_fun

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.exo_viewpager_fun.databinding.PageItemBinding
import com.google.android.exoplayer2.ui.PlayerView

class PageViewHolder(private val binding: PageItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(videoData: VideoData) {
        binding.previewImage.load(videoData.previewImageUri)
    }

    fun attach(playerView: PlayerView) {
        if (binding.playerContainer == playerView.parent) {
            // Already attached
            return
        }

        /**
         * Since effectively only one [PlayerView] instance is used in the app, it might currently
         * be attached to a View from a previous page. In that case, remove it from that parent
         * and add it to this ViewHolder's View.
         */
        playerView.detachFromParent()
        binding.playerContainer.addView(playerView)
    }

    fun setPreviewImage(isVisible: Boolean) {
        binding.previewImage.isVisible = isVisible
    }
}
