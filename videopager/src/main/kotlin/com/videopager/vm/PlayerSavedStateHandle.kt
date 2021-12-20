package com.videopager.vm

import androidx.lifecycle.SavedStateHandle
import com.player.models.PlayerState

// Convenience wrapper for a SavedStateHandle.
internal class PlayerSavedStateHandle(
    private val handle: SavedStateHandle
) {
    fun get(): PlayerState? {
        return handle[KEY_PLAYER_STATE]
    }

    fun set(playerState: PlayerState) {
        handle[KEY_PLAYER_STATE] = playerState
    }

    companion object {
        private const val KEY_PLAYER_STATE = "player_state"
    }
}
