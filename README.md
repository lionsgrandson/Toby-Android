# Toby

Moshe Schwartzberg’s original voice assistant, repaired for modern Android. Toby keeps the original mascot and local notes, with a rebuilt voice/command lifecycle and a Java 17 build.

The complete untouched project is preserved on [`Original-Toby`](https://github.com/lionsgrandson/Toby-Android/tree/Original-Toby), at commit `e31e0e13e75544a91f1ac7353204388b070e9ee5`. Development is on `main`; the previous `master` branch is also untouched.

## Run Toby

Requires Android 8.0 or newer. Download `Toby-debug-apk` from a successful [Android build and tests run](https://github.com/lionsgrandson/Toby-Android/actions/workflows/android.yml), unzip it, and install `app-debug.apk` on your phone.

The debug APK is for personal testing. Updating an existing installation requires the same signing key as that installation. If Android reports a signature mismatch, build/sign with your original key to retain app data. Uninstalling erases notes. CI debug signing keys can also differ between runs; use your own stable signing key for ongoing use.

Tap **Speak**, grant microphone permission, and then talk. Toby does not listen in the background or continuously monitor a wake word. “Hey Toby” is accepted at the beginning of a command after tapping Speak. Android needs an installed, enabled speech recognition service and English language support. Recognition may use the provider’s internet service. Typed commands work without microphone permission or a speech service.

**Stop listening / speaking** cancels microphone capture, speech output, pending notes, and pending answer display. Recognition and speech are stopped when Toby leaves the foreground. Missing text-to-speech data does not prevent reading replies on screen.

## Commands

| Command | Behavior |
| --- | --- |
| `Save note buy milk` | Appends a note to private device storage |
| `Save note` | Asks for the next spoken or typed note; `cancel` exits |
| `Read notes` | Reads stored notes, including the original `note.txt` |
| `Set a timer for ten minutes` | Opens Clock with the duration supplied |
| `Set a timer for 1 hour 30 minutes` | Supports compound durations and number words through 99 |
| `Set an alarm for 7 am` | Opens Clock with an explicit time supplied |
| `Set an alarm for 19:30` | Supports 24-hour time |
| `Start stopwatch` / `Stop stopwatch` | Stopwatch continues across screen rotation, backgrounding, and same-boot process recreation |
| `What time is it?` / `What is the date?` | Local device time/date |
| `Echo hello` | Speaks/repeats text |
| `Who created you?` | Toby’s original origin story |
| `Help` | Lists available commands |

Confirm timers and alarms in your Clock app. Toby does not claim they were scheduled before confirmation. Day expressions such as “tomorrow” and ambiguous times open the Clock editor without guessing; choose the intended day/time there. A compatible Clock app is required. The stopwatch resets after device reboot and does not schedule an alert.

## Wolfram answers

In **Settings**, enter your own Wolfram App ID with [Short Answers API access](https://products.wolframalpha.com/short-answers-api/documentation). General questions are sent over HTTPS to Wolfram; requests run off the UI thread with timeouts and readable errors. No API key is embedded in the APK. Notes, timers, alarms, and local commands do not need a key.

The old source contained a public App ID. It is no longer used; revoke/rotate it in the Wolfram developer portal if it belongs to you, as it remains in the original branch/history. Your new ID is stored in app-private preferences and excluded from Android backup along with app data. This is suitable for your own device; do not distribute your personal API credential in a public build.

## Build on Windows

1. Install Android Studio, Android SDK Platform 35, and Build Tools 35.0.0.
2. Select **JDK 17** for Gradle in Android Studio. For terminal builds set `JAVA_HOME` to a JDK 17 installation.
3. Run `buildapp.cmd`. It detects your SDK from `local.properties`, `ANDROID_HOME`, `ANDROID_SDK_ROOT`, the usual `%LOCALAPPDATA%\Android\Sdk` folder, or `sdkmanager.bat` on PATH. It repairs a stale SDK path while preserving unrelated local properties.
4. If the SDK is elsewhere, paste its folder when asked. If no SDK is installed, install it in Android Studio > SDK Manager first. Missing Platform 35 and Build-Tools 35.0.0 packages are installed through `sdkmanager` when available; review its license prompt. Otherwise the helper tells you which components to install in Android Studio.

`local.properties` stays local and is ignored by Git. The helper only configures environment variables for the current build, without changing your global Windows settings. To only check SDK detection, run `powershell -NoProfile -ExecutionPolicy Bypass -File .\buildapp.ps1 -CheckOnly`. Direct Gradle builds still work with `gradlew.bat testDebugUnitTest lintDebug assembleDebug` after setup.

On Linux/macOS: `./gradlew testDebugUnitTest lintDebug assembleDebug`.

Build versions: Android Gradle Plugin 8.9.2, Gradle 8.11.1, JDK 17, compile/target SDK 35, minimum SDK 26. The build no longer depends on JCenter, mixed Support Library/AndroidX dependencies, or the obsolete Wolfram/Apache desktop JARs. Empty experimental modules are retained in source history but excluded from the build.

## Validation

CI builds the APK, runs command parser regression tests and Android lint, then runs device tests on API 26 and 35. Device tests cover launch without microphone permission, typed commands, old note compatibility, two-turn note capture/cancellation, rotation, stopwatch state, and modern speech error codes.

On a physical phone also check microphone grant/denial, speech recognition and spoken replies, stopping speech, a real Wolfram query with your own ID, airplane-mode errors, and timer/alarm confirmation followed by locking the phone. Automated tests cannot verify a manufacturer’s speech service, your Wolfram account, or audible alarm delivery.
