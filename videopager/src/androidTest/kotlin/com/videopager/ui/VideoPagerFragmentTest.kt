package com.videopager.ui

import android.content.Context
import android.content.pm.ActivityInfo
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.player.models.VideoData
import com.player.ui.AppPlayerView
import com.videopager.R
import com.videopager.data.FakeVideoDataRepository
import com.videopager.players.FakeAppPlayer
import com.videopager.utils.TEST_VIDEO_DATA
import com.videopager.utils.TestImageLoader
import com.videopager.utils.atPage
import com.videopager.utils.awaitIdleScrollState
import com.videopager.utils.withPage
import com.videopager.vm.VideoPagerViewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoPagerFragmentTest {
    @Test
    fun whenScreenIsStarted_shouldCreatePlayer() = videoPagerFragment {
        assertPlayerCreated(count = 1)
    }

    @Test
    fun whenScreenIsStarted_shouldStartPlayerView() = videoPagerFragment {
        assertPlayerViewStarted()
    }

    @Test
    fun whenScreenIsDestroyed_shouldReleasePlayer() = videoPagerFragment {
        destroy()

        assertPlayerReleased()
    }

    @Test
    fun whenScreenIsDestroyed_shouldDetachPlayerView() = videoPagerFragment {
        destroy()

        assertPlayerViewDetached()
    }

    @Test
    fun whenScreenIsStoppedThenStarted_shouldRecreatePlayer() = videoPagerFragment {
        stop()
        start()

        assertPlayerCreated(count = 2)
    }

    @Test
    fun whenPlayerHasNotYetRenderedFrames_shouldShowImagePreview() = videoPagerFragment {
        emit(TEST_VIDEO_DATA)

        assertImagePreviewVisibility(isVisible = true)
    }

    @Test
    fun whenPlayerIsRenderingFrames_shouldHideImagePreview() {
        val isPlayerRendering = MutableStateFlow<Unit?>(null)

        videoPagerFragment(onPlayerRendering = isPlayerRendering.filterNotNull()) {
            emit(TEST_VIDEO_DATA)
            isPlayerRendering.value = Unit

            assertImagePreviewVisibility(isVisible = false)
        }
    }

    @Test
    fun whenSwipedToNextPage_shouldLoadNextPlayerItem() = videoPagerFragment {
        emit(TEST_VIDEO_DATA)

        swipeToNextPage()

        assertPlaylistChangedTo(1)
    }

    @Test
    fun whenScreenStartedWithVideoData_shouldAttachPlayerView() = videoPagerFragment {
        emit(TEST_VIDEO_DATA)

        assertPlayerViewPosition(0)
    }

    @Test
    fun whenSwipedToNextPage_shouldAttachPlayerViewToNextPage() = videoPagerFragment {
        emit(TEST_VIDEO_DATA)

        swipeToNextPage()

        assertPlayerViewPosition(1)
    }

    @Test
    fun whenScreenIsRecreated_shouldRestorePage() = videoPagerFragment {
        emit(TEST_VIDEO_DATA)

        recreate()

        assertPage(0)

        swipeToNextPage()

        recreate()

        assertPage(1)
    }

    @Test
    fun whenScreenIsTapped_shouldPauseOrPlayPlayer() = videoPagerFragment {
        emit(TEST_VIDEO_DATA)

        tapScreen()

        assertPlaying(false)

        tapScreen()

        assertPlaying(true)
    }

    @Test
    fun whenScreenIsRotated_shouldRestorePage() = videoPagerFragment {
        emit(TEST_VIDEO_DATA)

        swipeToNextPage()

        rotateLandscape()

        assertPage(1)
    }

    @Test
    fun whenScreenIsRotated_shouldNotRecreatePlayer() = videoPagerFragment {
        emit(TEST_VIDEO_DATA)

        swipeToNextPage()

        rotateLandscape()

        assertPlayerCreated(count = 1)
    }

    @Test
    fun whenErrorIsEmitted_messageIsDisplayed() {
        val errors = MutableStateFlow<Throwable?>(null)
        videoPagerFragment(errors = errors.filterNotNull()) {
            val error = RuntimeException("Uh oh!")

            errors.value = error

            assertTextOnScreen("Uh oh!")
        }
    }

    @Test
    fun whenPartiallySwiped_shouldMaintainCurrentVideo() = videoPagerFragment {
        emit(TEST_VIDEO_DATA)

        partialSwipe()

        assertPage(0)
        assertPlayerViewPosition(0)
        assertPlaying(isPlaying = true)
    }

    private fun videoPagerFragment(
        videoData: List<VideoData>? = null,
        onPlayerRendering: Flow<Unit> = emptyFlow(),
        errors: Flow<Throwable> = emptyFlow(),
        block: VideoPagerFragmentRobot.() -> Unit
    ) {
        VideoPagerFragmentRobot(videoData, onPlayerRendering, errors).block()
    }

    class VideoPagerFragmentRobot(
        videoData: List<VideoData>?,
        onPlayerRendering: Flow<Unit>,
        errors: Flow<Throwable>
    ) {
        private val videoDataFlow = MutableStateFlow(videoData)
        private val appPlayer = FakeAppPlayer(onPlayerRendering, errors)
        private val appPlayerFactory = FakeAppPlayer.Factory(appPlayer)
        private val appPlayerView = FakeAppPlayerView(ApplicationProvider.getApplicationContext())

        private val scenario: FragmentScenario<VideoPagerFragment> = launchFragmentInContainer(
            themeResId = R.style.Theme_MaterialComponents_DayNight_DarkActionBar
        ) {
            VideoPagerFragment(
                viewModelFactory = { owner ->
                    VideoPagerViewModelFactory(
                        repository = FakeVideoDataRepository(videoDataFlow),
                        appPlayerFactory = appPlayerFactory,
                    ).create(owner)
                },
                appPlayerViewFactory = object : AppPlayerView.Factory {
                    override fun create(context: Context): AppPlayerView {
                        return appPlayerView
                    }
                },
                imageLoader = TestImageLoader()
            )
        }

        fun start() {
            scenario.moveToState(Lifecycle.State.STARTED)
        }

        fun stop() {
            scenario.moveToState(Lifecycle.State.CREATED)
        }

        fun destroy() {
            scenario.moveToState(Lifecycle.State.DESTROYED)
        }

        fun emit(videoData: List<VideoData>) {
            videoDataFlow.value = videoData
        }

        fun swipeToNextPage() {
            onView(withId(R.id.view_pager))
                .perform(swipeUp())
                .perform(awaitIdleScrollState())
        }

        fun partialSwipe() {
            val partialSwipe = GeneralSwipeAction(
                Swipe.SLOW,
                { floatArrayOf(0f, 256f) },
                { floatArrayOf(0f, 0f) },
                Press.FINGER
            )
            onView(withId(R.id.view_pager))
                .perform(partialSwipe)
                .perform(awaitIdleScrollState())
        }

        fun recreate() {
            scenario.recreate()
        }

        fun tapScreen() {
            onView(withId(R.id.view_pager))
                .perform(click())
        }

        fun rotateLandscape() {
            scenario.onFragment { fragment ->
                fragment.requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }

        fun assertPlayerCreated(count: Int = 1) {
            assertEquals(count, appPlayerFactory.createCount)
        }

        fun assertPlayerReleased() {
            assertTrue(appPlayer.didRelease)
        }

        fun assertPlayerViewStarted() {
            assertTrue(appPlayerView.didAttach)
        }

        fun assertPlayerViewDetached() {
            assertTrue(appPlayerView.didDetach)
        }

        fun assertPage(page: Int) {
            onView(withId(R.id.view_pager))
                .perform(awaitIdleScrollState())
                .check(matches(withPage(page)))
        }

        fun assertImagePreviewVisibility(isVisible: Boolean) {
            val matcher = if (isVisible) {
                isDisplayed()
            } else {
                not(isDisplayed())
            }

            onView(withId(R.id.view_pager))
                .check(matches(atPage(0, matcher, R.id.preview_image)))
        }

        fun assertPlaylistChangedTo(position: Int) {
            assertEquals(position, appPlayer.playingMediaAt)
        }

        fun assertPlayerViewPosition(position: Int) {
            onView(withId(R.id.view_pager))
                .check(matches(atPage(position, hasDescendant(withChild(`is`(appPlayerView.view))))))
        }

        fun assertPlaying(isPlaying: Boolean) {
            assertEquals(isPlaying, appPlayer.currentPlayerState.isPlaying)
        }

        fun assertTextOnScreen(text: String) {
            onView(withText(text))
                .check(matches(isDisplayed()))
        }
    }
}
