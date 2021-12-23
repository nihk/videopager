package com.videopager.ui.extensions

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.view.doOnNextLayout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal suspend fun View.awaitNextLayout() = suspendCoroutine<Unit> { cont ->
    doOnNextLayout {
        cont.resume(Unit)
    }
}

internal fun View.findParentById(@IdRes id: Int): ViewGroup? {
    return if (this.id == id) {
        this as? ViewGroup
    } else {
        (parent as? View)?.findParentById(id)
    }
}
