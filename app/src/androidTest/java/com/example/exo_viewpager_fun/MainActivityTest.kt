package com.example.exo_viewpager_fun

import android.view.LayoutInflater
import androidx.lifecycle.Lifecycle
import androidx.savedstate.SavedStateRegistryOwner
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import coil.ImageLoader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MainActivityTest {
    @Test
    fun whenActivityIsStarted_shouldCreatePlayer() = mainActivity {
        assertPlayerCreated()
    }

    @Test
    fun whenActivityIsStarted_shouldStartPlayerView() = mainActivity {
        assertPlayerViewStarted()
    }

    @Test
    fun whenActivityIsStopped_shouldStopPlayer() = mainActivity {
        stop()

        assertPlayerStopped()
    }

    @Test
    fun whenActivityIsStopped_shouldStopPlayerView() = mainActivity {
        stop()

        assertPlayerViewStopped()
    }

    @Test
    fun whenPlayerHasNotYetRenderedFrames_shouldShowImagePreview() = mainActivity {
        emit(TEST_VIDEO_DATA)

        assertImagePreviewVisibility(isVisible = true)
    }

    @Test
    fun whenPlayerIsRenderingFrames_shouldHideImagePreview() {
        val isPlayerRendering = MutableStateFlow(false)

        mainActivity(isPlayerRendering = isPlayerRendering) {
            emit(TEST_VIDEO_DATA)
            isPlayerRendering.value = true

            assertImagePreviewVisibility(isVisible = false)
        }
    }

    @Test
    fun whenSwipedToNextPage_shouldLoadNextPlayerItem() = mainActivity {
        emit(TEST_VIDEO_DATA)

        swipeToNextPage()

        assertPlaylistChangedTo(1)
    }

    @Test
    fun whenActivityStartedWithVideoData_shouldAttachPlayerView() = mainActivity {
        emit(TEST_VIDEO_DATA)

        assertPlayerViewPosition(0)
    }

    @Test
    fun whenSwipedToNextPage_shouldAttachPlayerViewToNextPage() = mainActivity {
        emit(TEST_VIDEO_DATA)

        swipeToNextPage()

        assertPlayerViewPosition(1)
    }

    @Test
    fun whenActivityIsRecreated_shouldRestorePage() = mainActivity {
        emit(TEST_VIDEO_DATA)

        recreate()

        assertPage(0)

        swipeToNextPage()

        recreate()

        assertPage(1)
    }

    fun mainActivity(
        videoData: List<VideoData>? = null,
        isPlayerRendering: Flow<Boolean> = emptyFlow(),
        block: MainActivityRobot.() -> Unit
    ) {
        MainActivityRobot(videoData, isPlayerRendering).block()
    }

    class MainActivityRobot(
        videoData: List<VideoData>?,
        isPlayerRendering: Flow<Boolean>
    ) {
        private val app: App = ApplicationProvider.getApplicationContext()
        private val videoDataFlow = MutableStateFlow(videoData)
        private val appPlayer = FakeAppPlayer(isPlayerRendering)
        private val appPlayerFactory = FakeAppPlayer.Factory(appPlayer)
        private val appPlayerView = FakeAppPlayerView(app)
        private val scenario: ActivityScenario<MainActivity>

        init {
            app.mainModule = object : MainModule {
                override fun viewModelFactory(savedStateRegistryOwner: SavedStateRegistryOwner): MainViewModel.Factory {
                    return MainViewModel.Factory(
                        repository = FakeVideoDataRepository(videoDataFlow),
                        appPlayerFactory = appPlayerFactory,
                        savedStateRegistryOwner = savedStateRegistryOwner
                    )
                }

                override fun appPlayerView(layoutInflater: LayoutInflater): AppPlayerView {
                    return appPlayerView
                }

                override fun imageLoader(): ImageLoader {
                    return TestImageLoader()
                }
            }

            scenario = launchActivity()
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
                .perform(AwaitIdleScrollState())
        }

        fun recreate() {
            scenario.recreate()
        }

        fun assertPlayerCreated() {
            assertEquals(1, appPlayerFactory.createCount)
        }

        fun assertPlayerStopped() {
            assertTrue(appPlayer.didRelease)
        }

        fun assertPlayerViewStarted() {
            assertTrue(appPlayerView.didStart)
        }

        fun assertPlayerViewStopped() {
            assertTrue(appPlayerView.didStop)
        }

        fun assertPage(page: Int) {
            onView(withId(R.id.view_pager))
                .check(matches(ViewPager2Page(page)))
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
                .check(matches(AtViewPager2Position(position, IsParentOf(appPlayerView.view))))
        }
    }
}
