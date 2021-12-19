package com.exo.ui

import android.content.Context
import com.videopager.ui.AppPlayerView

class ExoAppPlayerViewFactory : AppPlayerView.Factory {
    override fun create(context: Context): AppPlayerView {
        return ExoAppPlayerView(context.layoutInflater)
    }
}
