# WeatherSnap

A production-quality Android application that lets you search for real-time weather data, capture a photo through a custom CameraX camera interface, add notes, and save weather reports locally.

---

## Tech Stack

| Layer       | Technology                            |
|-------------|---------------------------------------|
| Language    | Kotlin                                |
| UI          | Jetpack Compose + Material 3          |
| Architecture| MVVM                                  |
| State       | StateFlow + sealed UiState classes    |
| DI          | Hilt                                  |
| Navigation  | Navigation Compose (with transitions) |
| Network     | Retrofit 2 + Gson + OkHttp Logging    |
| Database    | Room (IO dispatcher)                  |
| Camera      | CameraX (custom preview, no intent)   |
| Images      | Coil Compose                          |
| Async       | Coroutines                            |

---

## Screens

### 1. Weather Screen
- Search city by name with 500 ms debounce
- Suggestions appear after 3 characters via Open-Meteo Geocoding API
- Suggestion results are cached in memory using `ConcurrentHashMap`
- Selecting a suggestion fetches live weather from Open-Meteo Forecast API
- Displays: city name, temperature, condition, humidity, wind speed, pressure
- Distinct loading / success / error / idle states with `AnimatedContent` transitions
- "Create Report" and "Saved Reports" actions visible after weather loads

### 2. Create Report Screen
- Shows the frozen weather snapshot card (not re-fetched)
- Dashed placeholder before photo is captured, animated image preview after
- Original and compressed image sizes displayed in a badge card
- Notes text field (saved continuously to `SavedStateHandle`)
- Save button writes the report to Room DB, then navigates to Saved Reports
- Draft recovery: notes, image URI, and weather snapshot all survive device rotation and process death

### 3. Custom Camera Screen
- CameraX live preview — no device camera app intent is used
- Title "Custom Camera" visible in the overlay header
- Shutter button with a circular progress indicator during capture
- After capture, shows original vs compressed file sizes before confirming
- Close button returns to Create Report without capturing
- Raw capture file is deleted immediately after compression to prevent leaks

### 4. Saved Reports Screen
- Lists all reports from Room DB, newest first
- Each card shows: photo, city, temperature, condition, humidity, wind, pressure, notes, original size, compressed size, timestamp
- Empty state displayed with descriptive text when no reports exist
- Staggered entry animation on list items

---

## Project Structure

```
app/src/main/java/com/weathersnap/
├── WeatherSnapApp.kt                # @HiltAndroidApp Application
├── MainActivity.kt                  # @AndroidEntryPoint Activity
│
├── data/
│   ├── api/
│   │   └── WeatherApis.kt          # GeocodingApi + WeatherApi Retrofit interfaces
│   ├── local/
│   │   ├── AppDatabase.kt          # Room database
│   │   ├── ReportDao.kt            # DAO: insert + getAllReports (Flow)
│   │   └── ReportEntity.kt         # Entity + domain mappers
│   ├── model/
│   │   └── ApiModels.kt            # DTOs for Open-Meteo JSON responses
│   └── repository/
│       ├── WeatherRepositoryImpl.kt # Geocoding + weather with in-memory cache
│       └── ReportRepositoryImpl.kt  # Room CRUD on IO dispatcher
│
├── domain/
│   ├── model/
│   │   ├── Models.kt               # CitySuggestion, WeatherSnapshot
│   │   └── Report.kt               # Report domain model
│   └── repository/
│       └── Repositories.kt         # WeatherRepository + ReportRepository interfaces
│
├── di/
│   └── AppModule.kt                # Hilt module: Retrofit, Room, Repos
│
├── ui/
│   ├── theme/
│   │   └── Theme.kt                # Material 3 light/dark colour scheme + Typography
│   ├── navigation/
│   │   ├── NavRoutes.kt            # Screen sealed class
│   │   └── AppNavHost.kt           # NavHost with slide+fade transitions
│   ├── weather/
│   │   ├── WeatherScreen.kt        # Weather UI: AnimatedContent states, suggestion dropdown
│   │   └── WeatherViewModel.kt     # Debounced search + city selection
│   ├── create/
│   │   ├── CreateReportScreen.kt   # Report creation with animated image preview
│   │   └── CreateReportViewModel.kt# Draft management, image compression, save
│   ├── camera/
│   │   ├── CustomCameraScreen.kt   # CameraX preview + animated post-capture card
│   │   └── CameraViewModel.kt      # Camera binding, photo capture, cleanup
│   └── saved/
│       ├── SavedReportsScreen.kt   # Reports list with staggered animation
│       └── SavedReportsViewModel.kt# Room Flow mapped to UiState
│
└── utils/
    └── ImageCompressor.kt          # JPEG compression utility (80% quality)
```

---

## API

No API key is required.

| API        | Base URL                                      | Used for                    |
|------------|-----------------------------------------------|-----------------------------|
| Geocoding  | `https://geocoding-api.open-meteo.com/v1/`    | City search suggestions      |
| Weather    | `https://api.open-meteo.com/v1/`              | Current weather data         |

---

## Developer Judgment: Draft Recovery

**Problem**: The user selects weather, opens Create Report, captures a photo, types notes, then rotates the device or kills the app before saving. All in-progress data must survive without duplicating saved reports.

**Approach chosen: SavedStateHandle scoped to the back-stack entry**

| Approach                  | Pros                                                      | Cons                                              |
|---------------------------|-----------------------------------------------------------|---------------------------------------------------|
| SavedStateHandle (chosen) | Survives rotation + process death; auto-scoped to entry; no orphan cleanup needed | Lost on force-kill |
| Room draft entity          | Survives everything including force-kill                  | Requires explicit cleanup to avoid orphaned rows   |
| DataStore                 | Simple key-value persistence                              | Global scope; needs manual lifecycle tie-in       |

**Why SavedStateHandle**: The create-report flow is a single transient session. SavedStateHandle is automatically scoped to the back-stack entry, so the draft is discarded when the user exits the flow without saving — no orphan cleanup code required.

**What is persisted across rotation and process death**:
- Weather snapshot JSON — automatically available from the navigation argument key `snapshotJson`
- Notes text — written on every keystroke via `savedStateHandle["draft_notes"]`
- Compressed image URI — written after compression via `savedStateHandle["draft_image"]`

**Duplicate prevention**: After a successful save the draft keys are explicitly removed from `SavedStateHandle`. Re-entering the same back-stack entry (e.g. by pressing back on Saved Reports) will not show stale data because the ViewModel has already been cleared and the keys are gone.

**Temporary file cleanup**:
- The raw `.jpg` file saved by CameraX to `cacheDir` is deleted immediately after successful compression
- The compressed file in `filesDir` becomes the permanent report asset after saving
- If the user exits the create-report flow without saving, `ViewModel.onCleared()` deletes the compressed temp file (rotation does not trigger `onCleared`, so the file is safe during rotation)

---

## Build and Run

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17+
- Android device or emulator running API 26 or higher
- Internet connection for weather API calls
- Camera hardware (real device recommended for CameraX)

### Steps

1. Clone the repository
   ```bash
   git clone https://github.com/YOUR_USERNAME/WeatherSnap.git
   cd WeatherSnap
   ```

2. Open in Android Studio
   - File > Open > select the project root
   - Wait for Gradle sync to complete

3. Run on device or emulator
   - Select a device (API 26+)
   - Click Run or press Shift+F10

4. Build debug APK
   ```bash
   ./gradlew assembleDebug
   ```
   Output: `app/build/outputs/apk/debug/app-debug.apk`

5. Run unit tests
   ```bash
   ./gradlew test
   ```

---

## Network Logging

OkHttp logging is controlled by the `DEBUG_LOGGING` build-config field:
- Debug builds: full `BODY`-level logging enabled
- Release builds: logging disabled entirely

---

## Screen Recording Checklist

The recording should demonstrate:
1. Weather screen visible on launch
2. Type a city name — suggestions appear as a dropdown after 3 characters
3. Select a city — weather card loads with all 6 data fields
4. Tap Create Report — navigate to the create screen
5. Tap Capture Photo — custom camera screen opens (title "Custom Camera" visible)
6. Take a photo — returns to create screen with image preview
7. Show original vs compressed file size in the badge card
8. Type notes
9. Rotate the device — notes, photo, and weather all survive
10. Tap Save Report — navigates to Saved Reports
11. Saved report card visible with photo, weather details, notes, sizes, and timestamp
12. Navigate back to Weather screen and use the Bookmark icon to open Saved Reports again
