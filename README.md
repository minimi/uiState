# uiState

Simple class to work with UI state.

In most cases we use 3 type of states when working with UI - Loading, Success and Error.
This lib describe these states as a sealed class and provides extensions to work with it.

Any way you can you UiState class as standalone with your "not Android" apps. 
That is why the project is decoupled on two modules.

## Changelog

### Version 1.0
- Initial release

## Getting started

### Prerequisites
- JDK 11 or higher
- Kotlin 1.5 or higher
- Gradle
- You favorite IDE

## Quick start

```kotlin
//
// in your viewmodel
//
class MainViewModel : ViewModel() {
  private val _someExampleState by lazy { MediatorLiveData<UiState<String>>() }
  val someExampleState: LiveData<UiState<String>> = _someExampleState


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
}

//
// in your fragment
//

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
  super.onViewCreated(view, savedInstanceState)

  val button = view.findViewById<Button>(R.id.button)

  button.setOnClickListener {
    viewModel.giveMeExample()
  }

  // observe your state with convenient Kotlin DSL
  uiState(viewModel.someExampleState) {
    success {
      message.text = it
    }
    loading {
      message.text = "Loading..."
    }
    error {
      message.text = it.message
    }
  }
}

```

#### Internals
The lib provide you several ways to observe data.

As an extension for Fragment:

```kotlin
  // in fragment
  uiState(viewModel.someExampleState) {
    success {
      message.text = it
    }
    loading {
      message.text = "Loading..."
    }
    error {
      message.text = it.message
    }
  }
```

As an extension for LiveData:

```kotlin

viewModel.someLiveData.observeUiState(viewLifecycleOwner) {
  success { data ->
    // do smth on success
    message.text = data
  }
  loading {
    // do smth on loading
    message.text = "Loading..."
  }
  error { ex ->
    // do smth on error
    message.text = ex.message
  }
}
```

Or deal with it like Builder pattern:

```kotlin

viewModel.someAnotherExampleState.observe(viewLifecycleOwner) { uiState ->
    uiState.onSuccess { data ->
      // do on success
    }.onLoading {
      // do on loading
    }.onFailure { ex ->
      // do on failure
    }
}

```

or even like this:

```kotlin

// assume the uiState is UiState<String>
viewModel.someAnotherExampleState.observe(viewLifecycleOwner) { uiState ->
    uiState
        .onSuccess(::showData)
        .onLoading(::showLoader)
        .onFailure(::showErrorMessage)
}

fun showData(data: String) { /* TODO */ }
fun showLoader() { /* TODO */ }
fun showErrorMessage(ex: Throwable) { /* TODO */ }

```

# License
[Apache License](./LICENSE.txt)
