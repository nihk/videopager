package com.example.exo_viewpager_fun

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.MotionEvents
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.viewpager2.widget.ViewPager2
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

class AwaitIdleScrollState : ViewAction {
    override fun getConstraints(): Matcher<View> {
        return allOf(isAssignableFrom(ViewPager2::class.java), isDisplayed())
    }

    override fun getDescription(): String {
        return "awaiting ViewPager2 idle scroll state"
    }

    override fun perform(uiController: UiController, view: View) {
        val viewPager2 = view as ViewPager2
        while (viewPager2.scrollState != ViewPager2.SCROLL_STATE_IDLE) {
            uiController.loopMainThreadForAtLeast(50L)
        }
    }
}

class Tap : ViewAction {
    override fun getConstraints(): Matcher<View> {
        return isDisplayed()
    }

    override fun getDescription(): String {
        return "tap"
    }

    override fun perform(uiController: UiController, view: View) {
        val location = IntArray(2)
        view.getLocationOnScreen(location)

        val coords = floatArrayOf(location.first().toFloat(), location.last().toFloat())
        val precision = floatArrayOf(1f, 1f)

        val downEvent = MotionEvents.sendDown(uiController, coords, precision)
        MotionEvents.sendUp(uiController, downEvent.down, coords)
        // Can't have more than 1 touch listener to await Up event, so use this hack instead.
        uiController.loopMainThreadForAtLeast(1_500L)
    }
}
