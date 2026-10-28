# Third-party licenses

Search & Translate is licensed under the [GNU General Public License v3.0](../LICENSE).
It is a fork of [CircleToSearch](https://github.com/AKS-Labs/CircleToSearch) by AKS Labs (GPL-3.0).

It includes the following third-party components. The full license texts are in this
folder and are also packaged inside the app (APK `assets/`).

## Fonts

| Component | Copyright | License |
|---|---|---|
| [Google Sans Flex](https://github.com/googlefonts/googlesans-flex) | Copyright 2015 The Google Sans Flex Authors | SIL Open Font License 1.1 — [OFL-1.1-GoogleSansFlex.txt](OFL-1.1-GoogleSansFlex.txt) |

"Google", "Google Sans" and "Google Sans Flex" are trademarks of Google LLC. The font is used
unmodified; this app is not affiliated with or endorsed by Google.

Google Lens (Google LLC), Bing (Microsoft Corporation) and TinEye (Idée Inc.) are trademarks of
their respective owners and are only named to describe the search services the app can open.

## Translator module (`OCR-translation`)

| Component | License |
|---|---|
| [Offline Translator](https://github.com/DavidVentura/offline-translator) by David Ventura (Kotlin sources of the module) | GPL-3.0 — [LICENSE](../LICENSE) |
| [translator-rs](https://github.com/DavidVentura/translator-rs) by David Ventura | MIT — [MIT-translator-rs.txt](MIT-translator-rs.txt) |
| [PP-OCR models](https://github.com/PaddlePaddle/PaddleOCR) (bundled in `assets/ppocr`) | Apache-2.0 — [Apache-2.0.txt](Apache-2.0.txt) |
| [Readability.js](https://github.com/mozilla/readability) (Arc90 Inc, Mozilla) | Apache-2.0 — [Apache-2.0.txt](Apache-2.0.txt) |

The native library `libbindings.so` is built from translator-rs and statically includes:

| Component | License |
|---|---|
| [MuPDF](https://mupdf.com/) (Artifex Software) | AGPL-3.0 — [AGPL-3.0.txt](AGPL-3.0.txt) |
| [eSpeak NG](https://github.com/espeak-ng/espeak-ng) | GPL-3.0 — [LICENSE](../LICENSE) |
| [MNN](https://github.com/alibaba/MNN) (Alibaba) | Apache-2.0 — [Apache-2.0.txt](Apache-2.0.txt) |
| [bergamot-translator](https://github.com/browsermt/bergamot-translator) | MPL-2.0 — [MPL-2.0.txt](MPL-2.0.txt) |
| [SentencePiece](https://github.com/google/sentencepiece) | Apache-2.0 — [Apache-2.0.txt](Apache-2.0.txt) |
| [adblock-rust](https://github.com/brave/adblock-rust) (Brave) | MPL-2.0 — [MPL-2.0.txt](MPL-2.0.txt) |
| [UniFFI](https://github.com/mozilla/uniffi-rs) (Mozilla) | MPL-2.0 — [MPL-2.0.txt](MPL-2.0.txt) |
| [Piper](https://github.com/rhasspy/piper) / piper-rs | MIT |

Further Rust crates are listed in `OCR-translation/src/main/bindings/Cargo.lock`; they are
published under MIT and/or Apache-2.0 unless noted otherwise in their own repositories.
The source code of all MPL-2.0 and (A)GPL components is available from the projects linked
above and, for this app, from this repository.

`libc++_shared.so` is part of the Android NDK (LLVM project), Apache-2.0 with LLVM exception.

Translation and speech models are downloaded at runtime from the translator project's catalog
and are not part of this repository; each model is distributed under its own license.

## Android libraries

| Component | License |
|---|---|
| AndroidX / Jetpack Compose, Material Components for Android (Google) | Apache-2.0 |
| Kotlin standard library, kotlinx.coroutines, kotlinx.serialization (JetBrains) | Apache-2.0 |
| [Gson](https://github.com/google/gson) (Google) | Apache-2.0 |
| [uCrop](https://github.com/Yalantis/uCrop) (Yalantis) | Apache-2.0 |
| [JNA](https://github.com/java-native-access/jna) | Apache-2.0 (dual-licensed with LGPL-2.1) |
| [Material Symbols / Material Icons](https://github.com/google/material-design-icons) (Google) — the app icon combines the "search" and "translate" symbols; further icons in the UI | Apache-2.0 |

Full text: [Apache-2.0.txt](Apache-2.0.txt).

## Trademarks

Google, Google Lens, Bing and TinEye are trademarks of their respective owners. They are
mentioned only to name the search services the app can open. This app is not affiliated with,
sponsored or endorsed by any of them.
