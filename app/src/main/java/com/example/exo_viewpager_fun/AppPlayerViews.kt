package com.example.exo_viewpager_fun

import android.view.LayoutInflater
import android.view.View
import com.example.exo_viewpager_fun.databinding.PlayerViewBinding

interface AppPlayerView {
    val view: View

    fun onStart(appPlayer: AppPlayer)
    fun onStop()
    fun renderEffect(viewEffect: ViewEffect)
}

class ExoAppPlayerView(layoutInflater: LayoutInflater) : AppPlayerView {
    override val view: View = layoutInflater.inflate(R.layout.player_view, null)
    private val binding = PlayerViewBinding.bind(view)
    private val animationEffect = FadeInThenOutAnimationEffect(binding.playPause)

    override fun onStart(appPlayer: AppPlayer) {
        binding.playerView.player = (appPlayer as? ExoAppPlayer)?.exoPlayer
    }

    // ExoPlayer and PlayerView hold circular ref's to each other, so avoid leaking
    // Activity here by nulling it out.
    override fun onStop() {
        binding.playerView.player = null
    }

    override fun renderEffect(viewEffect: ViewEffect) {
        val drawableRes = when (viewEffect) {
            ShowPauseAnimation -> R.drawable.pause
            ShowPlayAnimation -> R.drawable.play
        }
        binding.playPause.setImageResource(drawableRes)
        animationEffect.go()
    }
}
