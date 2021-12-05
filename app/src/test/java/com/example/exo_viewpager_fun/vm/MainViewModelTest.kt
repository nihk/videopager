package com.example.exo_viewpager_fun.vm

import androidx.lifecycle.SavedStateHandle
import com.example.exo_viewpager_fun.TEST_VIDEO_DATA
import com.example.exo_viewpager_fun.data.repositories.FakeVideoDataRepository
import com.example.exo_viewpager_fun.models.OnPageSettledEvent
import com.example.exo_viewpager_fun.models.PlayerErrorEffect
import com.example.exo_viewpager_fun.models.PlayerLifecycleEvent
import com.example.exo_viewpager_fun.models.PlayerState
import com.example.exo_viewpager_fun.models.TappedPlayerEvent
import com.example.exo_viewpager_fun.models.VideoData
import com.example.exo_viewpager_fun.models.ViewEffect
import com.example.exo_viewpager_fun.models.ViewState
import com.example.exo_viewpager_fun.players.FakeAppPlayer
import com.example.exo_viewpager_fun.utils.CoroutinesTestRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {
    @get:Rule
    val rule = CoroutinesTestRule()

    @Test
    fun `should create new player when none exist`() = mainViewModel {
        startPlayer()

        assertPlayerCreatedCount(1)
        assertStateOwnsPlayer()
    }

    @Test
    fun `should not create new player when one exists`() = mainViewModel {
        startPlayer()
        startPlayer()

        assertPlayerCreatedCount(1)
        assertStateOwnsPlayer()
    }

    @Test
    fun `should create new player after stop event and not changing configs`() = mainViewModel {
        startPlayer()
        tearDownPlayer(isChangingConfigurations = false)
        startPlayer()

        assertPlayerCreatedCount(2)
        assertStateOwnsPlayer()
    }

    @Test
    fun `should tear down player when stopped event and not changing configs`() = mainViewModel {
        startPlayer()
        tearDownPlayer(isChangingConfigurations = false)

        assertPlayerReleased(didRelease = true)
        assertStateDoesNotOwnPlayer()
    }

    @Test
    fun `should not tear down player when stopped event and changing configs`() = mainViewModel {
        startPlayer()
        tearDownPlayer(isChangingConfigurations = true)

        assertPlayerReleased(didRelease = false)
        assertStateOwnsPlayer()
    }

    @Test
    fun `should save player state when view model is torn down`() = mainViewModel {
        val playerState = PlayerState(
            currentMediaItemId = "id",
            seekPositionMillis = 60L,
            isPlaying = false
        )
        startPlayer()
        setPlayerState(playerState)
        tearDownPlayer(isChangingConfigurations = false)

        assertPlayerStateSaved(playerState)
    }

    @Test
    fun `should hide player when media position is changed`() = mainViewModel(
        isPlayerRendering = flowOf(true)
    ) {
        startPlayer()
        setCurrentMediaIndex(7)
        changeMediaPosition(42)

        assertIsLoading(true)
    }

    @Test
    fun `should not hide player when media position change to same position is attempted`() = mainViewModel(
        isPlayerRendering = flowOf(true)
    ) {
        startPlayer()
        setCurrentMediaIndex(7)
        changeMediaPosition(7)

        assertIsLoading(false)
    }

    @Test
    fun `should show player when player starts rendering`() {
        val isPlayerRendering = MutableStateFlow(false)
        mainViewModel(isPlayerRendering = isPlayerRendering) {
            startPlayer()
            isPlayerRendering.value = true

            assertIsLoading(false)
        }
    }

    @Test
    fun `should emit video data when repository emits video data`() = mainViewModel {
        emitVideoData(TEST_VIDEO_DATA)

        assertCachedVideoData(TEST_VIDEO_DATA)
    }

    @Test
    fun `should setup app player when repository emits video data`() = mainViewModel {
        startPlayer()
        val videoData = TEST_VIDEO_DATA + listOf(VideoData(mediaUri = "asdf", previewImageUri = "png"))
        emitVideoData(videoData)

        assertPlayerSetupWith(videoData)
    }

    @Test
    fun `should setup app player when player is requested and cached video data exists`() = mainViewModel(
        videoData = TEST_VIDEO_DATA
    ) {
        startPlayer()

        assertPlayerSetupWith(TEST_VIDEO_DATA)
    }

    @Test
    fun `should pause player when tapped while playing`() = mainViewModel(
        initialPlayerState = PlayerState.INITIAL.copy(isPlaying = true)
    ) {
        startPlayer()

        tapPlayer()

        assertPlaying(false)
    }

    @Test
    fun `should play player when tapped while paused`() = mainViewModel(
        initialPlayerState = PlayerState.INITIAL.copy(isPlaying = false)
    ) {
        startPlayer()

        tapPlayer()

        assertPlaying(true)
    }

    @Test
    fun `should play player when tapped twice`() = mainViewModel {
        startPlayer()

        tapPlayer()
        tapPlayer()

        assertPlaying(true)
    }

    @Test
    fun `should emit error effect when error happens`() {
        val errors = MutableStateFlow<Throwable?>(null)
        mainViewModel(errors = errors.filterNotNull()) {
            startPlayer()

            val error = RuntimeException("Uh oh!")
            errors.value = error

            assertErrorEffect(error)
        }
    }

    fun mainViewModel(
        initialPlayerState: PlayerState = PlayerState.INITIAL,
        videoData: List<VideoData> = TEST_VIDEO_DATA,
        isPlayerRendering: Flow<Boolean> = emptyFlow(),
        errors: Flow<Throwable> = emptyFlow(),
        block: MainViewModelRobot.() -> Unit
    ) {
        MainViewModelRobot(
            initialPlayerState = initialPlayerState,
            videoData = videoData,
            isPlayerRendering = isPlayerRendering,
            errors = errors,
            scope = TestCoroutineScope(rule.testDispatcher)
        ).block()
    }

    class MainViewModelRobot(
        initialPlayerState: PlayerState,
        videoData: List<VideoData>,
        isPlayerRendering: Flow<Boolean>,
        errors: Flow<Throwable>,
        scope: CoroutineScope
    ) {
        private val appPlayer = FakeAppPlayer(isPlayerRendering, errors).apply {
            currentPlayerState = initialPlayerState
        }
        private val appPlayerFactory = FakeAppPlayer.Factory(appPlayer)
        private val handle = PlayerSavedStateHandle(
            handle = SavedStateHandle()
        ).apply { set(initialPlayerState) }
        private val videoDataFlow = MutableStateFlow(videoData)
        private val viewModel = MainViewModel(
            repository = FakeVideoDataRepository(videoDataFlow),
            appPlayerFactory = appPlayerFactory,
            handle = handle,
            initialState = ViewState(videoData = videoData)
        )
        private val collectedStates = mutableListOf<ViewState>()
        private val collectedEffects = mutableListOf<ViewEffect>()

        init {
            viewModel.states.onEach(collectedStates::add).launchIn(scope)
            viewModel.effects.onEach(collectedEffects::add).launchIn(scope)
        }

        fun startPlayer() {
            viewModel.processEvent(PlayerLifecycleEvent.Start)
        }

        fun tearDownPlayer(isChangingConfigurations: Boolean) {
            viewModel.processEvent(PlayerLifecycleEvent.Stop(isChangingConfigurations))
        }

        fun setPlayerState(playerState: PlayerState) {
            appPlayer.currentPlayerState = playerState
        }

        fun setCurrentMediaIndex(index: Int) {
            appPlayer.currentPlayerState = appPlayer.currentPlayerState.copy(currentMediaIndex = index)
        }

        fun changeMediaPosition(position: Int) {
            viewModel.processEvent(OnPageSettledEvent(position))
        }

        fun emitVideoData(videoData: List<VideoData>) {
            videoDataFlow.value = videoData
        }

        fun tapPlayer() {
            viewModel.processEvent(TappedPlayerEvent)
        }

        fun assertPlayerCreatedCount(times: Int) {
            assertEquals(times, appPlayerFactory.createCount)
        }

        fun assertPlayerReleased(didRelease: Boolean) {
            assertEquals(didRelease, appPlayer.didRelease)
        }

        fun assertStateOwnsPlayer() {
            assertNotNull(viewModel.states.value.appPlayer)
        }

        fun assertStateDoesNotOwnPlayer() {
            assertNull(viewModel.states.value.appPlayer)
        }

        fun assertPlayerStateSaved(playerState: PlayerState) {
            assertEquals(playerState, handle.get())
        }

        fun assertIsLoading(value: Boolean) {
            assertEquals(value, viewModel.states.value.isLoading)
        }

        fun assertCachedVideoData(videoData: List<VideoData>) {
            assertEquals(videoData, viewModel.states.value.videoData)
        }

        fun assertPlayerSetupWith(videoData: List<VideoData>) {
            assertEquals(videoData, appPlayer.setups.last())
        }

        fun assertPlaying(isPlaying: Boolean) {
            assertEquals(isPlaying, appPlayer.currentPlayerState.isPlaying)
        }

        fun assertErrorEffect(throwable: Throwable) {
            assertEquals(throwable, (collectedEffects.last() as PlayerErrorEffect).throwable)
        }
    }
}
