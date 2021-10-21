package com.example.exo_viewpager_fun.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayerState(
    val currentMediaItemId: String?,
    val currentMediaIndex: Int? = null,
    val seekPositionMillis: Long,
    val isPlaying: Boolean
) : Parcelable {
    companion object {
        val INITIAL = PlayerState(
            currentMediaItemId = null,
            currentMediaIndex = null,
            seekPositionMillis = 0L,
            isPlaying = true
        )
    }
}
