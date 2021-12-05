package com.example.exo_viewpager_fun.ui

import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import com.example.exo_viewpager_fun.R
import com.example.exo_viewpager_fun.models.VideoData
import com.example.exo_viewpager_fun.databinding.PageItemBinding
import com.example.exo_viewpager_fun.ui.extensions.detachFromParent

class PageViewHolder(
    private val binding: PageItemBinding,
    private val imageLoader: ImageLoader
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(videoData: VideoData) {
        binding.previewImage.load(
            uri = videoData.previewImageUri,
            imageLoader = imageLoader
        )

        ConstraintSet().apply {
            clone(binding.root)
            // Optimize video container size if aspect ratio is available. This can avoid a flicker
            // when ExoPlayer renders its first frame but hasn't yet adjusted the video size.
            setDimensionRatio(R.id.player_container, videoData.aspectRatio?.let { "$it:1" })
            applyTo(binding.root)
        }
    }

    fun attach(appPlayerView: AppPlayerView) {
        if (binding.playerContainer == appPlayerView.view.parent) {
            // Already attached
            return
        }

        /**
         * Since effectively only one [AppPlayerView] instance is used in the app, it might currently
         * be attached to a View from a previous page. In that case, remove it from that parent
         * before adding it to this ViewHolder's View.
         */
        appPlayerView.view.detachFromParent()
        binding.playerContainer.addView(appPlayerView.view)
    }

    fun setPreviewImage(isVisible: Boolean) {
        binding.previewImage.isVisible = isVisible
    }
}
