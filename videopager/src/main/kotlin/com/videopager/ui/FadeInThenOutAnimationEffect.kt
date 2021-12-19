package com.videopager.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import android.view.animation.OvershootInterpolator

// This animation effect can be seen when you tap the player.
internal class FadeInThenOutAnimationEffect(private val view: View) {
    private var animatorSet: AnimatorSet? = null

    fun go() {
        reset()

        val fadeIn = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
        val fadeOut = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f)
        val expandX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 2f)
        val expandY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 2f)
        val shrinkX = PropertyValuesHolder.ofFloat(View.SCALE_X, 2f, 0f)
        val shrinkY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 2f, 0f)

        val start = ObjectAnimator.ofPropertyValuesHolder(view, fadeIn, expandX, expandY)
            .apply { interpolator = OvershootInterpolator(6f) }
            .setDuration(200L)
        val end = ObjectAnimator.ofPropertyValuesHolder(view, fadeOut, shrinkX, shrinkY)
            .apply { startDelay = 400L }
            .setDuration(200L)

        animatorSet = AnimatorSet().apply {
            playSequentially(start, end)
            start()
        }
    }

    fun reset() {
        animatorSet?.cancel()
        with(view) {
            alpha = 0f
            scaleX = 0f
            scaleY = 0f
        }
    }
}
