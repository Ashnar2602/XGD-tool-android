<p align="center">
  <img src="docs/assets/logo.png" alt="XGDTool Android" width="220">
</p>

<p align="center">
  <a href="README.md">🇬🇧 English</a> ·
  <a href="README.it.md">🇮🇹 Italiano</a> ·
  <a href="README.fr.md">🇫🇷 Français</a> ·
  <a href="README.de.md">🇩🇪 Deutsch</a> ·
  <a href="README.es.md">🇪🇸 Español</a> ·
  <a href="README.pt.md">🇵🇹 Português</a> ·
  <a href="README.zh-CN.md">🇨🇳 简体中文</a>
</p>

# XGDTool for Android

Unofficial port of [XGDTool](https://github.com/wiredopposite/XGDTool)
(GPL-3.0) to Android: converts Xbox / Xbox 360 disc images (ISO, stripped
XISO) to **ZAR**, **GOD**, **CCI**, and **CSO** directly from your phone,
so you can back up your physical game collection without a PC.

<p align="center">
  <img src="docs/assets/screenshot_it.png" alt="App main screen" width="280">
</p>

## Features

- Convert between XISO, ZAR, GOD, CCI, CSO — the same C++ engine used by
  the desktop version of XGDTool.
- **Batch conversion**: select multiple files at once, they're processed
  in a queue automatically.
- Optional automatic online title lookup, to rename the output with a
  readable game title.
- No invasive storage permissions: everything goes through Android's
  Storage Access Framework — you choose which folders the app can see.
- Interface in 7 languages (auto-detected from the phone's system
  language): Italian, English, French, German, Spanish, Portuguese,
  Simplified Chinese.
- Runs as a foreground service: you can leave the app during a long
  conversion without interrupting it.

## Requirements

- Android 8.0 (API 26) or later, **arm64-v8a** architecture.
- Free space equal to at least 2× the size of the largest game in your
  collection.

## Installation

Go to the [Releases](../../releases) page of this repo, download the
latest `XGDTool-android-debug.apk`, and install it on your phone (you'll
need to enable "Install unknown apps"). For the full usage guide and
troubleshooting, see [docs/MANUAL.md](docs/MANUAL.md).

## Building from source

Requires Android NDK r27, Android SDK (platform 34), Gradle 8.7+, JDK 17+.
Full instructions in
[docs/MANUAL.md](docs/MANUAL.md#building-from-source).

## Disclaimer

Hobby project, not affiliated with or endorsed by Microsoft. "Xbox" is a
registered trademark of its respective owner, used here only in a
descriptive sense. Intended for personal backups of legally owned discs.

## License

The XGDTool core is GPL-3.0 — see [LICENSE](LICENSE). Third-party
components embedded in the core are listed in
[XGDTool/ATTRIBUTION.md](XGDTool/ATTRIBUTION.md). If you publicly share a
modified version, GPL-3.0 requires making the modified source available
too.
