package com.github.minimi.uiState

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * Class for observing liveData with generic type of [UiState]<[T]> and provide callbacks for different states
 *
 * @param owner The LifecycleOwner which controls the observer
 * @param uiStateLiveData [LiveData] with generic type of [UiState]<[T]>
 */
class UiStateContext<T>(
    private val owner: LifecycleOwner,
    uiStateLiveData: LiveData<UiState<T>>,
) {

    /** Refers to block of code responsible for success event handling */
    private lateinit var inCaseOfSuccessBlock: (data: T) -> Unit

    /** Refers to block of code responsible for error event handling */
    private lateinit var inCaseOfErrorBlock: (t: Throwable) -> Unit

    /** Refers to block of code responsible for loading event handling */
    private lateinit var whileLoadingBlock: () -> Unit


    /**
     * Function to set the lambda for handling success event.
     *
     * @param block The block of code called on success
     */
    fun success(block: (data: T) -> Unit) {
        inCaseOfSuccessBlock = block
    }

    /**
     * Function to set the lambda for handling error event.
     *
     * @param block The block of code called on error
     */
    fun error(block: (Throwable) -> Unit) {
        inCaseOfErrorBlock = block
    }

    /**
     * Function to set the lambda for handling loading event.
     * @param block The block of code called on loading
     */
    fun loading(block: () -> Unit) {
        whileLoadingBlock = block
    }


    /**
     * Observer for wrapped liveData passed in constructor
     */
    private val observer = Observer<UiState<T>> { uiState ->
        when (uiState) {
            is UiState.Loading -> loadingHandler()
            is UiState.Error -> errorHandler(uiState.cause)
            is UiState.Success -> successHandler(uiState.data)
        }
    }

    init {
        uiStateLiveData.observe(owner, observer)
    }

    /**
     * [UiState.Loading] state handler
     */
    private fun loadingHandler() {
        if (this::whileLoadingBlock.isInitialized) {
            whileLoadingBlock.invoke()
        }
    }

    /**
     * [UiState.Error] state handler
     */
    private fun errorHandler(t: Throwable) {
        if (this::inCaseOfErrorBlock.isInitialized) {
            inCaseOfErrorBlock.invoke(t)
        }
    }

    /**
     * [UiState.Success] state handler
     */
    private fun successHandler(data: T) {
        if (this::inCaseOfSuccessBlock.isInitialized) {
            inCaseOfSuccessBlock.invoke(data)
        }
    }
}