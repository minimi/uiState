package com.github.minimi.uiState

import kotlin.test.Test
import kotlin.test.fail

class UiStateTest {

    @Test
    @Suppress("SENSELESS_COMPARISON")
    fun checkSuccessState() {
        val state: UiState<String> = UiState.Success("SomeData")
        assert(state is UiState.Success) { "Must be Success type if success" }
        assert(state.getOrNull() == "SomeData") { "getOrNull() must return data on Success" }
        assert(state.exceptionOrNull() == null) { "exceptionOrNull() must not return" }
        assert(state.isSuccess) { "Wrong return value for isSuccess when state is Success" }
        assert((state as UiState.Success).data != null) { "Must return data when state is Success" }
        state.onSuccess { assert(it == "SomeData") { "Must be Success type if success" } }
    }

    @Test
    @Suppress("SENSELESS_COMPARISON")
    fun checkErrorState() {
        val state: UiState<String> = UiState.Error(Exception("Some Message"))
        assert(state is UiState.Error) { "Must be Error type on error!" }
        assert(state.getOrNull() == null) { "getOrNull() function must return null on error!" }
        assert(state.exceptionOrNull() is Throwable && state.exceptionOrNull()!!.message == "Some Message")
        assert(state.isFailed) { "Wrong return value for isFailed" }
        assert((state as UiState.Error).cause != null) { "Must return cause on error!" }
        state.onFailure { assert(it.message == "Some Message") { "Must return exception" } }
    }

    @Test
    fun checkLoadingState() {
        val state: UiState<String> = UiState.Loading
        assert(state is UiState.Loading) { "Must be Loading type when loading!" }
        assert(state.getOrNull() == null) { "getOrNull() function must return null on loading!" }
        assert(state.exceptionOrNull() == null) { "exceptionOrNull() must not return" }
        assert(state.isLoading) { "Wrong return value for isLoading" }
        state.onLoading { assert(true) { "onLoading must be called" } }
    }


    @Test
    fun map() {
        val state = UiState.Success("SomeData")
        val result = state.map { uiState ->
            val newData = "${uiState.getOrNull()}New"
            UiState.Success(newData)
        }
        assert(result.getOrNull() == "SomeDataNew") { "Cannot convert with map" }
    }

    @Test
    fun flatMap() {
        val de = UiState.Success("SomeData")
        val result = de.flatMap { "${it}New" }
        assert(result.getOrNull() == "SomeDataNew") { "Cannot convert with flatMap" }
        assert((result as UiState.Success).data == "SomeDataNew") { "Cannot convert with flatMap" }
    }

    @Test
    fun `test call chain in case of success`() {
        val state: UiState<String> = UiState.Success("SomeData")
        state.onLoading {
            fail("onLoading method must not be called when state is Success")
        }.onFailure {
            fail("onFailure method must not be called when state is Success")
        }.onSuccess {
            assert(it == "SomeData") { "onSuccess method must return data when state is Success" }
        }
    }

    @Test
    fun `test call chain in case of failure`() {
        val state = UiState.Error(IllegalStateException("Some text"))
        state.onLoading {
            fail("onLoading method must not be called when state is Error")
        }.onFailure {
            assert(it is IllegalStateException) { "onFailure method must return exception when state is Error" }
        }.onSuccess {
            fail("onSuccess method must not be called when state is Error")
        }
    }

    @Test
    fun `test call chain in case of loading`() {
        val state = UiState.Loading
        state.onLoading {
            assert(state.isLoading)
        }.onFailure {
            fail("onLoading method must not be called when state is Loading")
        }.onSuccess {
            fail("onSuccess method must not be called when state is Loading")
        }
    }
}