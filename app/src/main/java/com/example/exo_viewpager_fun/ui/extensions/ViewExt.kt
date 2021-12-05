package com.example.exo_viewpager_fun.ui.extensions

import android.view.View
import android.view.ViewManager

fun View.detachFromParent() {
    val parent = parent as? ViewManager ?: return
    parent.removeView(this)
}
