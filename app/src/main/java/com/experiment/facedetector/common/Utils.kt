package com.experiment.facedetector.common

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
fun <T : Any, R : Any> Flow<PagingData<T>>.suspendMapPagingNotNull(
    transform: suspend (T) -> R
): Flow<PagingData<R>> {
    return this.flatMapConcat { pagingData ->
        flowOf(
            pagingData.map { item ->
                withContext(Dispatchers.Default) {
                    transform(item)
                }
            }
        )
    }
}
