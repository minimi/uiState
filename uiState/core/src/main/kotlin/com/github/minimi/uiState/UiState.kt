package com.github.minimi.uiState

/**
 * Class describing UI State.
 *
 * Usually we use such class to represents state of screen or its part when data is loading,
 * when data successfully received and we need to show results, and when error occurs during loading
 * and we need show an error.
 */
sealed class UiState<out T> {
    object Loading: UiState<Nothing>()
    data class Success<out T>(val data: T): UiState<T>()
    data class Error(val cause: Throwable): UiState<Nothing>()
}

/**
 * Indicates the UiState is Success
 *
 * @return Boolean true if success
 */
val <T> UiState<T>.isSuccess: Boolean get() = this is UiState.Success

/**
 * Indicates the UiState is Loading
 *
 * @return Boolean true if is loading
 */
val <T> UiState<T>.isLoading: Boolean get() = this is UiState.Loading

/**
 * Indicates the UiState is Error
 *
 * @return Boolean true if is failed
 */
val <T> UiState<T>.isFailed: Boolean get() = this is UiState.Error

/**
 * Transform one [UiState] with generic type [T] class to another one with generic type [R] using
 * [transform] lambda function
 *
 * @return UiState of new generic type [R]
 */
fun <T, R> UiState<T>.map(transform: (UiState<T>) -> UiState<R>): UiState<R> {
    return transform(this)
}

/**
 * Transform one [UiState] with generic type [T] class to another one with generic type [R] using
 * [transform] lambda function
 *
 * @return UiState of new generic type [R]
 */
fun <T, R> UiState<T>.flatMap(transform: (T) -> R): UiState<R> {
    return when(this) {
        is UiState.Success -> UiState.Success(transform.invoke(this.data))
        is UiState.Error -> UiState.Error(this.cause)
        is UiState.Loading -> UiState.Loading
    }
}

/**
 * @return value of [T] if state is Success or null otherwise
 */
fun <T> UiState<T>.getOrNull(): T? {
    if(isSuccess) return (this as UiState.Success).data
    return null
}

/**
 * @return [Throwable] if state is Error or null otherwise
 */
fun <T> UiState<T>.exceptionOrNull(): Throwable? {
    if(isFailed) return (this as UiState.Error).cause
    return null
}

/**
 * Invokes [block] in case of Success state
 *
 * @param block lambda function that will be called in case if state is [UiState.Success]
 * @return [this] for chaining
 */
inline fun <T> UiState<T>.onSuccess(block: (value: T) -> Unit): UiState<T> {
    if (isSuccess) block((this as UiState.Success).data)
    return this
}

/**
 * Invokes [block] in case of Error state
 *
 * @param block lambda function that will be called in case if state is [UiState.Error]
 * @return [this] for chaining
 */
inline fun <T> UiState<T>.onFailure(block: (exception: Throwable) -> Unit): UiState<T> {
    if (isFailed) block((this as UiState.Error).cause)
    return this
}

/**
 * Invokes [block] in case of Success state
 *
 * @param block lambda function that will be called in case if state is [UiState.Loading]
 * @return [this] for chaining
 */
inline fun <T> UiState<T>.onLoading(block: () -> Unit): UiState<T> {
    if (isLoading) block()
    return this
}
