package com.example.videopager.ui.extensions

import android.view.View
import android.view.ViewManager
import androidx.core.view.doOnLayout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun View.detachFromParent() {
    val parent = parent as? ViewManager ?: return
    parent.removeView(this)
}

suspend fun View.awaitLayout() = suspendCoroutine<Unit> { cont ->
    doOnLayout {
        cont.resume(Unit)
    }
}
