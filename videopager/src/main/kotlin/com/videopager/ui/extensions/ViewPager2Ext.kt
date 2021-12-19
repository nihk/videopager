package com.videopager.ui.extensions

import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal fun ViewPager2.pageIdlings(): Flow<Unit> = callbackFlow {
    val callback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            if (state != ViewPager2.SCROLL_STATE_IDLE) return
            trySend(Unit)
        }
    }

    registerOnPageChangeCallback(callback)

    awaitClose { unregisterOnPageChangeCallback(callback) }
}

internal fun ViewPager2.pageChanges(): Flow<Unit> = callbackFlow {
    val callback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            /**
             * Ignore changes that aren't perceived as a change by the user. For example, if
             * a list of videos (A) gets updated to (X, Y, A), then the active page will have
             * changed from index 0 to 2, but from the user perspective they are on the same page.
             */
            if (scrollState == ViewPager2.SCROLL_STATE_IDLE) return
            trySend(Unit)
        }
    }

    registerOnPageChangeCallback(callback)

    awaitClose { unregisterOnPageChangeCallback(callback) }
}

internal val ViewPager2.isIdle get() = scrollState == ViewPager2.SCROLL_STATE_IDLE
