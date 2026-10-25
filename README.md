<!-- ---------- Header ---------- -->
<div align="center">
  <img src="fastlane/metadata/android/en-US/images/icon.png" width="200" height="200">
  <h1>Search & Translate</h1>
  <h3>Circle anything on your screen to search or translate it</h3>
  <p>🔍 <em>Search</em> · 🌐 <em>Translate offline</em></p>

<!-- ---------- Badges ---------- -->
  <div align="center">
    <img alt="License" src="https://img.shields.io/badge/License-GPL--3.0-blue.svg?style=flat-square">
    <img alt="Last commit" src="https://img.shields.io/github/last-commit/fuzzyfly0/SearchAndTranslate?color=c3e7ff&style=flat-square">
    <img alt="Repo size" src="https://img.shields.io/github/repo-size/fuzzyfly0/SearchAndTranslate?color=c3e7ff&style=flat-square">
    <br>




<a href="https://github.com/fuzzyfly0/SearchAndTranslate/releases">

  <img src="https://img.shields.io/github/downloads/fuzzyfly0/SearchAndTranslate/total?color=ff9500&style=for-the-badge&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHRpdGxlPmRvd25sb2FkPC90aXRsZT48cGF0aCBkPSJNNSwyMEgxOVYxOEg1TTE5LDlIMTVWM0g5VjlINUwxMiwxNkwxOSw5WiIgZmlsbD0id2hpdGUiIC8+PC9zdmc+&labelColor=a6660d"/>

</a>

<a href="https://github.com/fuzzyfly0/SearchAndTranslate/stargazers">

  <img src="https://img.shields.io/github/stars/fuzzyfly0/SearchAndTranslate?color=ffff00&style=for-the-badge&labelColor=a1a116&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHRpdGxlPnN0YXI8L3RpdGxlPjxwYXRoIGQ9Ik0xMiwxNy4yN0wxOC4xOCwyMUwxNi41NCwxMy45N0wyMiw5LjI0TDE0LjgxLDguNjJMMTIsMkw5LjE5LDguNjJMMiw5LjI0TDcuNDUsMTMuOTdMNS44MiwyMUwxMiwxNy4yN1oiIGZpbGw9IndoaXRlIiAvPjwvc3ZnPg=="/>

</a>
</div>

<!-- ----------   Labels ---------- -->
<div align="center">
  <img alt="API" src="https://img.shields.io/badge/Api%2029+-50f270?logo=android&logoColor=black&style=for-the-badge"/>
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-a503fc?logo=kotlin&logoColor=white&style=for-the-badge"/>
  <img alt="Jetpack Compose" src="https://img.shields.io/static/v1?style=for-the-badge&message=Jetpack+Compose&color=4285F4&logo=Jetpack+Compose&logoColor=FFFFFF&label="/>
  <img alt="material" src="https://custom-icon-badges.demolab.com/badge/material%20you-lightblue?style=for-the-badge&logocolor=333&logo=material-you"/>
</div>
</div>

---
## ℹ️ About this fork

**Search & Translate** is a fork of [CircleToSearch](https://github.com/AKS-Labs/CircleToSearch) by [AKSLabs](https://github.com/akslabs), renamed and modified by [fuzzyfly0](https://github.com/fuzzyfly0). Like the original, it is licensed under the [GNU GPL v3](LICENSE); the original copyright notices are kept in the source files.

It installs alongside the original app (package `com.fuzzyfly0.searchandtranslate`). It is not affiliated with AKSLabs or Google.

Google and Google Lens are trademarks of Google LLC, Bing is a trademark of Microsoft Corporation, TinEye is a trademark of Idée Inc. They are named only to describe which search services the app can open; this project is not affiliated with or endorsed by any of them.

The app bundles third-party components (fonts, OCR models, translation engine, MuPDF and others) under their own licenses — see [THIRD_PARTY_LICENSES.md](licenses/THIRD_PARTY_LICENSES.md).

---
## 🎯 What is Search & Translate?

Draw a circle around anything on your screen – then **search** it or **translate** it right where it is. Works on any Android 10+ phone, no Google app or Play Services required for translation, text recognition and QR codes.

---
## ✨ Features

### 🔍 Search
- **Circle to select**: Draw a circle or scribble around anything on the screen, or search the whole screen ("Search all").
- **Search or Translate**: After circling, choose *Search* or *Translate* right next to the selection – or set one of them as the default (Settings › Search).
- **Image search engines**: Google Lens, Bing and TinEye. Your cookie-banner choice for Google and Bing is remembered.
- **Voice and song search**: Mic button for a spoken web search; song button opens Shazam, SoundHound or the Google app.
- **Share & Save**: Share or save the circled area, also directly from the search results.

### 🌐 Translate (offline)
- **In-place translation**: The circled area – or the whole screen – is translated and drawn over the original text.
- **On-device**: Text recognition (PaddleOCR) and translation (Bergamot models) run on your phone. Language packs are downloaded once, then everything works offline.
- **Live cam**: Point the camera at text and see it translated live.

### 🧰 More tools
- **Links & info**: Finds links, phone numbers, e-mail addresses and more on the screen – offline.
- **QR codes**: Detected offline.
- **Copy text**: Tap words on the screen to select and copy them.

### 📱 Starting the app
- Double-tap the status bar, use the floating bubble, a quick-settings tile, a hardware button, or set it as the default assistant (home button long-press / corner swipe).

### 🔒 Privacy
- Only the area you circle is used. Translation, text recognition, QR codes and links & info stay on the device.
- **Image search needs the internet**: the circled area is uploaded to a temporary image host (imgdb.io, or Litterbox as fallback; deleted after 1 hour) so the search engine can load it. No tracking or analytics in the app.

---
## 📋 Requirements

- Android 10 or newer.
- Download the APK ending in **`arm64-v8a`** from the [releases](https://github.com/fuzzyfly0/SearchAndTranslate/releases) – it fits almost every current phone. Use `armeabi-v7a` only for old 32-bit devices.
- For translation, download a language pack once (Settings › General › Offline Translator).

## 🔑 Permissions

- **Accessibility service** – used to take a screenshot when you start the app (status-bar double-tap, bubble, hardware button) and to draw the translation over other apps. It does not read or collect anything in the background.
- **Default assistant** (optional) – lets you start the app with a home-button long-press or corner swipe, without the accessibility service.
- **Camera** – only for Live cam.
- **Internet** – only for image search and downloading language packs.
- Also requested: **storage** (language packs, saving images), **notifications** (download progress), **list of installed apps** (picking apps in the settings, opening song-recognition apps), **start at boot** and **screen capture** (used by the translator module).

---



## 📥 **Get Search & Translate**

<div align="center">
    <p align="center">
  <a href="https://github.com/fuzzyfly0/SearchAndTranslate/releases">
    <img alt="Get it on GitHub" src="https://user-images.githubusercontent.com/69304392/148696068-0cfea65d-b18f-4685-82b5-329a330b1c0d.png" height="80px">
  </a>
  
  <br>
  <p><strong>• 🔓 Open Source    • 🚀 Ready to Use</strong></p>
</div>

---


## 🤝 **Community**

- 🐛 **Bug reports:** [GitHub Issues](https://github.com/fuzzyfly0/SearchAndTranslate/issues)
- 💡 **Feature requests:** [GitHub Issues](https://github.com/fuzzyfly0/SearchAndTranslate/issues)
- 👨‍💻 **Contribute:** Fork and submit pull requests
- ⭐ Star the repo


---

<p align="center">
  Based on <a href="https://github.com/AKS-Labs/CircleToSearch">CircleToSearch</a> by <a href="https://github.com/akslabs">AKSLabs</a>, licensed under GPL-3.0
</p>
