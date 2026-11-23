# GameHub — Unified PC Game Search, Price Comparison & Library Tracker

GameHub is an Android application built with **Jetpack Compose** and a **Python FastAPI** backend that enables gamers to search, compare, track, and manage PC games across **Steam** and the **Epic Games Store** with real-time **Indian Rupee (₹ INR)** pricing.

---

## Project Structure

```
Android/
├── backend/                  # Python FastAPI Backend Service
│   ├── app/
│   │   ├── main.py           # FastAPI entrypoint & CORS
│   │   ├── config.py         # App settings & URLs
│   │   ├── models/           # Pydantic schemas (Game, Stores, Offers, Library)
│   │   ├── services/         # Steam & Epic connectors, title normalizer, price comparator
│   │   └── routers/          # Search, details, prices, offers, library, accounts
│   ├── tests/                # Pytest test suite (100% passing)
│   ├── requirements.txt      # Backend dependencies
│   └── run_server.py         # Local server launcher
│
└── android/                  # Android Studio Project
    ├── app/
    │   ├── build.gradle.kts  # Dependencies: Jetpack Compose, Room, DataStore, Retrofit, Coil
    │   └── src/main/
    │       ├── AndroidManifest.xml
    │       └── java/com/gamehub/app/
    │           ├── MainActivity.kt          # Compose Single-Activity & Navigation Scaffold
    │           ├── data/
    │           │   ├── local/               # Room DB (Wishlist, Recent Searches) & DataStore
    │           │   ├── remote/              # Retrofit API Service & Pydantic-mirrored models
    │           │   └── repository/          # Unified GameHub Repository
    │           └── ui/
    │               ├── navigation/          # 5-Tab Bottom Navigation (Home, Library, Wishlist, Offers, Settings)
    │               ├── theme/               # Dark Gaming AMOLED Theme & Neon Accents
    │               ├── viewmodel/           # Reactive StateFlow ViewModels
    │               └── screens/             # Home, Game Details & Comparison Sheet, Library, Wishlist, Offers, Settings
    ├── build.gradle.kts
    └── settings.gradle.kts
```

---

## 1. Running the FastAPI Backend

### Prerequisites
- Python 3.10+ installed

### Setup & Launch
```bash
# From workspace root
cd backend

# Run the backend server
..\venv\Scripts\python run_server.py
```

- **Interactive Swagger Docs**: Open [http://127.0.0.1:8000/docs](http://127.0.0.1:8000/docs)
- **Health Check**: [http://127.0.0.1:8000/health](http://127.0.0.1:8000/health)
- **Live Search**: [http://127.0.0.1:8000/api/search?q=cyberpunk](http://127.0.0.1:8000/api/search?q=cyberpunk)
- **Epic Free Games**: [http://127.0.0.1:8000/api/free-games](http://127.0.0.1:8000/api/free-games)

### Running Automated Backend Tests
```bash
cd backend
..\venv\Scripts\pytest -v
```

---

## 2. Opening & Running the Android App

1. Launch **Android Studio**.
2. Select **Open** and choose the `d:\Projects\Android\android` folder.
3. Allow Gradle to sync dependencies.
4. Run the app on an Android Emulator (API 26+) or a physical device connected via USB/Wi-Fi.
   > **Note for Emulator**: The Android app is preconfigured to communicate with the host machine's FastAPI backend via `http://10.0.2.2:8000/`.

---

## Features Implemented

1. **Home & Game Search**: Search Steam & Epic catalogs concurrently with INR prices, cover art, discount chips, and recent search history.
2. **Price Comparison Engine**: Side-by-side Steam vs Epic pricing in ₹ INR, detecting the lowest valid purchase price and potential savings.
3. **Smart Purchase Assistant**: Highlights whether you already own a title, identifies sale events, and gives a direct recommendation verdict.
4. **My Gaming Library**: Unified view of Steam and Epic titles, playtime hours, estimated total library value in ₹, and duplicate game detection.
5. **Wishlist**: Device-local Room database storing tracked games and price targets.
6. **Free Games & Offers**: Epic Games weekly free giveaways with countdown/expiry dates and one-tap claim links.
7. **Store Account Connections**: Connect SteamID and Epic accounts with instant sync.
8. **Settings & Preferences**: Dark AMOLED gaming theme, default INR currency, and library refresh.
