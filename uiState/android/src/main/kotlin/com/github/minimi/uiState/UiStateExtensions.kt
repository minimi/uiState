@file:Suppress("unused")

package com.github.minimi.uiState

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlin.experimental.ExperimentalTypeInference

typealias SuccessCallback<T> = (T) -> Unit
typealias ErrorCallback = (Throwable) -> Unit
typealias LoadingCallback = () -> Unit


/**
 * Function for observing [liveData] with [UiState] within the [context].
 *
 * @param owner lifecycle owner
 * @param liveData observed [LiveData]
 * @param context Context which will be used for observing [liveData]
 */
inline fun <reified T> uiState(
    owner: LifecycleOwner,
    liveData: LiveData<UiState<T>>,
    context: UiStateContext<T>.() -> Unit
): UiStateContext<T> {
    return UiStateContext(owner, liveData).apply {
        context()
    }
}

/**
 * Extension function for [LiveData] allow to set observer as callbacks for different [UiState] states
 *
 * @param owner Lifecycle owner
 * @param onSuccess Callback called in case of success. [T] passed as a parameter
 * @param onError Callback called in case of error with [Throwable] passed as a parameter
 * @param onLoading Loading callback. Called when data still loading
 */
fun <T> LiveData<UiState<T>>.observeUiState(
    owner: LifecycleOwner,
    onSuccess: SuccessCallback<T>?,
    onError: ErrorCallback? = null,
    onLoading: LoadingCallback? = null,
) {
    this.observe(owner) { uiState ->
        when (uiState) {
            is UiState.Loading -> {
                onLoading?.invoke()
            }
            is UiState.Error -> {
                onError?.invoke(uiState.cause)
            }
            is UiState.Success -> {
                onSuccess?.invoke(uiState.data)
            }
        }
    }
}

/**
 * Extension function for [LiveData] allow to set observer as callbacks for different [UiState] states
 *
 * @param owner Lifecycle owner
 * @param context Context which will be used for observing [liveData]
 */
fun <T> LiveData<UiState<T>>.observeUiState(
    owner: LifecycleOwner,
    context: UiStateContext<T>.() -> Unit
): UiStateContext<T> {
    return UiStateContext(
        owner = owner,
        uiStateLiveData = this
    ).apply {
        context()
    }
}


//fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
//    observe(
//        lifecycleOwner,
//        object : Observer<T> {
//            override fun onChanged(t: T) {
//                observer.onChanged(t)
//                removeObserver(this)
//            }
//        }
//    )
//}


/**
 * Extension for [Fragment] allow to observe [liveData] within given context.
 * [Fragment] in this case is lifecycle owner
 *
 *
 * Example:
 * ```
 * class ExampleFragment: Fragment() {
 *   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *     super.onViewCreated(view, savedInstanceState)
 *
 *     uiState(viewModel.awesomeLiveData) {
 *          success { data ->
 *              showContent(data)
 *          }
 *
 *          loading {
 *              showLoader();
 *          }
 *
 *          error { error ->
 *              showError("TAG", error.message)
 *          }
 *     }
 *
 * ```
 *
 * @param liveData [LiveData] which must be observed
 * @param context Context which will be used for observing [liveData]
 */
inline fun <T> Fragment.uiState(
    liveData: LiveData<UiState<T>>,
    context: UiStateContext<T>.() -> Unit
): UiStateContext<T> {
    return UiStateContext(this, liveData).apply {
        context()
    }
}


/**
 * Extension for [AppCompatActivity] allow to observe [liveData] within given context.
 * [AppCompatActivity] in this case is lifecycle owner
 *
 *
 * Example:
 * ```
 * class ExampleActivity: Activity() {
 *   override fun onCreate(savedInstanceState: Bundle?) {
 *     super.onCreate(savedInstanceState)
 *
 *     uiState(viewModel.awesomeLiveData) {
 *          success { data ->
 *              showContent(data)
 *          }
 *
 *          loading {
 *              showLoader();
 *          }
 *
 *          error { error ->
 *              showError("TAG", error.message)
 *          }
 *     }
 *
 * ```
 *
 * @param liveData [LiveData] which must be observed
 * @param context Context which will be used for observing [liveData]
 */
inline fun <T> AppCompatActivity.uiState(
    liveData: LiveData<UiState<T>>,
    context: UiStateContext<T>.() -> Unit
): UiStateContext<T> {
    return UiStateContext(this, liveData).apply {
        context()
    }
}

/**
 * Return [Flow] of [UiState].
 * Could be useful for http clients calls
 *
 * Example:
 * ```
 * val state = uiState {
 *      retrofitApi.getSomeData()
 * }
 *
 * state.collect { state ->
 *    state.onSuccess {
 *        // do on success
 *    }.onLoading {
 *        // do on loading
 *    ).onFailure {
 *        // do on error
 *    }
 * }
 * ```
 *
 * @param block of code, return  [T]
 * @return [Flow] of [UiState] with generic of [T]
 */
@OptIn(ExperimentalTypeInference::class)
suspend fun <T> uiState(@BuilderInference block: suspend () -> T) = flow {

    emit(UiState.Loading)

    try {
        val response = withContext(Dispatchers.IO) { block.invoke() }
        emit(UiState.Success(response))
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        emit(UiState.Error(e))
    }
}

/**
 * Transform [kotlin.Result] to [UiState]
 */
fun <T> Result<T>.toUiState(): UiState<T> {
    exceptionOrNull()?.let { error ->
        return@toUiState UiState.Error(error)
    }

    val data = this.getOrNull()

    return UiState.Success<T>(data!!)
}

