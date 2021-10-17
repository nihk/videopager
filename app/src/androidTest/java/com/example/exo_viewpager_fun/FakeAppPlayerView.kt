package com.example.exo_viewpager_fun

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout

class FakeAppPlayerView(context: Context) : AppPlayerView {
    override val view: View = FrameLayout(context).apply {
        id = View.generateViewId()
        setBackgroundColor(Color.GREEN)
    }
    var didStart: Boolean = false
    var didStop: Boolean = false

    override fun onStart(appPlayer: AppPlayer) {
        didStart = true
    }

    override fun onStop() {
        didStop = true
    }
}
