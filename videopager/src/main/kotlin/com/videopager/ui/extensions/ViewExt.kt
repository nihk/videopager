package com.videopager.ui.extensions

import android.view.View
import androidx.core.view.doOnNextLayout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal suspend fun View.awaitNextLayout() = suspendCoroutine<Unit> { cont ->
    doOnNextLayout {
        cont.resume(Unit)
    }
}
