# Focus Launcher

**Android Launcher that helps you not get distracted.**

A clean, minimal Android home screen launcher built from scratch with **Kotlin** and **Jetpack Compose**. Designed with a pure black background and distraction-free interface to help you maintain focus.

🌐 **[Visit the Website ](https://brandonsr.github.io/FocusLauncher/)**

---

## Features

* 🏠 **Home screen** — Completely redesigned to feature a custom Clock and Music Player widget alongside your pinned apps.
* 🔍 **App drawer** — Swipe horizontally from the home screen to reveal a full-screen drawer with real-time search.
* ✨ **Animations** — Smooth slide-from-left animation when opening and closing the app drawer.
* 📌 **Pinned apps** — Long-press apps in the drawer to pin them to the home screen. Includes persistence across reboots and a clean indicator dot for pinned status.
* ⚫ **Minimalist aesthetic** — Sleek, pure black background for a distraction-free, battery-saving focus.
* 🔄 **Auto-refresh** — App list updates automatically when apps are installed, removed, or changed.
* ⬅️ **Back button suppressed** — Proper launcher behavior: back press does nothing on the home screen.
* 📱 **Edge-to-edge UI** — Full-screen immersive layout.

---

## Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM (ViewModel + StateFlow) |
| **Async** | Kotlin Coroutines (viewModelScope + Dispatchers.IO) |
| **IDE** | Android Studio |
| **Build** | Gradle (Kotlin DSL) |

---

## Project Structure

```text
app/src/main/java/com/example/focuslauncher/
├── MainActivity.kt          # Entry point; sets up immersive UI and back handler
├── HomeScreen.kt            # Root composable; manages drawer state + horizontal swipe gesture
├── HomeScreenContent.kt     # Clock, music player widget, and pinned apps row
├── AppDrawerScreen.kt       # Animated drawer (slides from left) with search bar and app grid
├── AppViewModel.kt          # MVVM ViewModel; loads apps, manages StateFlow and pinned persistence
├── AppUtils.kt              # PackageManager helpers: getInstalledApps, launchApp
├── AppInfo.kt               # Data class: label, packageName, icon, isPinned status
└── PackageReceiver.kt       # BroadcastReceiver for package install/remove events
Setup
Requirements
Android Studio (latest stable)

JDK 21

Android device or emulator running API 24+

Run
Clone the repository:

Bash
git clone [https://github.com/brandonsr/FocusLauncher.git](https://github.com/brandonsr/FocusLauncher.git)
Open the project in Android Studio.

Connect a physical device (recommended — emulators don't fully support launcher testing).

Click Run ▶

When prompted, set Focus Launcher as your default home app.

Architecture Overview
Plaintext
MainActivity
    └── HomeScreen (Compose)
            ├── HorizontalSwipeHandler  ← detects left/right swipe to slide drawer
            ├── HomeScreenContent       ← Clock, Music Player, and Pinned Apps
            └── AppDrawerScreen         ← animated slide-in from left when shown
                    ├── SearchBar
                    └── LazyVerticalGrid (filtered apps)

AppViewModel (AndroidViewModel)
    ├── StateFlow<List<AppInfo>>   ← observed by HomeScreen (includes pinned state)
    ├── loadApps()                 ← runs on Dispatchers.IO
    ├── togglePinState()           ← manages pinning/unpinning and persistence
    └── PackageReceiver            ← triggers reload on package changes
Manifest Configuration
The launcher registers itself as a home screen via:

XML
<activity android:launchMode="singleTask" android:stateNotNeeded="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.HOME" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
Package visibility on API 30+ is handled via a <queries> block — no QUERY_ALL_PACKAGES permission required.

Roadmap
[x] Horizontal swipe gesture for app drawer

[x] Open/close drawer animation

[x] Pinned / favorite apps row on the home screen

[x] Clock and music player widgets

[ ] Weather widget integration on the home screen

[ ] Configurable grid column count in the app drawer

[ ] Adjustable icon size settings

License
MIT