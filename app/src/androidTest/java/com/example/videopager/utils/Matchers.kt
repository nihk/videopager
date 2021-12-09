package com.example.videopager.utils

import android.view.View
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

fun atPage(page: Int, matcher: Matcher<View>): Matcher<View> {
    return object : BoundedMatcher<View, ViewPager2>(ViewPager2::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("matching at ViewPager2 page: $page")
        }

        override fun matchesSafely(viewPager2: ViewPager2): Boolean {
            val recyclerView = viewPager2.getChildAt(0) as RecyclerView
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(page)
            return viewHolder != null && matcher.matches(viewHolder.itemView)
        }
    }
}
