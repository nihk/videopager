package com.example.exo_viewpager_fun

import androidx.lifecycle.SavedStateHandle

class PlayerSavedStateHandle(
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