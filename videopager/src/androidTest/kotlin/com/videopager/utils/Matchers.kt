package com.videopager.utils

import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.viewpager2.widget.ViewPager2
import org.hamcrest.Description
import org.hamcrest.Matcher

fun withPage(page: Int): Matcher<View> {
    return object : BoundedMatcher<View, ViewPager2>(ViewPager2::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("matching currentItem to page: $page")
        }

        override fun matchesSafely(item: ViewPager2): Boolean {
            return item.currentItem == page
        }
    }
}

fun atPage(
    page: Int,
    matcher: Matcher<View>,
    @IdRes targetViewId: Int? = null
): Matcher<View> {
    return object : BoundedMatcher<View, ViewPager2>(ViewPager2::class.java) {
        override fun describeTo(description: Description) {
            val targetViewMsg = targetViewId?.toString() ?: "root"
            description.appendText("matching at ViewPager2 page: $page for target view $targetViewMsg")
        }

        override fun matchesSafely(viewPager2: ViewPager2): Boolean {
            val recyclerView = viewPager2.getChildAt(0) as RecyclerView
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(page)
            val view = if (targetViewId != null) {
                viewHolder?.itemView?.findViewById(targetViewId)
            } else {
                viewHolder?.itemView
            }
            return view != null && matcher.matches(view)
        }
    }
}
