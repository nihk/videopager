package com.videopager.ui

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import com.player.players.AppPlayer
import com.player.ui.AppPlayerView

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

    override fun attach(appPlayer: AppPlayer) {
        didAttach = true
    }

    override fun detachPlayer() {
        didDetach = true
    }
}
