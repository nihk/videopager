package com.example.exo_viewpager_fun.utils

import android.view.View
import android.view.ViewGroup
import androidx.core.view.allViews
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.viewpager2.widget.ViewPager2
import org.hamcrest.Description
import org.hamcrest.Matcher

class ViewPager2Page(
    private val page: Int
) : BoundedMatcher<View, ViewPager2>(ViewPager2::class.java) {
    override fun describeTo(description: Description) {
        description.appendText("matching currentItem to page: $page")
    }

    override fun matchesSafely(item: ViewPager2): Boolean {
        return item.currentItem == page
    }
}

class AtViewPager2Position(
    private val position: Int,
    private val matcher: Matcher<View>
) : BoundedMatcher<View, ViewPager2>(ViewPager2::class.java) {
    override fun describeTo(description: Description) {
        description.appendText("matching at RecyclerView position: $position")
    }

    override fun matchesSafely(viewPager2: ViewPager2): Boolean {
        val recyclerView = viewPager2.getChildAt(0) as RecyclerView
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
        return viewHolder != null && matcher.matches(viewHolder.itemView)
    }
}

class IsParentOf(private val view: View) : BoundedMatcher<View, ViewGroup>(ViewGroup::class.java) {
    override fun describeTo(description: Description) {
        description.appendText("matching child $view")
    }

    override fun matchesSafely(viewGroup: ViewGroup): Boolean {
        return view in viewGroup.allViews
    }
}
