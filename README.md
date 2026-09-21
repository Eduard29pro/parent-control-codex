# Parent Control — Codex Development Pack

Android parental-control MVP for enforcing maximum screen brightness and media volume on a child device.

Start with `CODEX.md`, then read the documents under `docs/`.

Target: Kotlin, Android Studio, Jetpack Compose, minSdk 29, target latest stable SDK available in the environment.

Core requirements:
- Parent PIN gate.
- Configurable maximum brightness and media-volume percentages.
- Child may reduce values, but cannot keep them above the configured maximum.
- Persist settings across reboot.
- Re-apply limits after boot.
- Prefer Android Enterprise / Device Owner capabilities where they provide stronger enforcement.
- No cloud/backend/account/network dependency in MVP.

## Current repository state
This pack now includes an Android Studio Kotlin/Compose skeleton under `app/`. It is intentionally a scaffold, not a claim of production-ready enforcement. Codex should finish it using `docs/CODEX_PROMPT.md`.

In addition to the brightness/volume caps, the app has a Device-Owner-only **screen-time cycle**: after a configurable amount of active screen time, the device shows an unremovable, Lock Task-based break screen (configurable duration) that a child can partially shorten by solving simple math problems, after an initial non-skippable hard-lock window. See `docs/STATUS.md` for the full feature list and remaining phases (scheduling, per-app budgets, QR provisioning).

### Development Device Owner command
After installing the debug APK on an eligible test device/user, the admin component is:

```bash
adb shell dpm set-device-owner --user current ru.makdigital.parentcontrol/.policy.AdminReceiver
```

Device Owner provisioning has Android state prerequisites. If the command is rejected, use a fresh/eligible emulator or test device and follow current Android managed-device provisioning requirements. Note: some Android versions no longer accept the `--name` option on this command; omit it if you see `Unknown option: --name`.

### Build and test
Open the repository root in Android Studio, or run:

```bash
./gradlew test lint assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

### Device Owner test setup

Use an eligible fresh emulator/device with one user, install the debug APK, and do not add an account before provisioning. Then run:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell dpm set-device-owner --user current --name "Parent Control" ru.makdigital.parentcontrol/.policy.AdminReceiver
adb shell dumpsys device_policy | grep -A4 ru.makdigital.parentcontrol
```

Android may reject provisioning on an already-managed or non-fresh user; that is a platform prerequisite, not an app success state. On a non-owner device, grant the displayed WRITE_SETTINGS permission and record OEM/background-lifetime behavior separately. The Device Owner service is system-bound and visible through the app's status card; no hidden service or INTERNET permission is used.
