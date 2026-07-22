# 📱 FenLab Android

Android client for **FenLab** — a platform where students can discover, share, and interact with science experiments.

> Built with Kotlin & Jetpack Compose · MVVM · Hilt · Retrofit

---

## ✨ Features

- 🔐 Register & login with JWT authentication
- 🏠 Explore experiments with advanced filtering and sorting
- 🔍 Search experiments by keyword
- 📋 View step-by-step experiment instructions, materials, and media
- ➕ Add new experiments with photos and videos
- ⭐ Rate and comment on experiments
- ❤️ Save experiments to favorites
- 📄 Download experiments as PDF
- 🔔 Notification support
- 👤 User profile with personal experiment history

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Repository Pattern |
| DI | Hilt |
| Networking | Retrofit 2 + OkHttp |
| Async | Coroutines + Flow |
| Local Storage | DataStore Preferences |
| Image Loading | Coil |
| Animations | Lottie |
| Navigation | Navigation Compose |

---

## 📂 Project Structure

```
app/src/main/java/com/nursena/fenlab_android/
│
├── core/
│   ├── Constants.kt          # Base URL, server config
│   └── network/              # RetrofitClient, AuthInterceptor
│
├── data/
│   ├── remote/
│   │   ├── api/              # Retrofit interfaces
│   │   └── dto/              # Request & Response DTOs
│   └── repository/           # Repository implementations
│
├── domain/
│   ├── model/                # Domain models & enums
│   └── repository/           # Repository interfaces
│
└── ui/
    ├── screens/
    │   ├── auth/             # Login & Register
    │   ├── home/             # Experiment feed
    │   ├── search/           # Search
    │   ├── detail/           # Experiment detail
    │   ├── add/              # Add experiment
    │   ├── favorites/        # Favorites
    │   ├── profile/          # User profile
    │   └── splash/           # Splash screen
    ├── components/           # Reusable UI components
    ├── navigation/           # NavGraph
    └── theme/                # Colors, typography, theme
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 26+
- A running instance of [fenlab-backend](https://github.com/username/fenlab-backend)

### Setup

1. Clone the repository:
```bash
git clone https://github.com/username/fenlab-android.git
cd fenlab-android
```

2. Set your backend server IP in `app/src/main/java/com/nursena/fenlab_android/core/Constants.kt`:

```kotlin
object Constants {
    const val SERVER_IP = "10.0.2.2"        // Android Emulator
    // const val SERVER_IP = "192.168.x.x"  // Physical device (local network)
    // const val SERVER_IP = "x.x.x.x"      // Production server IP

    const val BASE_URL  = "http://$SERVER_IP:8080/"
    const val MINIO_URL = "http://$SERVER_IP:9000"
}
```

3. Open the project in Android Studio and run it on an emulator or device.

---

## 🔌 API Overview

The app communicates with the backend via REST API. Key interfaces:

| API | Endpoints |
|-----|-----------|
| `AuthApi` | `POST /api/auth/register`, `POST /api/auth/login` |
| `ExperimentApi` | CRUD + filter + pagination |
| `CommentApi` | Get, add, update, delete comments |
| `RatingApi` | Rate experiments |
| `FavoriteApi` | Add/remove favorites |
| `FileUploadApi` | Upload images/videos to MinIO |
| `PdfApi` | Download experiment as PDF |
| `NotificationApi` | Fetch user notifications |
| `UserApi` | Get/update user profile |
| `QuestionApi` | Q&A on experiments |

All authenticated requests include a `Bearer` JWT token via `AuthInterceptor`.

---

## 📸 Screens

```
Splash → Auth (Login/Register) → Main
                                   ├── Home      (feed, filter, sort)
                                   ├── Search    (keyword search)
                                   ├── Add       (create experiment)
                                   ├── Favorites (saved experiments)
                                   └── Profile   (user info & experiments)
                                         └── Detail (steps, materials, media, comments)
```

---

## 🗂️ Experiment Filters

| Filter | Options |
|--------|---------|
| Subject | `SCIENCE`, `PHYSICS`, `CHEMISTRY`, `BIOLOGY`, `MATH`, `OTHER` |
| Difficulty | `EASY`, `MEDIUM`, `HARD` |
| Environment | `HOME`, `LABORATORY`, `CLASSROOM`, `OUTDOOR` |
| Grade Level | Min / Max grade (numeric range) |
| Sort | `MOST_RECENT`, `MOST_POPULAR`, `HIGHEST_RATED` |

---
## Demo Video
[![Fenlab Demo](https://img.youtube.com/vi/o-wnNHWU20A/0.jpg)](https://youtube.com/shorts/o-wnNHWU20A)

---

## 👩‍💻 Developer

**Nur Sena Sevilmiş**  
Computer Engineering — Aydın Adnan Menderes University
