package com.example.exo_viewpager_fun.ui

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import com.example.exo_viewpager_fun.models.PlayerViewEffect
import com.example.exo_viewpager_fun.players.AppPlayer

class FakeAppPlayerView(
    context: Context
) : AppPlayerView {
    val viewId = View.generateViewId()
    override val view: View = FrameLayout(context).apply {
        id = viewId
        setBackgroundColor(Color.GREEN)
    }
    var didAttach: Boolean = false
    var didDetach: Boolean = false
    var latestEffect: PlayerViewEffect? = null

    override fun attach(appPlayer: AppPlayer) {
        didAttach = true
    }

    override fun detachPlayer() {
        didDetach = true
    }

    override fun renderEffect(playerViewEffect: PlayerViewEffect) {
        latestEffect = playerViewEffect
    }
}
