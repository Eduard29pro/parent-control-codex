# Engineering decisions

Record only decisions that change or clarify the specification.

Initial decisions:
- Default sound control is media volume (`STREAM_MUSIC`), not ringtone/alarm/call volume.
- Default behavior is a numeric cap, not a total prohibition on adjustment.
- Child may lower controlled values.
- No network/backend in MVP.
- Device Owner is the preferred managed-device mode, but the UI must truthfully show whether it is provisioned.

- 2026-09-19: Numeric cap mode must allow the child to LOWER brightness and media volume. Therefore `DISALLOW_CONFIG_BRIGHTNESS` and `DISALLOW_ADJUST_VOLUME` are not used as the default enforcement mechanism.
- 2026-09-19: MVP audio scope is media stream only, not ringtone/alarm/call volume.
- 2026-09-19: No INTERNET permission, backend, account, telemetry, location, or remote control in MVP.
- 2026-09-19: Numeric cap observation uses a documented `DeviceAdminService` with public `ContentObserver`s for Device Owner mode. Non-owner mode relies on WRITE_SETTINGS and is subject to Android background lifetime limits.
- 2026-09-19: Adaptive brightness is switched to manual while limits are enabled and the previous automatic mode is restored when limits are disabled, when the relevant system setting is writable.
- 2026-09-20: Added the Gradle wrapper (pinned to Gradle 8.9, compatible with AGP 8.7.3) since it was missing and the project had never actually been compiled. Set explicit `sourceCompatibility`/`targetCompatibility`/`jvmTarget` to Java 17 to fix a Java/Kotlin JVM-target mismatch. Fixed real compile errors in `ParentControlRoot.kt` (wrong `isValidPin` argument type, `stringResource` calls from non-composable `onClick` lambdas, missing `@OptIn(ExperimentalMaterial3Api::class)`). Fixed `LimitsMath.brightnessValueFromPercent` to coerce out-of-range percentages to the true minimum raw value (was clamping negative percent to 1%, i.e. raw 3, instead of raw 1).
- 2026-09-20: Manually verified on an Android 36 (Google Play) emulator: PIN setup/confirm/unlock, WRITE_SETTINGS permission flow, and save-triggered clamping of both screen brightness (255 -> 102 for a 40% cap) and media volume (11/15 -> 7/15 for a 45% cap) all match `LimitsMath` exactly. Also verified real reboot persistence (`adb reboot`): `BootReceiver` reapplied the brightness cap without opening the UI. Note: on this Android version, manifest-registered `BOOT_COMPLETED` receivers can sit in the OS's broadcast "offload" queue for several minutes under system load before delivery — this is a platform battery-optimization behavior (worse on a resource-constrained host), not an app defect. Also confirmed a freshly reinstalled (`pm install -r`) app is put back into Android's "stopped" state and will not receive `BOOT_COMPLETED`/`MY_PACKAGE_REPLACED` until launched once — expected platform behavior, not a bug.
