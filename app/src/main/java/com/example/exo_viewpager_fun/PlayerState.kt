package com.example.exo_viewpager_fun

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayerState(
    val currentMediaItemId: String?,
    val seekPositionMillis: Long,
    val isPlaying: Boolean
) : Parcelable {
    companion object {
        val INITIAL = PlayerState(
            currentMediaItemId = null,
            seekPositionMillis = 0L,
            isPlaying = true
        )
    }
}
