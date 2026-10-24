# DayOffset 📅

**DayOffset** is an intuitive, modern Android app built with **Jetpack Compose** and **Material 3** that allows you to calculate target dates by moving a start date forward or backward by days, weeks, months, or years.

---

## ✨ Features

- ⏱ **Flexible Date Calculations**: Add or subtract days, weeks, months, or years from any date with instant results.
- ⚡ **Quick Presets**: One-tap quick offset buttons (e.g., `+1 Day`, `+7 Days`, `+30 Days`, `+1 Month`, `-1 Week`, `-1 Year`, etc.).
- 🗓 **Date Navigation**: Quick navigation controls to shift dates by days or weeks, or jump back to **Today**.
- 📊 **Detailed Date Information**:
  - Day of week & Day of year
  - Week of year (Standard & ISO)
  - Leap year indicator
  - Smart end-of-month clamping detection
- 📜 **Calculation History**: Automatically saves your last 10 calculations with quick-restore and single-tap delete functionality.
- ⚙️ **Extensive Customization**:
  - Theme options: **Light**, **Dark**, or **System Default** with **Material You Dynamic Colors**.
  - Multiple date format displays (`d MMMM yyyy`, `MMM d, yyyy`, `dd/MM/yyyy`, `yyyy-MM-dd`).
  - Customizable metadata cards (toggle Weekday, Week Number, ISO Week, Day of Year, Leap Year displays).
  - Configurable default offset unit and direction.
  - Option to restore the last used start date on app restart.
- 📋 **Easy Export & Sharing**: One-tap copying of result date or complete calculation summary, with native Android sharing support.

---

## 🛠 Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3 Design](https://m3.material.io/)
- **Navigation**: [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **Architecture**: Clean Architecture & MVVM (Model-ViewModel-StateFlow pattern)
- **State Management**: `StateFlow` & `collectAsStateWithLifecycle`
- **Persistence**: `SharedPreferences` for user settings and calculation history
- **Target SDK**: Android 16 (API 37) | **Min SDK**: Android 8.0 (API 26)
- **Build System**: Gradle Version Catalogs (`libs.versions.toml`)

---

## 📁 Project Structure

```
com.example.dayoffset/
├── calculator/      # Core date offset calculation logic
├── data/            # Local storage implementation (SharedPreferences)
├── domain/          # Validation and core domain business logic
├── formatter/       # Localized date and text formatting logic
├── history/         # History management and serialization
├── model/           # Data models, UI state, and configuration Enums
├── navigation/      # NavGraph and top-level navigation bar setup
├── repository/      # Repository layer managing settings and history persistence
├── ui/              # Compose screens and ViewModel
│   ├── about/       # About screen
│   ├── history/     # Calculation history screen
│   ├── home/        # Main date offset calculation screen
│   ├── settings/    # Application settings screen
│   ├── splash/      # Animated splash screen
│   └── theme/       # Material 3 typography, color palette, and theme
└── util/            # Helpers for clipboard and native intent actions
```

---

## 🚀 Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (Ladybug or newer recommended)
- JDK 11 or higher
- Android SDK 37 (or configure compatible SDK versions)

### Building the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/DayOffset.git
   cd DayOffset
   ```

2. Open the project in **Android Studio**.

3. Sync Gradle and build the project:
   ```bash
   ./gradlew assembleDebug
   ```

4. Run unit tests:
   ```bash
   ./gradlew test
   ```

---

## 📱 Screenshots & UI Highlights

| Home / Calculator | Presets & Details | Settings |
| :---: | :---: | :---: |
| Clean input with live output | Quick offset presets & metadata | Theme, date format & display preferences |

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
