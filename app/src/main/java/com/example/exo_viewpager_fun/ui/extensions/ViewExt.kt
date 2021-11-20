package com.example.exo_viewpager_fun.ui.extensions

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun View.detachFromParent() {
    val parent = parent as? ViewManager ?: return
    parent.removeView(this)
}

@SuppressLint("ClickableViewAccessibility")
fun View.taps(): Flow<Unit> = callbackFlow {
    val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
            trySend(Unit)
            return true
        }
    }
    val gestureDetector = GestureDetector(context, gestureListener)

    setOnTouchListener { _, motionEvent ->
        gestureDetector.onTouchEvent(motionEvent)
        true
    }

    awaitClose { setOnTouchListener(null) }
}
