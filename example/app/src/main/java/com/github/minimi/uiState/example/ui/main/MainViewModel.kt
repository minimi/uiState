package com.github.minimi.uiState.example.ui.main

import androidx.lifecycle.*
import com.github.minimi.uiState.UiState
import com.github.minimi.uiState.uiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _someExampleState by lazy { MediatorLiveData<UiState<String>>() }
    val someExampleState: LiveData<UiState<String>> = _someExampleState

    private val _someAnotherExampleState by lazy { MediatorLiveData<UiState<String>>() }
    val someAnotherExampleState: LiveData<UiState<String>> = _someAnotherExampleState


    fun giveMeExample() {
        _someExampleState.value = UiState.Loading
        viewModelScope.launch {
            delay(1500)

            if((1..5).random() > 3) {
                _someExampleState.postValue(UiState.Error(IllegalStateException("Exception example")))
            } else {
                _someExampleState.postValue(UiState.Success("Hello from example"))
            }
        }
    }

    fun giveMeAnotherExample() {
        _someAnotherExampleState.value = UiState.Loading
        viewModelScope.launch {
            delay(1000)

            repeat(10) {
                delay(1000) // simulate some heavy task
                _someAnotherExampleState.postValue(UiState.Success("Data received: $it"))
            }

            _someAnotherExampleState.postValue(UiState.Error(RuntimeException("No More Data")))
        }
    }
}