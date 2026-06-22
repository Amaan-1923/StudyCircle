# StudyCircle 📚

An AI-powered social Android app that helps students find study partners, share notes, get instant doubt-solving help, and connect with peers nearby.

Built as a portfolio project to demonstrate full-stack Android development skills.

---

## 📱 Screenshots

> Add screenshots here after taking them from your emulator/device

---

## ✨ Features

- 🔐 **Authentication** — Email/password login and registration via Firebase Auth
- 📰 **Real-time Feed** — Post study questions, share notes, and filter by subject with live Firestore updates
- 🤖 **AI Study Assistant** — Chat with StudyBot powered by Groq's LLaMA 3.3 70B model for instant doubt-solving
- 💬 **Real-time Chat** — Join subject-based study rooms with live messaging via Firebase Realtime Database
- 🗺️ **Nearby Students** — Discover and connect with students studying near you using Google Maps SDK
- 🎨 **Dark Mode** — Full dark/light theme support with custom Material 3 design system

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| Authentication | Firebase Auth |
| Database | Firebase Firestore + Firebase Realtime DB |
| AI | Groq API (LLaMA 3.3 70B) |
| Maps | Google Maps SDK + Compose Maps |
| Local DB | Room DB |
| Networking | Retrofit + OkHttp |
| Image Loading | Coil |
| Navigation | Jetpack Navigation Compose |
| Dependency Injection | Manual (Repository pattern) |
| Version Control | Git + GitHub |

---

## 🏗️ Architecture

This project follows **MVVM + Clean Architecture** principles:
app/

├── data/

│   ├── local/          → Room DB, DAOs

│   ├── remote/         → API calls, Firebase

│   └── repository/     → Single source of truth

├── domain/

│   └── model/          → Data classes (User, Post, Message, etc.)

├── ui/

│   ├── auth/           → Login, Register screens

│   ├── feed/           → Home feed, Create post

│   ├── ai/             → AI Assistant (StudyBot)

│   ├── chat/           → Chat rooms, Messaging

│   ├── map/            → Nearby students map

│   ├── components/     → Shared UI components

│   └── theme/          → Colors, Typography, Theme

└── utils/              → Helper functions
---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK API 26+
- A Google account for Firebase

### Setup

1. **Clone the repository**
```bash
git clone https://github.com/Amaan-1923/StudyCircle.git
cd StudyCircle
```

2. **Firebase Setup**
    - Create a new Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
    - Add an Android app with package name `com.example.studycircle`
    - Download `google-services.json` and place it in the `app/` folder
    - Enable **Email/Password** authentication
    - Create a **Firestore** database
    - Create a **Realtime Database**

3. **API Keys Setup**

   Create a `local.properties` file in the root directory and add:
   GROQ_API_KEY=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
   MAPS_API_KEY=AIzaSyA2DJl5L0uskLMLqs_f5U30c9l-7frWEeU

- Get a free Groq API key at [console.groq.com](https://console.groq.com)
    - Get a Maps API key from [Google Cloud Console](https://console.cloud.google.com) with Maps SDK for Android enabled

4. **Build and Run**
    - Open the project in Android Studio
    - Sync Gradle files
    - Run on an emulator or physical device (API 26+)

---

## 📂 Key Files

| File | Purpose |
|---|---|
| `AuthRepository.kt` | Firebase Auth logic |
| `PostRepository.kt` | Firestore CRUD for posts |
| `ChatRepository.kt` | Firebase RTDB for real-time chat |
| `AiRepository.kt` | Groq API integration |
| `LocationRepository.kt` | Location sharing logic |
| `NavGraph.kt` | Navigation between all screens |
| `Theme.kt` | Custom Material 3 theme |

---

## 🔑 Features in Detail

### AI Study Assistant
StudyBot uses Groq's LLaMA 3.3 70B model — one of the fastest and most capable open-source LLMs available. It maintains conversation history for multi-turn interactions and is prompted specifically for academic help.

### Real-time Feed
Posts are stored in Firestore and fetched using snapshot listeners, meaning new posts appear instantly without refreshing. Users can filter by subject using Material 3 filter chips.

### Real-time Chat
Chat rooms use Firebase Realtime Database for true real-time messaging. Messages appear instantly across all connected devices with animated bubble UI.

### Maps Integration
Users can optionally share their current location to appear on the map for other students to discover. Location data is stored in Firebase RTDB and removed when the user leaves the screen.

---

## 📋 Future Improvements

- [ ] Push notifications via Firebase Cloud Messaging
- [ ] Note sharing with Firebase Storage
- [ ] Study groups with invite system
- [ ] User profiles with subject interests
- [ ] Study session scheduling
- [ ] Leaderboard and study streaks

---

## 👨‍💻 Developer

**Amaan Shaikh**
BE Computer Engineering — Mumbai University

[![GitHub](https://img.shields.io/badge/GitHub-Amaan--1923-black?style=flat&logo=github)](https://github.com/Amaan-1923)

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).