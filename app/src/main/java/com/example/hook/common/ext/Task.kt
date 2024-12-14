package com.example.hook.common.ext

import android.util.Log
import com.example.hook.common.result.Result
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest


fun <T> Task<T>.asFlow() = callbackFlow<Result<T>> {

    this@asFlow.addOnSuccessListener { result ->
        val sendResult = trySend(Result.Success(result))
        if (sendResult.isFailure) trySend(Result.Error(sendResult.exceptionOrNull() ?: Throwable("Unknown error")))
    }

    this@asFlow.addOnFailureListener { exception ->
        trySend(Result.Error(exception))
    }

    awaitClose { /* leave empty */ }
}




fun <T, R> Flow<Result<T>>.mapSuccess(success: (data: T) -> R): Flow<Result<R>> {
    Log.d("Task",success.toString())
    return this.mapLatest { data ->
        when(data) {
            is Result.Error -> Result.Error(data.error)
            is Result.Success -> Result.Success(success(data.data))
        }
    }
}
// ova ekstenzija mapira uspjesan rezultat iz jednog tipa T u drugi R

fun <T, R: Throwable> Flow<Result<out T>>.mapError(errorData: (data: Throwable) -> R): Flow<Result<T>> {
    return this.mapLatest { data ->
        when(data) {
            is Result.Error -> Result.Error(errorData(data.error))
            is Result.Success -> Result.Success(data.data)
        }
    }
}