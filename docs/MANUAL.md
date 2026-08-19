<p align="center">
  <a href="MANUAL.md">🇬🇧 English</a> ·
  <a href="MANUAL.it.md">🇮🇹 Italiano</a> ·
  <a href="MANUAL.fr.md">🇫🇷 Français</a> ·
  <a href="MANUAL.de.md">🇩🇪 Deutsch</a> ·
  <a href="MANUAL.es.md">🇪🇸 Español</a> ·
  <a href="MANUAL.pt.md">🇵🇹 Português</a> ·
  <a href="MANUAL.zh-CN.md">🇨🇳 简体中文</a>
</p>

# User & Debug Manual — XGDTool for Android

## Table of contents

- [What it is](#what-it-is)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick start](#quick-start)
- [Full interface guide](#full-interface-guide)
- [Output formats](#output-formats)
- [How it works internally](#how-it-works-internally)
- [Debugging / troubleshooting](#debugging--troubleshooting)
- [FAQ](#faq)
- [Known limitations](#known-limitations)
- [Building from source](#building-from-source)
- [License and credits](#license-and-credits)

## What it is

XGDTool for Android brings the C++ core of
[XGDTool](https://github.com/wiredopposite/XGDTool) (GPL-3.0) — the same
conversion engine used on PC — directly to your phone, to convert Xbox
and Xbox 360 disc images (ISO, stripped XISO) into more compact or
emulator-friendly formats (**ZAR**, **GOD**, **CCI**, **CSO**), without
needing a computer.

The original GUI (wxWidgets) isn't portable to Android, so it was
replaced with a Kotlin app that talks to the same C++ core via JNI.

## Requirements

- Android 8.0 (API 26) or later.
- **arm64-v8a** architecture (the vast majority of recent Android
  phones). 32-bit-only devices aren't supported by this build — see
  [Building from source](#building-from-source).
- Free space equal to at least **2× the size of the largest game** in
  your collection (the copied source file and the produced output
  temporarily coexist during conversion).
- Internet connection is **optional**: only needed for automatic online
  title lookup (to name converted files more clearly); the app works
  fine offline too.

## Installation

1. Download the latest `XGDTool-android-debug.apk` from the
   [Releases](../../releases) section of this repo.
2. On your phone, enable "Install unknown apps" for whichever app you
   use to open the file (file manager, browser, etc.).
3. Install the APK. It's signed with the standard Android debug key —
   fine for personal sideloading, not intended for the Play Store.
4. On first launch the app asks for the notifications permission (needed
   for the background service's progress notification).

The app automatically detects the system language among the 7 supported
(Italian, English, French, German, Spanish, Portuguese, Simplified
Chinese); if the phone's language isn't among these, it falls back to
English.

## Quick start

1. Tap **Select ISO/XISO to convert** and choose one or more files
   (multi-select supported: long-press a file in the system picker to
   enable it).
2. Tap **Select destination folder** and choose where converted files
   will be saved — any folder accessible from your phone, including an
   external SD card.
3. Choose the **output format** among the available chips (ZAR is
   preselected).
4. Tap **Convert**.
5. Follow progress in the "Progress" card and the log at the bottom of
   the screen. You can leave the app during conversion: it runs as a
   foreground service with its own notification, and survives being
   backgrounded.

## Full interface guide

**Source** — the file (or files) ISO/XISO to convert. The picker uses
Android's Storage Access Framework: you can pick from any storage
provider visible to your phone (internal storage, SD card, local cloud
sync folders, etc.).

**Destination** — the folder where converted files will be written.
Again, any folder accessible via SAF.

**Conversion options**
- *Format*: see [Output formats](#output-formats) below.
- *Offline mode*: if disabled, the app tries to look up the correct game
  title online to name the output more clearly (requires network, with a
  maximum timeout of a few seconds — if the network is slow or absent
  the app waits up to that timeout and then proceeds offline anyway). If
  enabled, this step is skipped entirely.

**Progress** — each file goes through 3 visible phases, each with its
own label and progress bar:

1. **Local copy** — the chosen file is copied from its SAF location into
   the app's private cache (necessary because the native engine works on
   real paths, not content:// Uris).
2. **Conversion** — connectivity check, online title lookup (if not
   offline), then the actual write pass. Starts with an "indeterminate"
   (scrolling) bar because the network phase's duration isn't
   predictable, then switches to a real percentage once it starts
   writing data.
3. **Writing output** — copying the result from cache to the chosen
   destination folder.

With multiple files selected, these 3 phases repeat in sequence for each
one (no parallel conversion — see [Known limitations](#known-limitations));
the header shows "File X of Y" with the current file's name.

**Cancel** stops the current conversion cleanly, at the next useful
checkpoint — it doesn't truncate a write mid-way.

**Log** — shows every line produced by the native engine, useful to see
exactly what it's doing or why a file failed.

## Output formats

| Format | Typical use |
|---|---|
| **ZAR** | Universal compressed archive, the most efficient format for use with **Xenia Canary** (Xbox 360 emulator). Preselected. |
| **GOD** | *Games on Demand* — native format used by Xbox 360 and various front-ends/RGH setups. |
| **CCI** | Compressed format intended for original Xbox emulators. |
| **CSO** | Compressed format, alternative to CCI supported by several emulators. |

## How it works internally

```
SAF Uri (input)
   │  byte-by-byte copy with progress
   ▼
app private cache (real path)
   │  XgdNative.convert() — JNI, synchronous, on a dedicated thread
   ▼
libxgdtool.so (XGDTool C++ core)
   │  writes the chosen format into a cache folder
   ▼
app private cache (output)
   │  byte-by-byte copy with progress to the SAF destination
   ▼
Destination folder chosen by the user
```

One file at a time, in a sequential queue. Each phase reports
bytes-processed/total to the UI via JNI callbacks
(`XgdCallback.onLog` / `onProgress`), handled in `ConvertService` (an
Android foreground service).

## Debugging / troubleshooting

If a file fails, the app's log shows the reason — look for a line like:

```
<Error type> in <file:line>: <detail>
```

If you need more context than what's shown in the app (e.g. to
understand a native crash, not just a handled exception), connect the
phone to a PC with `adb` and run, during a conversion:

```bash
adb logcat -s XgdJNI:* XgdCore:*
```

- `XgdCore` prints every log line and progress update emitted by the
  converter's C++ code (same content as the in-app log, but nothing is
  lost if the app is backgrounded or the process dies).
- `XgdJNI` prints JNI bridge diagnostics (callback method resolution,
  pending exceptions).

If the app behaves strangely with no useful log even in logcat, a native
crash (SIGSEGV) is likely — in that case you'll need a full (unfiltered)
`adb logcat` taken right after the crash, or a tombstone
(`adb bugreport` / `/data/tombstones`).

Common issues:

- **The system file picker shows "No items"** even when browsing the
  phone's main storage — this is an Android system component issue
  (DocumentsUI/ExternalStorageProvider), not the app. Try rebooting the
  phone, or clearing the cache of the system "Files"/"File manager" app
  from Settings.
- **Copy stuck or file truncated**: check free space (see
  [Requirements](#requirements)); the app detects and reports an
  incomplete copy instead of proceeding with partial data.
- **Conversion slow to start**: if offline mode is disabled and the
  network is absent or very slow, the app waits for the online title
  lookup's maximum timeout before proceeding — enabling offline mode
  avoids this wait.

## FAQ

**Does the app modify or delete the original files?**
No. Source files are only read (copied to a temporary cache for
conversion, then the cached copy is deleted once done). The output is
always a new file in the destination folder you chose.

**Do I need an internet connection?**
No, it's optional. It's only used for automatic online title lookup
(nicer output naming). With offline mode enabled, or without network
available, the app still converts, using the game's name as it appears
on the disc.

**Can I convert multiple files at once?**
Yes, select multiple files in step 1 — they'll be processed in a queue
one at a time, with a final summary of how many succeeded/failed.

**Is this an official Microsoft/Xbox app?**
No. It's a hobby project, not affiliated with or endorsed by Microsoft.
"Xbox" is a registered trademark of its respective owners, used here
only in a descriptive/compatibility sense.

## Known limitations

- One file at a time, no parallel conversion.
- The SAF → cache copy requires free space equal to at least 2× the size
  of the largest game in your collection (the copied source file and the
  produced output temporarily coexist).
- **arm64-v8a** only (the vast majority of recent phones; if yours is a
  32-bit-only device it needs to be rebuilt for `armeabi-v7a` too, not
  included in this build).
- APK signed with the debug key: reinstalling over a previous version
  always works (same signature), but it's not distributable through app
  stores.
- No automated test suite: every change is verified with a clean build +
  manual testing on a real device.

## Building from source

Requires Android NDK r27, Android SDK (platform 34, build-tools 34.0.0),
Gradle 8.7+, JDK 17+.

```bash
# 1. Cross-compile zstd, lz4, OpenSSL, curl for arm64-v8a with the NDK.
#    XGDTool/android/CMakeLists.txt expects everything under
#    ~/android/install-arm64 by default (override with -DXGD_DEPS_PREFIX).

# 2. Build the native library
cd XGDTool/android && mkdir build && cd build
cmake -G Ninja \
  -DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK_HOME/build/cmake/android.toolchain.cmake \
  -DANDROID_ABI=arm64-v8a -DANDROID_PLATFORM=android-24 \
  -DCMAKE_PREFIX_PATH=$HOME/android/install-arm64 \
  -DCMAKE_FIND_ROOT_PATH=$HOME/android/install-arm64 \
  -DCMAKE_FIND_ROOT_PATH_MODE_INCLUDE=BOTH \
  -DCMAKE_FIND_ROOT_PATH_MODE_LIBRARY=BOTH \
  -DCMAKE_FIND_ROOT_PATH_MODE_PACKAGE=BOTH ..
ninja
$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/<host>/bin/llvm-strip \
  --strip-unneeded libxgdtool.so -o ../../../XGDToolAndroid/app/src/main/jniLibs/arm64-v8a/libxgdtool.so
# <host> = linux-x86_64, windows-x86_64, or darwin-x86_64 depending on your system

# 3. Build the APK
cd ../../../XGDToolAndroid
gradle clean assembleDebug
# APK at app/build/outputs/apk/debug/app-debug.apk
```

Note: `XGDTool/cmake/embed_resources.cmake` generates
`XGDTool/src/Executable/AttachXbe.h` from a binary included in the
`external/Repackinator` submodule the first time CMake configures — don't
touch it by hand, it regenerates itself.

## License and credits

XGDTool's core and this port are distributed under **GPL-3.0** — see
[LICENSE](../LICENSE) at the repo root. The C++ core in turn embeds
third-party components listed in
[ATTRIBUTION.md](../XGDTool/ATTRIBUTION.md).

Upstream project: [wiredopposite/XGDTool](https://github.com/wiredopposite/XGDTool).
