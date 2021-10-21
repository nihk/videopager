package com.example.exo_viewpager_fun

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout

class FakeAppPlayerView(context: Context) : AppPlayerView {
    val viewId = View.generateViewId()
    override val view: View = FrameLayout(context).apply {
        id = viewId
        setBackgroundColor(Color.GREEN)
    }
    var didStart: Boolean = false
    var didStop: Boolean = false
    var latestEffect: ViewEffect? = null

    override fun onStart(appPlayer: AppPlayer) {
        didStart = true
    }

    override fun onStop() {
        didStop = true
    }

    override fun renderEffect(viewEffect: ViewEffect) {
        latestEffect = viewEffect
    }
}
