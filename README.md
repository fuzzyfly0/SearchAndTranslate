<div align="center">
  <img src="fastlane/metadata/android/en-US/images/icon.png" width="160" height="160">
  <h1>Search & Translate</h1>
  <p><strong>Circle anything on your screen to search or translate it</strong></p>
  <p>
    <a href="LICENSE"><img alt="License GPL-3.0" src="https://img.shields.io/badge/License-GPL--3.0-blue.svg?style=flat-square"></a>
    <a href="https://github.com/fuzzyfly0/SearchAndTranslate/releases"><img alt="Downloads" src="https://img.shields.io/github/downloads/fuzzyfly0/SearchAndTranslate/total?color=ff9500&style=flat-square"></a>
  </p>
  <p>📥 <a href="https://github.com/fuzzyfly0/SearchAndTranslate/releases/latest"><strong>Download the latest release</strong></a></p>
</div>

---
## 🎯 What is Search & Translate?

Draw a circle around anything on your screen – then **search** it or **translate** it right where it is. Works on any Android 10+ phone, no Google app or Play Services required for translation and text recognition.

---
## ✨ Features

### 🔍 Search
- **Circle to select**: Draw a circle or scribble around anything on the screen, or search the whole screen ("Search all").
- **Search or Translate**: After circling, choose *Search* or *Translate* right next to the selection – or set one of them as the default (Settings › Search).
- **Image search engines**: Google Lens, Bing and TinEye. Your cookie-banner choice for Google and Bing is remembered.
- **Voice and song search**: Mic button for a spoken web search; song button opens Shazam, SoundHound or the Google app.
- **Share & Save**: Share or save the circled area, also directly from the search results.
- **Copy text**: Tap words on the screen to select and copy them.

### 🌐 Translate (offline)
- **In-place translation**: The circled area – or the whole screen – is translated and drawn over the original text.
- **On-device**: Text recognition (PaddleOCR) and translation (Bergamot models) run on your phone. Language packs are downloaded once, then everything works offline.
- **Live cam**: Point the camera at text and see it translated live.

### 📱 Starting the app
- Double-tap the status bar, use the floating bubble, a quick-settings tile, a hardware button, or set it as the default assistant (home button long-press / corner swipe).

### 🔒 Privacy
- Only the area you circle is used. Translation and text recognition stay on the device.
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
## ℹ️ About

**Search & Translate** is a fork of [CircleToSearch](https://github.com/AKS-Labs/CircleToSearch) by [AKSLabs](https://github.com/akslabs), licensed under the [GNU GPL v3](LICENSE). Third-party components are listed in [THIRD_PARTY_LICENSES.md](licenses/THIRD_PARTY_LICENSES.md).

Google Lens (Google LLC), Bing (Microsoft Corporation) and TinEye (Idée Inc.) are trademarks of their owners and are only named to describe the search services the app can open. This project is not affiliated with or endorsed by AKSLabs, Google, Microsoft or TinEye.

Bug reports and ideas: [GitHub Issues](https://github.com/fuzzyfly0/SearchAndTranslate/issues)
