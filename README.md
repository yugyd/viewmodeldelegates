View Model Delegates
====================

Architecture for Android applications in Kotlin using the MVVM pattern.**

- **Delegation-friendly**: solves the problem of oversized ViewModels
- **Structured**, uses the UDF (Unidirectional Data Flow) approach
- **Performant**: most of the code runs on the JVM without Android dependencies, and modules build
  faster
- **Testable and predictable**: everything is built around State and the JVM, with minimal Android
  dependencies
- **Modern**, Jetpack Compose–friendly
- **Simple**: minimal code required for implementation and a basic tech stack

## Stack

- Language: Kotlin
- Threading: Coroutines + Flow
- Android X: Android lifecycle ViewModel

# Quick Start (Step-by-step guide)

### Step 1 — Add dependencies (Gradle)

Groovy DSL:

```gradle
dependencies {
    implementation platform('com.yugyd.viewmodeldelegates:viewmodeldelegates-bom:{latest_version}')
    implementation 'com.yugyd.viewmodeldelegates:viewmodeldelegates'
    implementation 'com.yugyd.viewmodeldelegates:viewmodeldelegates-ui'
}
```

Kotlin DSL:

```kotlin
dependencies {
    implementation(platform("com.yugyd.viewmodeldelegates:viewmodeldelegates-bom:{latest_version}"))
    implementation("com.yugyd.viewmodeldelegates:viewmodeldelegates")
    implementation("com.yugyd.viewmodeldelegates:viewmodeldelegates-ui")
}
```

**Why:** the library is split conceptually into:

- **domain/runtime** part (event routing + state store),
- **ui** part (binding/mapping to Compose-friendly model).

---

### Step 2 — Create `State`

Create a single immutable state for the screen.

```kotlin
data class State(
    val arguments: Arguments = Arguments(),
    val isLoading: Boolean = false,
    val isWarning: Boolean = false,
    val data: String = "",
    val navigationState: NavigationState? = null,
) {

    data class Arguments(val userName: String = "")

    sealed interface NavigationState {
        object NavigateToFavourites : NavigationState
    }
}
```

**Why:**

- Immutable state + `copy()` makes updates explicit and safe.
- `navigationState` and `showErrorMessage` model *one-time effects* (more on that later).

---

### Step 3 — Create `Event`

Define all inputs as a sealed interface/class:

```kotlin
sealed interface Event {
    object LoadData : Event
    object OnActionClicked : Event
    object OnSnackbarDismissed : Event
    object OnNavigationHandled : Event
}
```

**Why:** UI communicates only via events; no direct mutation, no “call random method” style API.

---

### Step 4 — Implement ViewModel logic using delegates

#### 4.1 Define the ViewModel contract

```kotlin
interface SampleViewModel : JvmViewModel<Event, State> {
    // Add State/Events here for encapsulation
}
```

In the sample, the contract embeds `Event` and `State` inside the interface; that’s a good practice
for feature encapsulation.

---

#### 4.2 Implement a typical use-case delegate

```kotlin
class OnNavigationHandledViewModelDelegate : SampleViewModelDelegate {

    override fun accept(
        event: Event,
        viewModel: ViewModelDelegates<Event, State>,
        scope: CoroutineScope,
        getState: () -> State
    ): Boolean {
        if (event != Event.OnNavigationHandled) return false
        viewModel.updateState { it.copy(navigationState = null) }
        return true
    }
}
```

```kotlin
class LoadDataViewModelDelegate(
    private val repository: SampleRepository,
) : SampleViewModelDelegate {

    override fun accept(
        event: Event,
        viewModel: ViewModelDelegates<Event, State>,
        scope: CoroutineScope,
        getState: () -> State
    ): Boolean {
        if (event != Event.LoadData) return false

        // 1) Update state 
        viewModel.updateState {
            it.copy(
                isLoading = true,
                isWarning = false,
                message = "",
                showErrorMessage = false,
            )
        }

        // 2) Run async work in the provided scope
        scope.launch {
            // Add your logic
        }

        return true
    }
}
```

**Why this structure is important:**

- All async work is tied to the ViewModel lifecycle via `scope`.
- State updates are explicit and isolated via `updateState { }`.
- Each delegate handles a single event (returns `true` if handled, `false` otherwise).
- Delegates are pure Kotlin classes (no Android dependencies).
- Delegates encapsulate ViewModel and UseCase/Interactor logic.
- To reuse logic or store local state (e.g., a Job), you can use SharedDelegates, which can be
  attached to different ViewModelDelegates. Ensure a single instance via DI.

---

#### 4.4 Assemble the ViewModel with a factory

The sample uses a builder function (can be replaced by DI framework):

```kotlin
fun buildSampleBinder(): SampleBinder {
    // ...
    val viewModel = object : SampleViewModel,
        JvmViewModel<Event, State> by DefaultViewModelFactory().create(
            initialState = State(arguments = arguments),
            viewModelDelegates = setOf(
                LoadDataViewModelDelegate(repository),
                OnActionClickedViewModelDelegate(),
                OnNavigationHandledViewModelDelegate(),
                OnSnackbarDismissedViewModelDelegate(),
            ),
            initEvents = setOf(Event.LoadData),
            logger = buildLogger(),
            name = "SampleViewModel",
        ) {}

    return SampleBinder(
        viewModel = viewModel,
        mapper = SampleMapper(),
    )
}
```

**Why:**

- `initEvents = setOf(Event.LoadData)` triggers initial loading automatically.
- Kotlin delegation `by factory.create(...)` avoids boilerplate while still exposing a typed
  `SampleViewModel` interface.
- Delegates are composed without inheritance.
- You can set `autoInit = false` and trigger init events manually if needed; this is also useful for
  mocks in tests.
- Logger can be customized or disabled (null) for production.
- ViewModel name is useful for logging.
- `DefaultViewModelFactory` can be wrapped in DI framework factories.

---

### Step 5 — Integrate with UI (Compose) via Binder + Mapper

#### 5.1 Create a UI Model (optional but recommended)

Sample `SampleBinder.Model`:

```kotlin
data class Model(
    val isLoading: Boolean = false,
    val isWarning: Boolean = false,
    val data: String = "",
    val navigationState: NavigationUiState? = null,
) {
    sealed interface NavigationUiState {
        object NavigateToFavourites : NavigationUiState
    }
}
```

**Why:**

- UI model can differ from domain state (formatting, UI flags, string resources, etc.).
- Use a wrapper for `NavigationState` annotated with `@Immutable` to eliminate mapping and make the
  code simpler.

---

#### 5.2 Map State → Model (optional but recommended)

```kotlin
class SampleMapper : StateToModelMapper<State, Model> {

    override fun map(state: State): Model {
        return Model(
            isLoading = state.isLoading,
            isWarning = state.isWarning,
            data = state.data,
            navigationState = when (state.navigationState) {
                State.NavigationState.NavigateToFavourites -> Model.NavigationUiState.NavigateToFavourites
                null -> null
            },
        )
    }
}
```

**Why:** mapping isolates UI from domain changes and keeps Compose code simple.

---

#### 5.3 Create a Binder (UI-facing ViewModel) на основе ModelViewModelBinder

```kotlin
class SampleBinder(
    private val viewModel: SampleViewModel,
    mapper: SampleMapper,
) : ModelViewModelBinder<Event, State, Model>(
    viewModel = viewModel,
    initialModel = Model(),
    stateToModelMapper = mapper,
) {

    fun onActionClicked() = viewModel.accept(Event.OnActionClicked)
    fun onSnackbarDismissed() = viewModel.accept(Event.OnSnackbarDismissed)
    fun onNavigationHandled() = viewModel.accept(Event.OnNavigationHandled)
}
```

**Why:**

- Binder exposes `model` as a stream for Compose.
- Binder is the single place where UI triggers events.

---

#### 5.4 Use in Compose

```kotlin
@Composable
fun SampleScreen(binder: SampleBinder) {
    val state by binder.model.collectAsStateWithLifecycle()
    // ...
}
```

#### 5.5 A simpler approach without a Mapper can be used (not recommended).

```kotlin
class SimpleHomeBinder(
    private val viewModel: HomeViewModel,
) : StateViewModelBinder<Event, State>(viewModel) {
    fun onEvent(event: Event) = viewModel.accept(event)
}
```

### Step 6 - Best practices

### Delegate design

- **One delegate = one responsibility** (loading, navigation, snackbar).
- Keep delegates **pure** (no Android Context, no UI references).
- Prefer repository/use-case injection into delegates (constructor DI).

### State & effects

- Keep `State` **immutable** and updated only via `copy`.
- Model one-time effects (navigation/snackbar) as nullable fields or consumable flags.
- Always add “handled” events to clear one-time effects.

### Event routing

- Ensure each `Event` is handled by **exactly one delegate**.
    - In the sample, delegates are passed as a `setOf(...)` (unordered).
    - If two delegates handle the same event, behavior can become ambiguous.
    - Prefer designing events so they have a single clear owner.

### Coroutines

- Use the provided `scope` for async operations (it is lifecycle-bound).
- Use `getState()` inside coroutines when you need **fresh** state values.

### UI mapping

- Use `StateToModelMapper` to keep Compose simple and stable.
- Put formatting/transformation logic in the mapper, not inside Composables.

---

# Overview: what problem it solves and why it helps in MVVM

### The problem

In a typical MVVM project, ViewModels tend to grow into “God objects”:

- a huge `when(event)` (or dozens of public methods),
- mixed concerns (loading, error handling, navigation, analytics, validation),
- hard-to-test logic due to tight coupling and large state mutation blocks,
- inconsistent patterns between features.

### What the library provides

**View Model Delegates** standardizes ViewModel logic as a **composition of small event handlers** (
“delegates”), while keeping:

- **single immutable State** (for rendering UI),
- **Events** (inputs from UI / lifecycle),
- **deterministic state updates** (`updateState { copy(...) }`),
- **structured concurrency** (delegates receive a `CoroutineScope`),
- optional **UI Binder** to map domain `State` → UI `Model`.

### Why it’s useful in MVVM

It enforces a predictable “unidirectional” flow:

`UI → Event → Delegate → State update → UI re-render`

and improves maintainability by making your ViewModel:

- **modularity** (each delegate handles a single responsibility.)
- **composable** (add/remove delegates),
- **testable** (test each delegate in isolation),

---

# Sample project

The sample project demonstrates the usage of the library in a simple screen with loading, warning,
data display, snackbar, and navigation.

## Stack

* Language: Kotlin
* Architecture: clean
* UI: Compose, Material 3
* Navigation: Jetpack Compose Navigation 3

# License

```
   Copyright 2025 Roman Likhachev

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
