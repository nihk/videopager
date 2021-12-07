package com.example.exo_viewpager_fun.vm

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import com.example.exo_viewpager_fun.R
import com.example.exo_viewpager_fun.data.repositories.VideoDataRepository
import com.example.exo_viewpager_fun.models.AnimationEffect
import com.example.exo_viewpager_fun.models.AttachPlayerToViewResult
import com.example.exo_viewpager_fun.models.CreatePlayerResult
import com.example.exo_viewpager_fun.models.LoadVideoDataEvent
import com.example.exo_viewpager_fun.models.LoadVideoDataResult
import com.example.exo_viewpager_fun.models.NoOpResult
import com.example.exo_viewpager_fun.models.OnPageChangedEvent
import com.example.exo_viewpager_fun.models.OnPageSettledEvent
import com.example.exo_viewpager_fun.models.OnPageSettledResult
import com.example.exo_viewpager_fun.models.PlayerErrorEffect
import com.example.exo_viewpager_fun.models.PlayerErrorResult
import com.example.exo_viewpager_fun.models.PlayerLifecycleEvent
import com.example.exo_viewpager_fun.models.OnPlayerRenderingResult
import com.example.exo_viewpager_fun.models.ResetAnimationsEffect
import com.example.exo_viewpager_fun.models.TappedPlayerEvent
import com.example.exo_viewpager_fun.models.TappedPlayerResult
import com.example.exo_viewpager_fun.models.TearDownPlayerResult
import com.example.exo_viewpager_fun.models.ViewEffect
import com.example.exo_viewpager_fun.models.ViewEvent
import com.example.exo_viewpager_fun.models.ViewResult
import com.example.exo_viewpager_fun.models.ViewState
import com.example.exo_viewpager_fun.players.AppPlayer
import com.example.exo_viewpager_fun.ui.extensions.ViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

/**
 * Owns a stateful [ViewState.appPlayer] instance that will get created and torn down in parallel
 * with Activity lifecycle state changes.
 */
class MainViewModel(
    private val repository: VideoDataRepository,
    private val appPlayerFactory: AppPlayer.Factory,
    private val handle: PlayerSavedStateHandle,
    initialState: ViewState
) : MviViewModel<ViewEvent, ViewResult, ViewState, ViewEffect>(initialState) {

    override fun onStart() {
        processEvent(LoadVideoDataEvent)
    }

    override fun Flow<ViewEvent>.toResults(): Flow<ViewResult> {
        // MVI boilerplate
        return merge(
            filterIsInstance<LoadVideoDataEvent>().toLoadVideoDataResults(),
            filterIsInstance<PlayerLifecycleEvent>().toPlayerLifecycleResults(),
            filterIsInstance<TappedPlayerEvent>().toTappedPlayerResults(),
            filterIsInstance<OnPageSettledEvent>().toPageSettledResults(),
            filterIsInstance<OnPageChangedEvent>().toOnPageChangedResults()
        )
    }

    private fun Flow<LoadVideoDataEvent>.toLoadVideoDataResults(): Flow<ViewResult> {
        return flatMapLatest { repository.videoData() }
            .onEach { videoData ->
                // If the player exists, it should be updated with the latest video data that came in
                states.value.appPlayer?.setUpWith(videoData, handle.get())
            }
            .map { videoData -> LoadVideoDataResult(videoData) }
    }

    /**
     * This is a single flow instead of two distinct ones (e.g. one for starting, one for stopping)
     * so that when the PlayerLifecycleEvent.Type changes from upstream, the flow initiated by the
     * previous Type gets unsubscribed from (see: [flatMapLatest]). This is necessary to cancel flows
     * tied to the AppPlayer instance, e.g. [AppPlayer.onPlayerRendering], when the player is being
     * torn down.
     */
    private fun Flow<PlayerLifecycleEvent>.toPlayerLifecycleResults(): Flow<ViewResult> {
        val managePlayerInstance = filterNot { event ->
            // Don't need to create a player when one already exists. This can happen
            // after a configuration change
            states.value.appPlayer != null && event is PlayerLifecycleEvent.Start
                // Don't tear down the player across configuration changes
                || event is PlayerLifecycleEvent.Stop && event.isChangingConfigurations
        }.flatMapLatest { event ->
            when (event) {
                is PlayerLifecycleEvent.Start -> createPlayer()
                is PlayerLifecycleEvent.Stop -> tearDownPlayer()
            }
        }

        return merge(
            mapLatest { event -> AttachPlayerToViewResult(doAttach = event is PlayerLifecycleEvent.Start) },
            managePlayerInstance
        )
    }

    private fun createPlayer(): Flow<ViewResult> {
        check(states.value.appPlayer == null) { "Tried to create a player when one already exists" }
        val config = AppPlayer.Factory.Config(loopVideos = true)
        val appPlayer = appPlayerFactory.create(config)
        // If video data already exists then the player should have that video data set on it. This
        // can happen because the player has a lifecycle tied to Activity starting/stopping.
        states.value.videoData?.let { videoData -> appPlayer.setUpWith(videoData, handle.get()) }
        return merge(
            flowOf(CreatePlayerResult(appPlayer)),
            appPlayer.onPlayerRendering()
                // Only one rendered result per page. ExoPlayer can emit multiple 'onRenderedFirstFrame's
                // for the same video, unfortunately. This is a workaround to that.
                .distinctUntilChangedBy { states.value.page }
                .map { OnPlayerRenderingResult },
            appPlayer.errors().map(::PlayerErrorResult)
        )
    }

    private fun tearDownPlayer(): Flow<ViewResult> {
        val appPlayer = requireNotNull(states.value.appPlayer)
        // Keep track of player state so that it can be restored across player recreations.
        handle.set(appPlayer.currentPlayerState)
        // Videos are a heavy resource, so tear player down when the app is not in the foreground.
        appPlayer.release()
        return flowOf(TearDownPlayerResult)
    }

    private fun Flow<TappedPlayerEvent>.toTappedPlayerResults(): Flow<ViewResult> {
        return mapLatest {
            val appPlayer = requireNotNull(states.value.appPlayer)
            val drawable = if (appPlayer.currentPlayerState.isPlaying) {
                appPlayer.pause()
                R.drawable.pause
            } else {
                appPlayer.play()
                R.drawable.play
            }

            TappedPlayerResult(drawable)
        }
    }

    private fun Flow<OnPageSettledEvent>.toPageSettledResults(): Flow<ViewResult> {
        return mapLatest { event ->
            val appPlayer = requireNotNull(states.value.appPlayer)
            val changeVideo = appPlayer.currentPlayerState.currentMediaItemIndex != event.page
            if (changeVideo) {
                appPlayer.playMediaAt(event.page)
            }

            OnPageSettledResult(
                page = event.page,
                didChangeVideo = changeVideo
            )
        }
    }

    private fun Flow<OnPageChangedEvent>.toOnPageChangedResults(): Flow<ViewResult> {
        return mapLatest {
            val appPlayer = requireNotNull(states.value.appPlayer)
            appPlayer.pause()
            NoOpResult
        }
    }

    override fun ViewResult.reduce(state: ViewState): ViewState {
        // MVI reducer boilerplate
        return when (this) {
            is LoadVideoDataResult -> state.copy(videoData = videoData)
            is CreatePlayerResult -> state.copy(appPlayer = appPlayer)
            is TearDownPlayerResult -> state.copy(appPlayer = null)
            is OnPageSettledResult -> state.copy(page = page, isLoading = didChangeVideo || state.isLoading)
            is OnPlayerRenderingResult -> state.copy(isLoading = false)
            is AttachPlayerToViewResult -> state.copy(attachPlayer = doAttach)
            else -> state
        }
    }

    override fun Flow<ViewResult>.toEffects(): Flow<ViewEffect> {
        return merge(
            filterIsInstance<TappedPlayerResult>().toTappedPlayerEffects(),
            filterIsInstance<OnPageSettledResult>().toPageSettledEffects(),
            filterIsInstance<PlayerErrorResult>().toPlayerErrorEffects()
        )
    }

    private fun Flow<TappedPlayerResult>.toTappedPlayerEffects(): Flow<ViewEffect> {
        return mapLatest { result -> AnimationEffect(result.drawable) }
    }

    private fun Flow<OnPageSettledResult>.toPageSettledEffects(): Flow<ViewEffect> {
        return filter { result -> result.didChangeVideo }
            .mapLatest { ResetAnimationsEffect }
    }

    private fun Flow<PlayerErrorResult>.toPlayerErrorEffects(): Flow<ViewEffect> {
        return mapLatest { result -> PlayerErrorEffect(result.throwable) }
    }

    class Factory(
        private val repository: VideoDataRepository,
        private val appPlayerFactory: AppPlayer.Factory
    ) {
        fun create(owner: SavedStateRegistryOwner): ViewModelProvider.Factory {
            return object : AbstractSavedStateViewModelFactory(owner, null) {
                override fun <T : ViewModel?> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    val playerSavedStateHandle = PlayerSavedStateHandle(handle)

                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(
                        repository = repository,
                        appPlayerFactory = appPlayerFactory,
                        handle = playerSavedStateHandle,
                        initialState = ViewState(playerSavedStateHandle)
                    ) as T
                }
            }
        }
    }
}
