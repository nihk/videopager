package com.example.exo_viewpager_fun.ui.extensions

import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun ViewPager2.pageScrollStateChanges(): Flow<Int> = callbackFlow {
    val callback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            trySend(state)
        }
    }

    registerOnPageChangeCallback(callback)

    awaitClose { unregisterOnPageChangeCallback(callback) }
}

fun ViewPager2.pageChangesWhileScrolling(): Flow<Unit> = callbackFlow {
    val callback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            if (scrollState == ViewPager2.SCROLL_STATE_IDLE) return
            trySend(Unit)
        }
    }

    registerOnPageChangeCallback(callback)

    awaitClose { unregisterOnPageChangeCallback(callback) }
}

val ViewPager2.isIdle get() = scrollState == ViewPager2.SCROLL_STATE_IDLE
