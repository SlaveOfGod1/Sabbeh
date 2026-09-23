# Sabbeh - Digital Dhikr Counter

<div align="center">

![Sabbeh Logo](./app/src/main/res/drawable/app_icon.png)

**A beautiful native Android counter for your dhikr and prayers.**

*Free of Ads and Trackers. Made for the sake of Allah.*

[![GitHub](https://img.shields.io/badge/GitHub-SlaveOfGod1-blue?logo=github)](https://github.com/SlaveOfGod1/Sabbeh)
[![License](https://img.shields.io/badge/License-Personal%20Use%20Only-red)](./LICENSE.md)

[عربي](./README_AR.md)

</div>

---

## ✨ Features

### 🔢 Digital Counter
- Large, easy-to-read digital display
- Track rounds automatically when target is reached
- Reset counter with one tap
- **Haptic Feedback**: Optional vibration when a round is completed (toggleable in Settings)

### 📿 Dhikr Sessions
- **Custom Dhikrs**: Add your own dhikrs with custom titles and targets
- **Edit & Delete**: Long-press any dhikr to modify or remove it
- Progress saved for each dhikr session
- **Auto-advance**: Optionally switch to the next dhikr automatically after completing a round (toggleable in Settings)

### 🎨 Themes & Customization
- **4 Preset Themes**: Teal, Purple, Blue, Rose
- **Custom Color Picker**: Choose any color you like, the app remembers it
- **Night Mode**: Dark theme for comfortable use at night
- **App Icon**: Switch the launcher icon (Settings > App icon)

### 🌍 Multi-Language Support
Fully translated in 7 languages:
- English
- العربية (Arabic)
- Türkçe (Turkish)
- 中文 (Chinese)
- Bahasa Melayu (Malay)
- اردو (Urdu)
- 日本語 (Japanese)

### 💾 Data Management
- **Export Profile**: Save your data as a JSON file
- **Import Profile**: Restore your data from a backup
- **Reset Data**: Clear all data and start fresh

### 🛡️ Privacy First
- ✅ No Ads
- ✅ No Trackers
- ✅ No Data Collection
- ✅ Works Offline

---

## 🚀 Getting Started

### Prerequisites
- [JDK](https://adoptium.net/) 17 or newer
- Android SDK with `cmdline-tools`, `platforms;android-34`, `build-tools;34.0.0`, `platform-tools`
- No Android Studio and no Gradle needed — zero third-party dependencies

### Installation

1. **Install the SDK packages (once)**
   ```bat
   sdkmanager "platforms;android-34" "build-tools;34.0.0"
   ```

2. **Set your paths and compile the resources**
   ```bat
   set SDK=<path-to-android-sdk>
   set JDK=<path-to-jdk>
   set PROJ=%CD%
   set BT=%SDK%\build-tools\34.0.0
   set AJAR=%SDK%\platforms\android-34\android.jar
   "%BT%\aapt2.exe" compile --dir "%PROJ%\app\src\main\res" -o "%PROJ%\build\flats"
   "%BT%\aapt2.exe" link -o "%PROJ%\build\apk\app-unaligned.apk" -I "%AJAR%" --manifest "%PROJ%\app\src\main\AndroidManifest.xml" --java "%PROJ%\build\gen" --auto-add-overlay --min-sdk-version 24 --target-sdk-version 34 --version-code 3 --version-name 1.0.2 -R "%PROJ%\build\flats\values_colors.arsc.flat" -R "%PROJ%\build\flats\values_strings.arsc.flat" -R "%PROJ%\build\flats\values_styles.arsc.flat"
   ```

3. **Compile, dex, pack, align and sign**
   ```bat
   "%JDK%\bin\javac.exe" --release 8 -classpath "%AJAR%" -d "%PROJ%\build\obj" <your-sources>
   "%JDK%\bin\jar.exe" cf "%PROJ%\build\classes.jar" -C "%PROJ%\build\obj" .
   "%JDK%\bin\java.exe" -classpath "%SDK%\cmdline-tools\latest\lib\d8-classpath.jar" com.android.tools.r8.D8 --lib "%AJAR%" --min-api 24 --output "%PROJ%\build\dex" "%PROJ%\build\classes.jar"
   "%BT%\zipalign.exe" -f 4 "%PROJ%\build\apk\app-unaligned.apk" "%PROJ%\build\apk\app-aligned.apk"
   "%JDK%\bin\java.exe" -jar "%BT%\lib\apksigner.jar" sign --ks <your.keystore> --ks-pass pass:<password> --key-pass pass:<password> --out "%PROJ%\build\Sabbeh.apk" "%PROJ%\build\apk\app-aligned.apk"
   ```

4. **Run on your device** (USB debugging enabled)
   ```bat
   "%SDK%\platform-tools\adb.exe" install -r "%PROJ%\build\Sabbeh.apk"
   ```

---

## 🛠️ Tech Stack

- **Java** - Native Android app, no frameworks
- **Android SDK only** - aapt2, d8, apksigner, adb directly
- **SharedPreferences** - Local data persistence
- **Canvas gradients** - Theme backgrounds drawn at runtime

---

## 🤝 Contributing

Contributions are welcome! Feel free to:
- Report bugs via [GitHub Issues](https://github.com/SlaveOfGod1/Sabbeh/issues)
- Submit pull requests
- Suggest new features

---

## 📄 License

This project is **free for personal use only**.

> ⚠️ **IMPORTANT NOTICE**
>
> - ❌ **Commercial use is strictly prohibited**
> - ❌ **Selling or redistributing this app for profit is not allowed**
> - ❌ **Uploading to app stores for commercial purposes is forbidden**
> - ✅ Personal use only
>
> **Violation of these terms will result in a DMCA takedown notice.**

---

## 🤲 Dua

May Allah accept this work and make it beneficial for the Ummah.

*اللهم تقبل منا إنك أنت السميع العليم*

---

<div align="center">

**Made for the sake of Allah**

</div>
