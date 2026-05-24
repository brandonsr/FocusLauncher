# Focus Launcher

**An Android launcher designed to help you stay focused.**

A clean, minimal home screen launcher built from scratch with **Kotlin** and **Jetpack Compose**. Pure black background, distraction-free interface, and a built-in Focus Mode with a Pomodoro timer.

🌐 **[Visit the Website](https://brandonsr.github.io/FocusLauncher/)**

---

## Features

### Home Screen
- **Text-based pinned apps** — your pinned apps displayed as a clean centered list, no icons
- **Clock widget** — live time and date, thin font, centered
- **Music widget** — mini-player that reads your active media session; shows album art, track info, and playback controls
- **Swipe left** — opens the app drawer
- **Swipe right** — enters Focus Mode

### App Drawer
- **Text list** — all installed apps as a centered scrollable list, no icons
- **Real-time search** — filters as you type
- **Long-press to pin/unpin** — pins an app to the home screen; pinned apps show a small blue dot
- **Animated slide-in** from the left

### Focus Mode
- **Swipe right** from the home screen to enter
- **Pomodoro timer** — set any duration (1–180 min) before starting
- **Landscape layout** — automatically rotates to landscape while active
- **Left side** — countdown timer + End Session button
- **Right side** — music widget + mini calendar
- **Do Not Disturb** — activates DND when the session starts (requires one-time permission grant); restores normal mode when the session ends or the timer runs out
- **Mini calendar** — current month view with today highlighted

### General
- Wallpaper visible behind the launcher
- Back button suppressed (proper launcher behavior)
- App list auto-refreshes on install/uninstall
- Edge-to-edge immersive UI
- Persisted pinned apps across reboots

---

## Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM (ViewModel + StateFlow) |
| **Async** | Kotlin Coroutines (`viewModelScope` + `Dispatchers.IO`) |
| **Media** | `NotificationListenerService` + `MediaController` |
| **IDE** | Android Studio |
| **Build** | Gradle (Kotlin DSL) |

---

## Project Structure

```text
app/src/main/java/com/example/focuslauncher/
├── MainActivity.kt                 # Entry point; wallpaper flag, back handler, edge-to-edge
├── HomeScreen.kt                   # Root composable; gesture routing (drawer / focus mode)
├── HomeScreenContent.kt            # Clock, music widget, pinned apps text list
├── AppDrawerScreen.kt              # Text list drawer with search and pin indicators
├── FocusModeScreen.kt              # Pomodoro timer, landscape layout, DND, mini calendar
├── AppViewModel.kt                 # MVVM ViewModel; apps StateFlow, pin persistence, music controls
├── AppUtils.kt                     # getInstalledApps(), launchApp()
├── AppInfo.kt                      # Data class: label, packageName, icon
├── PackageReceiver.kt              # BroadcastReceiver; refreshes app list on package changes
├── ClockWidget.kt                  # Live clock composable (1-second tick)
├── MusicWidget.kt                  # Three-state mini-player composable
├── MusicRepository.kt              # Singleton StateFlow for playback state + MediaController
└── MusicNotificationListener.kt    # NotificationListenerService; extracts MediaSession token
```

---

## Setup

**Requirements**
- Android Studio (latest stable)
- JDK 21
- Android device or emulator running API 24+

**Run**
```bash
git clone https://github.com/brandonsr/FocusLauncher.git
```
1. Open the project in Android Studio
2. Connect a physical device (recommended — emulators don't fully support launcher behavior)
3. Click **Run ▶**
4. When prompted, set **Focus Launcher** as your default home app

**Optional permissions** (prompted in-app)
- **Notification Listener** — required for the music widget to read your active media session
- **Do Not Disturb Access** — required for Focus Mode to silence notifications during a session

---

## Gesture Reference

| Gesture | Action |
| :--- | :--- |
| Swipe left | Open app drawer |
| Swipe right (drawer open) | Close drawer |
| Swipe right (home) | Enter Focus Mode |
| Tap app (drawer) | Launch app |
| Long-press app (drawer) | Pin / unpin app |
| Tap app (home list) | Launch app |

---

## Architecture Overview
MainActivity
└── HomeScreen
├── Swipe left  → AppDrawerScreen
│       ├── SearchBar
│       └── LazyColumn (text list, long-press to pin)
├── Swipe right → FocusModeScreen
│       ├── Setup (duration input, DND prompt)
│       └── Active layout (landscape)
│               ├── Left:  Pomodoro countdown + End Session
│               └── Right: MusicWidget + MiniCalendar
└── HomeScreenContent
├── ClockWidget
├── MusicWidget
└── LazyColumn (pinned apps, text list)
AppViewModel
├── StateFlow<List<AppInfo>>     ← app list, loaded on Dispatchers.IO
├── StateFlow<List<String>>      ← pinned packages, persisted via SharedPreferences
├── StateFlow<MusicState?>       ← forwarded from MusicRepository singleton
└── PackageReceiver              ← triggers reload on package changes
MusicNotificationListener (NotificationListenerService)
└── MusicRepository (singleton StateFlow)
---

## Manifest Notes

- Launcher registered with `HOME` + `DEFAULT` categories; run config set to **Nothing**
- `android:configChanges="orientation|screenSize|..."` on `MainActivity` prevents Activity recreation when Focus Mode rotates to landscape
- `<queries>` block handles package visibility on API 30+ without `QUERY_ALL_PACKAGES`

---

## Roadmap

- [x] Swipe gesture for app drawer (left/right)
- [x] Animated drawer slide-in
- [x] Pinned apps on home screen
- [x] Clock and music widgets
- [x] Text-only app list (drawer + home screen)
- [x] Focus Mode (Pomodoro + landscape + DND + mini calendar)
- [ ] Weather widget on home screen
- [ ] Configurable grid/list settings
- [ ] Adjustable font size

---

## License

MIT