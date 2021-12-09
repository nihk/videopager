package com.example.exo_viewpager_fun.ui.extensions

import com.example.exo_viewpager_fun.TEST_VIDEO_DATA
import com.example.exo_viewpager_fun.models.VideoData
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListExtKtTest {
    @Test
    fun `should return true when all child elements are the same by reference`() {
        assertTrue(TEST_VIDEO_DATA elementsReferentiallyEqual TEST_VIDEO_DATA)
    }

    @Test
    fun `should return false when child elements are the same reference, but out of order`() {
        assertFalse(TEST_VIDEO_DATA elementsReferentiallyEqual TEST_VIDEO_DATA.reversed())
    }

    @Test
    fun `should return false when lists are not the same size`() {
        assertFalse(TEST_VIDEO_DATA elementsReferentiallyEqual TEST_VIDEO_DATA.drop(1))
    }

    @Test
    fun `should return false when lists have structural equality, but not referential equality`() {
        assertFalse(TEST_VIDEO_DATA elementsReferentiallyEqual TEST_VIDEO_DATA.map(VideoData::copy))
    }
}
