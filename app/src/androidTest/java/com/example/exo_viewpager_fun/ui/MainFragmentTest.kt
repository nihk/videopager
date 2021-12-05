package com.example.exo_viewpager_fun.ui

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.exo_viewpager_fun.R
import com.example.exo_viewpager_fun.TEST_VIDEO_DATA
import com.example.exo_viewpager_fun.data.repositories.FakeVideoDataRepository
import com.example.exo_viewpager_fun.models.AnimationEffect
import com.example.exo_viewpager_fun.models.VideoData
import com.example.exo_viewpager_fun.models.ViewEffect
import com.example.exo_viewpager_fun.players.FakeAppPlayer
import com.example.exo_viewpager_fun.utils.TestImageLoader
import com.example.exo_viewpager_fun.utils.atPage
import com.example.exo_viewpager_fun.utils.awaitIdleScrollState
import com.example.exo_viewpager_fun.utils.withPage
import com.example.exo_viewpager_fun.vm.MainViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MainFragmentTest {
    @Test
    fun whenScreenIsStarted_shouldCreatePlayer() = mainFragment {
        assertPlayerCreated()
    }

    @Test
    fun whenScreenIsStarted_shouldStartPlayerView() = mainFragment {
        assertPlayerViewStarted()
    }

    @Test
    fun whenScreenIsStopped_shouldStopPlayer() = mainFragment {
        stop()

        assertPlayerStopped()
    }

    @Test
    fun whenScreenIsStopped_shouldStopPlayerView() = mainFragment {
        stop()

        assertPlayerViewStopped()
    }

    @Test
    fun whenPlayerHasNotYetRenderedFrames_shouldShowImagePreview() = mainFragment {
        emit(TEST_VIDEO_DATA)

        assertImagePreviewVisibility(isVisible = true)
    }

    @Test
    fun whenPlayerIsRenderingFrames_shouldHideImagePreview() {
        val isPlayerRendering = MutableStateFlow(false)

        mainFragment(isPlayerRendering = isPlayerRendering) {
            emit(TEST_VIDEO_DATA)
            isPlayerRendering.value = true

            assertImagePreviewVisibility(isVisible = false)
        }
    }

    @Test
    fun whenSwipedToNextPage_shouldLoadNextPlayerItem() = mainFragment {
        emit(TEST_VIDEO_DATA)

        swipeToNextPage()

        assertPlaylistChangedTo(1)
    }

    @Test
    fun whenScreenStartedWithVideoData_shouldAttachPlayerView() = mainFragment {
        emit(TEST_VIDEO_DATA)

        assertPlayerViewPosition(0)
    }

    @Test
    fun whenSwipedToNextPage_shouldAttachPlayerViewToNextPage() = mainFragment {
        emit(TEST_VIDEO_DATA)

        swipeToNextPage()

        assertPlayerViewPosition(1)
    }

    @Test
    fun whenScreenIsRecreated_shouldRestorePage() = mainFragment {
        emit(TEST_VIDEO_DATA)

        recreate()

        assertPage(0)

        swipeToNextPage()

        recreate()

        assertPage(1)
    }

    @Test
    fun whenScreenIsTapped_shouldPauseOrPlayPlayer() = mainFragment {
        emit(TEST_VIDEO_DATA)

        tapScreen()

        assertPlaying(false)

        tapScreen()

        assertPlaying(true)
    }

    @Test
    fun whenScreenIsTapped_shouldRenderEffects() = mainFragment {
        emit(TEST_VIDEO_DATA)

        tapScreen()

        assertEffect(AnimationEffect(R.drawable.pause))

        tapScreen()

        assertEffect(AnimationEffect(R.drawable.play))
    }

    @Test
    fun whenErrorIsEmitted_messageIsDisplayed() {
        val errors = MutableStateFlow<Throwable?>(null)
        mainFragment(errors = errors.filterNotNull()) {
            val error = RuntimeException("Uh oh!")

            errors.value = error

            assertTextOnScreen("Uh oh!")
        }
    }

    private fun mainFragment(
        videoData: List<VideoData>? = null,
        isPlayerRendering: Flow<Boolean> = emptyFlow(),
        errors: Flow<Throwable> = emptyFlow(),
        block: MainFragmentRobot.() -> Unit
    ) {
        MainFragmentRobot(videoData, isPlayerRendering, errors).block()
    }

    class MainFragmentRobot(
        videoData: List<VideoData>?,
        isPlayerRendering: Flow<Boolean>,
        errors: Flow<Throwable>
    ) {
        private val taps = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        private val videoDataFlow = MutableStateFlow(videoData)
        private val appPlayer = FakeAppPlayer(isPlayerRendering, errors)
        private val appPlayerFactory = FakeAppPlayer.Factory(appPlayer)
        private val appPlayerView = FakeAppPlayerView(ApplicationProvider.getApplicationContext(), taps)

        private val scenario: FragmentScenario<MainFragment> = launchFragmentInContainer(
            themeResId = R.style.Theme_MaterialComponents_DayNight_DarkActionBar
        ) {
            MainFragment(
                viewModelFactory = MainViewModel.Factory(
                    repository = FakeVideoDataRepository(videoDataFlow),
                    appPlayerFactory = appPlayerFactory,
                ),
                appPlayerViewFactory = object : AppPlayerView.Factory {
                    override fun create(context: Context): AppPlayerView {
                        return appPlayerView
                    }
                },
                imageLoader = TestImageLoader()
            )
        }

        fun stop() {
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

        fun recreate() {
            scenario.recreate()
        }

        fun tapScreen() {
            taps.tryEmit(Unit)
            Espresso.onIdle()
        }

        fun assertPlayerCreated() {
            assertEquals(1, appPlayerFactory.createCount)
        }

        fun assertPlayerStopped() {
            assertTrue(appPlayer.didRelease)
        }

        fun assertPlayerViewStarted() {
            assertTrue(appPlayerView.didAttach)
        }

        fun assertPlayerViewStopped() {
            assertTrue(appPlayerView.didDetach)
        }

        fun assertPage(page: Int) {
            onView(withId(R.id.view_pager))
                .check(matches(withPage(page)))
        }

        fun assertImagePreviewVisibility(isVisible: Boolean) {
            val matcher = if (isVisible) {
                isDisplayed()
            } else {
                not(isDisplayed())
            }

            onView(withId(R.id.preview_image))
                .check(matches(matcher))
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

        fun assertEffect(viewEffect: ViewEffect) {
            assertEquals(viewEffect, appPlayerView.latestEffect)
        }

        fun assertTextOnScreen(text: String) {
            onView(withText(text))
                .check(matches(isDisplayed()))
        }
    }
}
