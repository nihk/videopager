package com.example.exo_viewpager_fun

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.Closeable

class MainViewModelTest {
    @get:Rule
    val rule = CoroutinesTestRule()

    @Test
    fun `should create new player when non exist`() = mainViewModel {
        initPlayer()

        assertPlayerCreatedCount(1)
    }

    @Test
    fun `should not create new player when one exists`() = mainViewModel {
        initPlayer()
        initPlayer()

        assertPlayerCreatedCount(1)
    }

    @Test
    fun `should create new player when cached is torn down`() = mainViewModel {
        initPlayer()
        tearDown()
        initPlayer()

        assertPlayerCreatedCount(2)
    }

    @Test
    fun `should tear down player when view model is torn down`() = mainViewModel {
        initPlayer()
        tearDown()

        assertPlayerReleased()
    }

    @Test
    fun `should save player state when view model is torn down`() = mainViewModel {
        val playerState = PlayerState(
            currentMediaItemId = "id",
            seekPositionMillis = 60L,
            isPlaying = false
        )
        initPlayer()
        setPlayerState(playerState)
        tearDown()

        assertPlayerStateSaved(playerState)
    }

    @Test
    fun `should hide player when media position is changed`() = mainViewModel(
        isPlayerRendering = flowOf(true)
    ) {
        initPlayer()
        setCurrentMediaIndex(7)
        changeMediaPosition(42)

        assertShowPlayer(false)
    }

    @Test
    fun `should not hide player when media position change to same position is attempted`() = mainViewModel(
        isPlayerRendering = flowOf(true)
    ) {
        initPlayer()
        setCurrentMediaIndex(7)
        changeMediaPosition(7)

        assertShowPlayer(true)
    }

    @Test
    fun `should show player when player starts rendering`() {
        val isPlayerRendering = MutableStateFlow(false)
        mainViewModel(isPlayerRendering = isPlayerRendering) {
            initPlayer()
            isPlayerRendering.value = true

            assertShowPlayer(true)
        }
    }

    @Test
    fun `should emit video data when repository emits video data`() = mainViewModel {
        emitVideoData(TEST_VIDEO_DATA)

        assertCachedVideoData(TEST_VIDEO_DATA)
    }

    @Test
    fun `should setup app player when repository emits video data`() = mainViewModel {
        initPlayer()
        val videoData = TEST_VIDEO_DATA + listOf(VideoData(mediaUri = "asdf", previewImageUri = "png"))
        emitVideoData(videoData)

        assertPlayerSetupWith(videoData)
    }

    @Test
    fun `should setup app player when player is requested and cached video data exists`() = mainViewModel(
        videoData = TEST_VIDEO_DATA
    ) {
        initPlayer()

        assertPlayerSetupWith(TEST_VIDEO_DATA)
    }

    fun mainViewModel(
        initialState: PlayerState = PlayerState.INITIAL,
        videoData: List<VideoData> = TEST_VIDEO_DATA,
        isPlayerRendering: Flow<Boolean> = emptyFlow(),
        block: MainViewModelRobot.() -> Unit
    ) {
        MainViewModelRobot(initialState, videoData, isPlayerRendering).use(block)
    }

    class MainViewModelRobot(
        initialState: PlayerState,
        videoData: List<VideoData>,
        isPlayerRendering: Flow<Boolean>
    ) : Closeable {
        private val appPlayer = FakeAppPlayer(isPlayerRendering)
        private val appPlayerFactory = FakeAppPlayer.Factory(appPlayer)
        private val handle = PlayerSavedStateHandle(
            handle = SavedStateHandle()
        ).apply { set(initialState) }
        private val videoDataFlow = MutableStateFlow(videoData)
        private val viewModel = MainViewModel(
            repository = FakeVideoDataRepository(videoDataFlow),
            appPlayerFactory = appPlayerFactory,
            handle = handle
        )

        fun initPlayer() {
            viewModel.getPlayer()
        }

        fun tearDown() {
            viewModel.tearDown()
        }

        fun setPlayerState(playerState: PlayerState) {
            appPlayer.currentPlayerState = playerState
        }

        fun setCurrentMediaIndex(index: Int) {
            appPlayer.currentMediaIndex = index
        }

        fun changeMediaPosition(position: Int) {
            viewModel.playMediaAt(position)
        }

        fun emitVideoData(videoData: List<VideoData>) {
            videoDataFlow.value = videoData
        }

        fun assertPlayerCreatedCount(times: Int) {
            assertEquals(times, appPlayerFactory.createCount)
        }

        fun assertPlayerReleased() {
            assertTrue(appPlayer.didRelease)
        }

        fun assertPlayerStateSaved(playerState: PlayerState) {
            assertEquals(playerState, handle.get())
        }

        fun assertShowPlayer(value: Boolean) = runBlocking {
            assertEquals(value, viewModel.showPlayer().first())
        }

        fun assertCachedVideoData(videoData: List<VideoData>) {
            assertEquals(videoData, viewModel.videoData.value)
        }

        fun assertPlayerSetupWith(videoData: List<VideoData>) {
            assertEquals(videoData, appPlayer.setups.last())
        }

        override fun close() {
            viewModel.tearDown()
        }
    }

    companion object {
        private val TEST_VIDEO_DATA = listOf(
            VideoData(
                mediaUri = "https://www.example.com/video.mp4",
                previewImageUri = "https://www.example.com/image.jpeg"
            )
        )
    }
}

class FakeVideoDataRepository(private val flow: Flow<List<VideoData>>) : VideoDataRepository {
    override fun videoData(): Flow<List<VideoData>> {
        return flow
    }
}

class FakeAppPlayer(private val isPlayerRendering: Flow<Boolean>) : AppPlayer {
    override var currentPlayerState: PlayerState = PlayerState.INITIAL
    override var currentMediaIndex: Int = 0
    val setups = mutableListOf<List<VideoData>>()
    var didPlayMediaAt: Boolean = false
    var didRelease: Boolean = false

    override fun setUpWith(videoData: List<VideoData>, playerState: PlayerState?) {
        setups += videoData
    }

    override fun isPlayerRendering() = isPlayerRendering

    override fun playMediaAt(position: Int) {
        didPlayMediaAt = true
    }

    override fun release() {
        didRelease = true
    }

    class Factory(private val appPlayer: AppPlayer) : AppPlayer.Factory {
        var createCount = 0

        override fun create(config: AppPlayer.Factory.Config): AppPlayer {
            ++createCount
            return appPlayer
        }
    }
}
