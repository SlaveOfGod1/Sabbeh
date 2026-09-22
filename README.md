# Sabbeh — Digital Dhikr Counter

A beautiful native Android counter for your dhikr and prayers.

*Free of ads and trackers. Made for the sake of Allah. Works fully offline.*

## Features

### Digital Counter
- Large, easy-to-read display with automatic round tracking
- One-tap reset
- Haptic vibration when a round completes (toggleable in Settings)

### Dhikr Sessions
- Custom dhikrs with your own titles and targets
- Edit and delete any dhikr (long-press)
- Progress saved per session
- Optional auto-advance to the next dhikr after each round

### Themes
- 4 presets: Teal, Purple, Blue, Rose
- Custom color picker (remembers your pick)
- Night mode

### Languages
English, العربية, Türkçe, 中文, Melayu, اردو, 日本語

### Profile Management
- Export your data as a JSON backup file
- Import a backup to restore
- Reset all data

### Privacy
No ads, no trackers, no data collection. Everything stays on your device.

## Build

No Android Studio and no Gradle required — just a JDK and the Android SDK
command-line tools. Zero third-party dependencies.

### 1. Requirements
- JDK 17 or newer
- Android SDK with: `cmdline-tools`, `platforms;android-34`,
  `build-tools;34.0.0`, `platform-tools` (install via `sdkmanager`)

### 2. One-time SDK setup
```bat
sdkmanager "platforms;android-34" "build-tools;34.0.0"
```

### 3. Build the APK (Windows `cmd`, from the repo root)
Set these to your own paths first:
```bat
set SDK=<path-to-android-sdk>
set JDK=<path-to-jdk-21>
set PROJ=%CD%
set BT=%SDK%\build-tools\34.0.0
set AJAR=%SDK%\platforms\android-34\android.jar
mkdir "%PROJ%\build\flats" "%PROJ%\build\gen" "%PROJ%\build\obj" "%PROJ%\build\dex" "%PROJ%\build\apk"
```

Compile resources and link the base APK:
```bat
"%BT%\aapt2.exe" compile --dir "%PROJ%\app\src\main\res" -o "%PROJ%\build\flats"
"%BT%\aapt2.exe" link -o "%PROJ%\build\apk\app-unaligned.apk" -I "%AJAR%" --manifest "%PROJ%\app\src\main\AndroidManifest.xml" --java "%PROJ%\build\gen" --auto-add-overlay --min-sdk-version 24 --target-sdk-version 34 --version-code 3 --version-name 1.0.2 -R "%PROJ%\build\flats\values_colors.arsc.flat" -R "%PROJ%\build\flats\values_strings.arsc.flat" -R "%PROJ%\build\flats\values_styles.arsc.flat"
```

Compile, dex, pack, align and sign (password: `android`):
```bat
dir /s /b "%PROJ%\app\src\main\java\*.java" > "%PROJ%\build\sources.txt"
```
Quote each line of `sources.txt` and use forward slashes (javac argfile rules),
then:
```bat
"%JDK%\bin\javac.exe" --release 8 -classpath "%AJAR%" -d "%PROJ%\build\obj" @"%PROJ%\build\sources.txt"
"%JDK%\bin\jar.exe" cf "%PROJ%\build\classes.jar" -C "%PROJ%\build\obj" .
"%JDK%\bin\java.exe" -classpath "%SDK%\cmdline-tools\latest\lib\d8-classpath.jar" com.android.tools.r8.D8 --lib "%AJAR%" --min-api 24 --output "%PROJ%\build\dex" "%PROJ%\build\classes.jar"
cd /d "%PROJ%\build\dex"
"%JDK%\bin\jar.exe" uf "%PROJ%\build\apk\app-unaligned.apk" classes.dex
cd /d "%PROJ%"
"%BT%\zipalign.exe" -f 4 "%PROJ%\build\apk\app-unaligned.apk" "%PROJ%\build\apk\app-aligned.apk"
"%JDK%\bin\keytool.exe" -genkeypair -keystore "%PROJ%\debug.keystore" -alias sabbeh -keyalg RSA -keysize 2048 -validity 9125 -storepass android -keypass android -dname "CN=Sabbeh Debug"
"%JDK%\bin\java.exe" -jar "%BT%\lib\apksigner.jar" sign --ks "%PROJ%\debug.keystore" --ks-pass pass:android --key-pass pass:android --out "%PROJ%\build\Sabbeh-debug.apk" "%PROJ%\build\apk\app-aligned.apk"
```

The Ionicons font must be stored uncompressed under `assets/` (packed
separately after linking). See the build notes above if you script this.

### 4. Install (phone with USB debugging enabled)
```bat
"%SDK%\platform-tools\adb.exe" install -r "%PROJ%\build\Sabbeh-debug.apk"
```

## Project Layout

```
app/src/main/
  AndroidManifest.xml
  assets/fonts/Ionicons.ttf   # bundled icon font
  java/com/sabbeh/
    MainActivity.java         # counter UI, drawer, theme sheet, settings
    data/DhikrStore.java      # state + persistence (SharedPreferences)
    data/Themes.java          # theme presets + custom colors
    data/Translations.java    # all 7 languages
    models/Dhikr.java
    views/                    # gradient, hue slider, flow layout, icons
  res/                        # colors, styles, launcher icons
```

## License

Free for personal use only. See [LICENSE.md](./LICENSE.md).
Commercial use, redistribution for profit, and store uploads for commercial
purposes are prohibited.
