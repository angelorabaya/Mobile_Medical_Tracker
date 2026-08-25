# 🏥 VitalsIQ — Intelligent Mobile Health & Medical Tracker

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-757575.svg)](https://m3.material.io/)
[![Room Database](https://img.shields.io/badge/Storage-Room%20DB-FFA000.svg)](https://developer.android.com/training/data-storage/room)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**VitalsIQ** is a modern, private, local-first Android application designed to help patients and caregivers systematically track, manage, and review their comprehensive healthcare records. From itemized lab test panels with real-time reference range evaluations and comparative trend analysis to multi-drug prescription schedules, high-resolution document scans, and daily medicine alarms, VitalsIQ ensures critical health data is organized, secure, and accessible at your fingertips.

---

## ✨ Key Features

### 📊 Comparative Lab Panel Review
- **Chronological Shift Tracking**: Automatically analyzes recent vs. previous lab findings for each test description over time.
- **Elevated vs. Decreased Trend Analytics**: Computes numeric deltas and status shifts (e.g. `▲ Elevated (+15 mg/dL)`, `▼ Decreased (-25 mg/dL)`, `— Stable`).
- **Health Dashboard Integration**: Collapsible panel on the home screen with search filters (`Elevated`, `Decreased`, `Stable`, `Baseline`) and historical finding timelines.

### 🧪 Advanced Lab Test Management
- **Single & Multi-Test Panels**: Record lab visits containing multiple itemized procedures under a single order (e.g. *Lipid Profile* broken down into *Total Cholesterol*, *Triglycerides*, *HDL*, *LDL*; *Complete Blood Count (CBC)*; *Liver Function Panels*; *Thyroid Profiles*).
- **Automated Clinical Reference Range Evaluation**:
  - Automatically evaluates findings against standard reference ranges.
  - Supports range bounds (`13.5 - 17.5 g/dL`, `70 - 99 mg/dL`), upper limits (`< 200 mg/dL`, `< 150 mg/dL`), lower limits (`> 40 mg/dL`), and qualitative results (*Negative*, *Clear*, *Non-reactive* vs *Positive*, *Elevated*).
  - Highlights results dynamically (**Green** for normal, **Red** with warning badges for out-of-range/abnormal values).
- **Collapsible & Expandable Cards**:
  - All test items start cleanly collapsed by default for quick browsing.
  - Smooth animated expansion (`AnimatedVisibility`) reveals full results, reference ranges, and doctor notes.
  - One-tap global **"Expand All" / "Collapse All"** toggle.
- **Customizable Standard Test Types**: Search autocomplete library of standard clinical tests with customizable categories and default reference ranges. Add custom test procedures on-the-fly with a single tap.
- **Ergonomic Bottom Add Buttons**: Seamlessly add more test procedures directly at the bottom of the list without needing to scroll back up.

### 💊 Multi-Medication Prescriptions
- **Multi-Drug Orders**: Manage complete prescription slips issued by doctors with multiple medicines per visit.
- **3-Box Daily Dosage Schedule**: Intuitive Morning, Noon, and Night dosage inputs (e.g., `1 - 0 - 1` tab).
- **Duration & Instructions**: Track course durations, special intake directions (e.g., *Take after meals*), and doctor notes.
- **Status Lifecycle**: Toggle active prescriptions or mark completed treatment courses.

### ⏰ Exact Medicine Reminders & Alarms
- **Scheduled Dose Notifications**: Set exact alarm times for each prescribed medicine.
- **Device Reboot Resilience**: Background `BootReceiver` automatically reschedules active alarms after phone restarts.
- **Quick Alarm Toggles**: Enable, disable, or delete daily medication reminders with a single tap.

### 📄 Document Scanner & Zoomable Photo Viewer
- **Attachment Storage**: Capture or import photos of lab report printouts, doctor prescription slips, and medication boxes.
- **Interactive Zoom & Pan**: High-resolution zoomable viewer with pinch-to-zoom and pan gestures to inspect small text and diagnostic charts.

### 👤 Patient Medical Profile
- **Personal & Emergency Record**: Store blood type, date of birth, gender, known drug/food allergies, and emergency contact details.
- **Health Dashboard**: Summary metric cards on the home screen for quick health status overview.

---

## 🎨 UI & Aesthetics
- **Material 3 Design System**: Clean, modern typography and soothing healthcare teal/mint visual identity.
- **Specialized Medical Icons**: Intuitive medical iconography across all categories (`Bloodtype`, `MonitorHeart`, `Biotech`, `PermMedia`, `WaterDrop`, `Air`, `DeviceThermostat`, `Psychology`, `PostAdd`, `EditNote`, `Rule`, `StickyNote2`).
- **Dynamic Color Feedback**: Clear visual badges for active/completed treatments and normal/abnormal clinical findings.

---

## 🛠️ Architecture & Tech Stack

```
com.example.medtrack
├── data
│   ├── dao          # Room DAOs (LabTestDao, PrescriptionDao, PatientDao, etc.)
│   ├── entity       # SQLite Entities & 1-to-many Relation Data Classes
│   └── DataRepository.kt
├── notification     # AlarmManager, ReminderReceiver, BootReceiver, NotificationHelper
├── theme            # Material 3 Color Schemes, Typography, Shapes
├── ui
│   ├── components   # Reusable UI widgets (Badges, Zoom Dialog, Dosage Schedule)
│   ├── home         # Health Dashboard & Patient Hero Card
│   ├── labtest      # List, Add, Detail, and Test Type Maintenance Screens
│   ├── prescription # List, Add, and Detail Prescription Screens
│   ├── profile      # Patient Profile & Registration Screens
│   └── reminder     # Medicine Alarm Management
└── util             # LabResultEvaluator, FrequencyHelper, ImageUtils
```

- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture Pattern**: MVVM (Model-View-ViewModel) + Clean Architecture with Repository pattern
- **Database**: Room Database (SQLite, 100% offline, local-first privacy)
- **Async & Reactive**: Kotlin Coroutines & `StateFlow` / `SharedFlow`
- **Image Loading**: Coil Compose
- **System Alarms**: Android `AlarmManager` with `USE_EXACT_ALARM` & `POST_NOTIFICATIONS`

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2+) or newer
- JDK 17
- Android SDK 35 (minSdkVersion: 26, targetSdkVersion: 35)
- Android device or emulator running Android 8.0 (API 26) or higher

### Build & Run
1. **Clone the repository**:
   ```bash
   git clone https://github.com/angelorabaya/Mobile_Medical_Tracker.git
   cd Mobile_Medical_Tracker
   ```

2. **Open in Android Studio**:
   - Open the project directory in Android Studio.
   - Allow Gradle to sync dependencies.

3. **Build the Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run Unit Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

5. **Install on connected device via ADB**:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 🧪 Unit Testing

VitalsIQ includes comprehensive unit tests for core business logic, such as the clinical reference range evaluator and comparative panel trend analytics:
```bash
./gradlew testDebugUnitTest --tests "com.example.medtrack.util.LabResultEvaluatorTest"
```

Verified test cases include:
- Numeric range boundaries (`Min - Max`)
- Upper limit thresholds (`< Limit`, `<= Limit`)
- Lower limit thresholds (`> Limit`, `>= Limit`)
- Qualitative text results (*Negative*, *Clear*, *Non-reactive* vs *Positive*, *Abnormal*)
- Decimal, integer, and unit-suffixed clinical inputs

---

## 🔒 Privacy & Security

- **100% Local-First**: All patient profiles, prescriptions, lab results, and document photos are stored strictly on the user's physical device inside protected SQLite database storage and app-private directory.
- **No Cloud Tracking**: No external telemetry, tracking, or mandatory internet connectivity required.

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.
