# StepRange

**StepRange** is a modern, feature-rich Android application built with **Jetpack Compose** and **Material 3** designed for calculating, generating, and analyzing stepped numerical ranges and sequences (arithmetic progressions). 

Whether you are working with mathematical sequences, engineering increments, financial projections, or planning stepped values, StepRange provides high-precision calculations, detailed statistical breakdowns, history management, and custom preferences in a clean, adaptive Material 3 UI.

---

## 🚀 Key Features

### 1. Advanced Range Generation & Stepping
- **Precise Inputs**: Define Start value, Increment/Step size, and End value with arbitrary-precision arithmetic (`BigDecimal`).
- **Direction Handling**: Automatically detects and handles increasing, decreasing, or constant ranges.
- **End Boundary Control**: Choose whether to include or exclude the exact end boundary.
- **Feasibility Validation**: Instantly validates whether the end value is reachable with the given step size and warns if generated values exceed performance limits.

### 2. Comprehensive Statistical Analysis
- **Core Statistics**: View Total Count, Sum, Average, Minimum, and Maximum values for any generated range.
- **Formula Breakdown**: Inspect mathematical formulas and step-by-step arithmetic transitions (`a_n = a_1 + (n - 1) * d`).
- **Indexed Lookup**: Instantly find the exact value at any specific position/index (e.g., 50th step).

### 3. Calculation History
- **Save & Review**: Automatically or manually save calculation results to local history.
- **Reload & Re-calculate**: Quickly restore past ranges back into the calculator with a single tap.
- **Search & Clear**: Filter through history items or manage stored calculations.

### 4. Customization & Settings
- **Material 3 Theming**: Full support for Light, Dark, and System default themes.
- **Dynamic Color**: Supports Android's Monet dynamic color theming on Android 12+.
- **Haptic Feedback**: Tactile haptic feedback on successful calculations and actions.
- **Calculation Precision**: Adjustable precision and rounding settings.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/) (100% Kotlin codebase)
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/)
- **Architecture**: MVVM (Model-View-ViewModel) with Unidirectional Data Flow (UDF) & StateFlow
- **Navigation**: Jetpack Navigation Compose
- **Math Engine**: Java `BigDecimal` for zero-loss high-precision decimal calculations
- **Testing**: JUnit, Kotlin Coroutines Test, and Compose UI Tests (`StepRangeCoverageTest`)

---

## 📱 Project Structure

```text
app/src/main/java/com/example/steprange/
├── data/
│   ├── model/         # Data classes (AppSettings, CalculationStep, StepRangeResult, etc.)
│   └── repository/    # Local storage & repositories (HistoryRepository, SettingsRepository)
├── domain/
│   └── math/          # Core calculation engine (StepRangeCalculator, Formatter, Parser, Validator)
├── ui/
│   ├── about/         # About / Info screen
│   ├── history/       # Saved calculations history screen
│   ├── home/          # Main calculator & range results screen (with modular result cards)
│   ├── navigation/    # Navigation graph & routes
│   ├── settings/      # App settings & preferences screen
│   ├── splash/        # Splash screen animation
│   └── theme/         # Material 3 color schemes, typography, and theme setup
└── util/              # Helpers (HapticFeedbackHelper, etc.)
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Koala or newer recommended)
- **Android SDK** (API level 26 minimum, API level 37 target/compile)
- **JDK 11** or higher

### Installation & Building
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/StepRange.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and run the app on an Android emulator or physical device.

---

## 🧪 Testing

Run unit tests and UI tests via Gradle:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
