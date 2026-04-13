# Focus Launcher

A clean, minimal Android home screen launcher built from scratch with **Kotlin** and **Jetpack Compose**.

---

## Features

- 🏠 **Home screen** — displays all installed apps in a 4-column alphabetical grid
- 🔍 **App drawer** — swipe up from the home screen to reveal a full-screen drawer with real-time search
- 🖼️ **Wallpaper support** — system wallpaper shows through the launcher background
- 🔄 **Auto-refresh** — app list updates automatically when apps are installed, removed, or changed
- ⬅️ **Back button suppressed** — proper launcher behavior: back press does nothing on the home screen
- 📱 **Edge-to-edge UI** — full-screen immersive layout

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Async | Kotlin Coroutines (viewModelScope + Dispatchers.IO) |
| IDE | Android Studio |
| Build | Gradle (Kotlin DSL) |

---

## Project Structure

```
app/src/main/java/com/example/focuslauncher/
├── MainActivity.kt          # Entry point; sets up wallpaper flag and back handler
├── HomeScreen.kt            # Root composable; manages drawer state + swipe-up gesture
├── HomeScreenContent.kt     # 4-column app grid for the home screen
├── AppDrawerScreen.kt       # Full-screen drawer with search bar and app grid
├── AppViewModel.kt          # MVVM ViewModel; loads apps, manages StateFlow
├── AppUtils.kt              # PackageManager helpers: getInstalledApps, launchApp
├── AppInfo.kt               # Data class: label, packageName, icon
└── PackageReceiver.kt       # BroadcastReceiver for package install/remove events
```

---

## Setup

### Requirements

- Android Studio (latest stable)
- JDK 21
- Android device or emulator running API 24+

### Run

1. Clone the repository
2. Open the project in Android Studio
3. Connect a physical device (recommended — emulators don't fully support launcher testing)
4. Click **Run ▶**
5. When prompted, set **Focus Launcher** as your default home app

---

## Architecture Overview

```
MainActivity
    └── HomeScreen (Compose)
            ├── NestedScrollConnection  ← detects swipe-up to open drawer
            ├── HomeScreenContent       ← LazyVerticalGrid of app icons
            └── AppDrawerScreen         ← shown when showDrawer == true
                    ├── SearchBar
                    └── LazyVerticalGrid (filtered apps)

AppViewModel (AndroidViewModel)
    ├── StateFlow<List<AppInfo>>   ← observed by HomeScreen
    ├── loadApps()                 ← runs on Dispatchers.IO
    └── PackageReceiver            ← triggers reload on package changes
```

---

## Manifest Configuration

The launcher registers itself as a home screen via:

```xml
<activity android:launchMode="singleTask" android:stateNotNeeded="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.HOME" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

Package visibility on API 30+ is handled via a `<queries>` block — no `QUERY_ALL_PACKAGES` permission required.

---

## Roadmap

- [ ] Swipe-down gesture to close the app drawer
- [ ] Open/close drawer animation
- [ ] Pinned / favorite apps row on the home screen
- [ ] Configurable grid column count
- [ ] Adjustable icon size
- [ ] Clock, date, and weather widget on the home screen

---

## License

MIT
