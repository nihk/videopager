package com.example.exo_viewpager_fun.vm

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.example.exo_viewpager_fun.R
import com.example.exo_viewpager_fun.data.VideoDataRepository
import com.example.exo_viewpager_fun.models.AnimationEffect
import com.example.exo_viewpager_fun.models.AttachPlayerToViewEvent
import com.example.exo_viewpager_fun.models.AttachPlayerToViewResult
import com.example.exo_viewpager_fun.models.CreatePlayerResult
import com.example.exo_viewpager_fun.models.LoadVideoDataEvent
import com.example.exo_viewpager_fun.models.LoadVideoDataResult
import com.example.exo_viewpager_fun.models.OnPageSettledEvent
import com.example.exo_viewpager_fun.models.OnPageSettledResult
import com.example.exo_viewpager_fun.models.PlayerLifecycleEvent
import com.example.exo_viewpager_fun.models.PlayerRenderingResult
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
 * Owns a stateful [ViewState.appPlayer] instance that will frequently get created and torn down in
 * parallel with Activity lifecycle state changes.
 */
class MainViewModel(
    private val repository: VideoDataRepository,
    private val appPlayerFactory: AppPlayer.Factory,
    private val handle: PlayerSavedStateHandle,
    initialState: ViewState
) : MviViewModel<ViewEvent, ViewResult, ViewState, ViewEffect>(initialState, LoadVideoDataEvent) {

    override fun Flow<ViewEvent>.toResults(): Flow<ViewResult> {
        return merge(
            filterIsInstance<LoadVideoDataEvent>().toLoadVideoDataResults(),
            filterIsInstance<PlayerLifecycleEvent>().toPlayerLifecycleResults(),
            filterIsInstance<AttachPlayerToViewEvent>().toAttachPlayerToViewResults(),
            filterIsInstance<TappedPlayerEvent>().toTappedPlayerResults(),
            filterIsInstance<OnPageSettledEvent>().toPageSettledResults()
        )
    }

    private fun Flow<LoadVideoDataEvent>.toLoadVideoDataResults(): Flow<ViewResult> {
        return flatMapLatest { repository.videoData() }
            .onEach { videoData -> states.value.appPlayer?.setUpWith(videoData, handle.get()) }
            .map { videoData -> LoadVideoDataResult(videoData) }
    }

    private fun Flow<PlayerLifecycleEvent>.toPlayerLifecycleResults(): Flow<ViewResult> {
        return filterNot { event ->
            // Don't need to create a player when one already exists. This can happen
            // after a configuration change
            states.value.appPlayer != null && event.type is PlayerLifecycleEvent.Type.Start
                // Keep player in memory across configuration changes
                || event.type is PlayerLifecycleEvent.Type.Stop && event.type.isChangingConfigurations
        }.flatMapLatest { event ->
            when (event.type) {
                is PlayerLifecycleEvent.Type.Start -> createPlayer()
                is PlayerLifecycleEvent.Type.Stop -> tearDownPlayer()
            }
        }
    }

    private fun createPlayer(): Flow<ViewResult> {
        val config = AppPlayer.Factory.Config(loopVideos = true)
        val appPlayer = appPlayerFactory.create(config)
        states.value.videoData?.let { videoData -> appPlayer.setUpWith(videoData, handle.get()) }
        return merge(
            flowOf(CreatePlayerResult(appPlayer)),
            appPlayer.isPlayerRendering().map(::PlayerRenderingResult)
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

    private fun Flow<AttachPlayerToViewEvent>.toAttachPlayerToViewResults(): Flow<ViewResult> {
        return mapLatest { event -> AttachPlayerToViewResult(event.doAttach) }
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
            val changeVideo = appPlayer.currentPlayerState.currentMediaIndex != event.page
            if (changeVideo) {
                appPlayer.playMediaAt(event.page)
            }

            OnPageSettledResult(
                page = event.page,
                didChangeVideo = changeVideo
            )
        }
    }

    override fun ViewResult.reduce(state: ViewState): ViewState {
        return when (this) {
            is LoadVideoDataResult -> state.copy(videoData = videoData)
            is CreatePlayerResult -> state.copy(appPlayer = appPlayer)
            is TearDownPlayerResult -> state.copy(appPlayer = null)
            is OnPageSettledResult -> state.copy(page = page, showPlayer = !didChangeVideo) // Hide the player if loading a new video
            is PlayerRenderingResult -> state.copy(showPlayer = isPlayerRendering)
            is AttachPlayerToViewResult -> state.copy(attachPlayer = doAttach)
            else -> state
        }
    }

    override fun Flow<ViewResult>.toEffects(): Flow<ViewEffect> {
        return merge(
            filterIsInstance<TappedPlayerResult>().toTappedPlayerEffects(),
            filterIsInstance<OnPageSettledResult>().toPageSettledEffects()
        )
    }

    private fun Flow<TappedPlayerResult>.toTappedPlayerEffects(): Flow<ViewEffect> {
        return mapLatest { result -> AnimationEffect(result.drawable) }
    }

    private fun Flow<OnPageSettledResult>.toPageSettledEffects(): Flow<ViewEffect> {
        return filter { result -> result.didChangeVideo }
            .mapLatest { ResetAnimationsEffect }
    }

    class Factory(
        private val repository: VideoDataRepository,
        private val appPlayerFactory: AppPlayer.Factory,
        savedStateRegistryOwner: SavedStateRegistryOwner
    ) : AbstractSavedStateViewModelFactory(savedStateRegistryOwner, null) {
        override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            val handle =  PlayerSavedStateHandle(handle)
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                repository = repository,
                appPlayerFactory = appPlayerFactory,
                handle = handle,
                initialState = ViewState(handle)
            ) as T
        }
    }
}

