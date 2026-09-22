# Sabbeh — Native Java Port (Android)

This is a complete Java rewrite of the React Native + Expo app in `../Sabbeh`,
with the same features, same styles, same strings, and same icons/positions.
Zero external dependencies — only the Android SDK (no libraries to install).

## What was ported

| Original (JS) | Java port |
|---|---|
| `app/index.js` (main screen, theme bottom-sheet) | `MainActivity` (main screen + theme sheet) |
| `components/CounterDisplay.js` | counter housing inside `MainActivity` (260×220, LCD `#CFD8DC`, 64sp monospace, COUNT/RESET labels) |
| `components/CounterControls.js` | big 160dp count button + 50dp reset button, same colors `#1C262B` / `#263238` / `#37474F` |
| `components/CustomDrawerContent.js` | left drawer panel (85% width, header, dhikr cards with target badge, dashed add button, options + add/edit dialogs, long-press 500ms behaviour via long-click) |
| `components/SettingsModal.js` | full-screen settings overlay (language, haptic toggle, auto-advance toggle, about, export/import, GitHub links, reset) |
| `components/SimpleColorPicker.js` | `views/HueSliderView.java` (rainbow slider, `hsl(h,60,45)` formula identical) |
| `context/DhikrContext.js` | `data/DhikrStore.java` (same defaults, same order `4,1,2,3,5,6`, same round/haptic/auto-advance logic, same `SharedPreferences` JSON shape `@sabbeh_dhikrs_v1`) |
| `constants/constants.js` | `data/Translations.java` (all 7 languages, every key) + `data/Themes.java` (Teal/Purple/Blue/Rose/Night + custom lighten/darken) |
| `app/_layout.js` (drawer + forced LTR) | `AndroidManifest.xml` (`supportsRtl="false"`) + root forced `LAYOUT_DIRECTION_LTR` |
| `app/sessions.js` | Not ported on purpose: it is dead code in the original (hidden drawer screen, mock data, never navigated to) |
| Export/Import (SAF + Sharing) | `ACTION_CREATE_DOCUMENT` / `ACTION_OPEN_DOCUMENT` JSON, same payload keys (`version`, `timestamp`, `dhikrs`, `progress`, `currentDhikrId`, `theme`, `isNightMode`) |
| Icons (Ionicons) | Same positions, same glyphs: `assets/fonts/Ionicons.ttf` copied from the Expo app's own `node_modules` (`@expo/vector-icons`), loaded via `views/IconFont.java` — no downloads |

App icons: copy the PNGs from `../Sabbeh/assets/` into
`app/src/main/res/drawable/` (or use Android Studio → Resource Manager →
import) if you want the launcher icon to match exactly.

## How to test on your phone (no Android Studio, no Gradle)

You run everything yourself — nothing has been executed or downloaded for you.
You need internet once for the SDK packages download.

**1. Prep the phone**
- Settings → About phone → tap *Build number* 7 times → Developer options → enable *USB debugging*.
- Plug the phone into the computer with a USB cable → accept *Allow USB debugging?* on the phone (keep the screen unlocked).

**2. One-time SDK setup** — open PowerShell in the `Sabbeh-java` folder and run:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1 -InstallSdk
```

This accepts licenses and downloads `platforms;android-34` + `build-tools;34.0.0`
into your existing SDK at `NewPipe\Android Tools\Sdk` using your JDK 21.

**3. Build + install** (every time, after any code change):

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1 -Install
```

It compiles, dexes, aligns, signs, and runs `adb install -r`.
The app installs as **Sabbeh** (`com.Sabbeh.debug`), side-by-side with your
existing `com.Sabbeh` build. APK output: `build\Sabbeh-debug.apk`.

**4. Checks if something fails**
- `adb devices` (inside the SDK `platform-tools` folder) must list your phone as `device`, not `unauthorized` — if unauthorized, unplug/replug and accept the prompt again.
- If the JDK path differs on your machine, edit `$JdkRoot` at the top of `build.ps1`.
- To build without installing: run `.\build.ps1` with no flags.

## Notes

- Package: `com.Sabbeh.debug` (temporary debug id so it installs
  side-by-side with the `com.Sabbeh` build already on the phone;
  change `applicationId` in `app/build.gradle` back to `com.Sabbeh` for release).
- Version: `1.0.2` (versionCode 3).
- Data is stored on-device only, works offline, no ads/trackers — same as original.
- Min SDK 24 (Android 7.0+), target/compile SDK 34.
