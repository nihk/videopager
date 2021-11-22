package com.example.exo_viewpager_fun.vm

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.transformLatest

class SubscriberCount(private val minimum: Int) : SharingStarted {
    override fun command(subscriptionCount: StateFlow<Int>): Flow<SharingCommand> {
        return subscriptionCount.transformLatest { count ->
            if (count >= minimum) {
                emit(SharingCommand.START)
            }
        }
    }
}
