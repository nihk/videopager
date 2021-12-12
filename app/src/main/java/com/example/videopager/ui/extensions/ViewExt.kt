package com.example.videopager.ui.extensions

import android.view.View
import android.view.ViewManager
import androidx.core.view.doOnNextLayout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun View.detachFromParent() {
    val parent = parent as? ViewManager ?: return
    parent.removeView(this)
}

suspend fun View.awaitNextLayout() = suspendCoroutine<Unit> { cont ->
    doOnNextLayout {
        cont.resume(Unit)
    }
}
