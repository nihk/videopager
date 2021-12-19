package com.videopager.utils

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.viewpager2.widget.ViewPager2
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

fun awaitIdleScrollState(): ViewAction {
    return object : ViewAction {
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
}
