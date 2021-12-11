package com.example.videopager.ui

import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import com.example.videopager.databinding.PageItemBinding
import com.example.videopager.models.AnimationEffect
import com.example.videopager.models.PageEffect
import com.example.videopager.models.ResetAnimationsEffect
import com.example.videopager.models.VideoData
import com.example.videopager.ui.extensions.detachFromParent

class PageViewHolder(
    private val binding: PageItemBinding,
    private val imageLoader: ImageLoader,
    private val click: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    private val animationEffect = FadeInThenOutAnimationEffect(binding.playPause)

    init {
        binding.root.setOnClickListener { click() }
    }

    fun bind(videoData: VideoData) {
        binding.previewImage.load(
            uri = videoData.previewImageUri,
            imageLoader = imageLoader
        )

        ConstraintSet().apply {
            clone(binding.root)
            // Optimize video preview / container size if aspect ratio is available. This can avoid
            // a flicker when ExoPlayer renders its first frame but hasn't yet adjusted the video size.
            val ratio = videoData.aspectRatio?.let { "$it:1" }
            setDimensionRatio(binding.playerContainer.id, ratio)
            setDimensionRatio(binding.previewImage.id, ratio)
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

    fun renderEffect(effect: PageEffect) {
        when (effect) {
            is ResetAnimationsEffect -> animationEffect.reset()
            is AnimationEffect -> {
                binding.playPause.setImageResource(effect.drawable)
                animationEffect.go()
            }
        }
    }

    fun setPreviewImage(isVisible: Boolean) {
        binding.previewImage.isVisible = isVisible
    }
}
