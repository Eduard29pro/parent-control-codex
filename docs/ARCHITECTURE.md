# Architecture

## Stack
Kotlin, Gradle Kotlin DSL, Jetpack Compose Material 3, AndroidX Lifecycle/ViewModel, DataStore Preferences. Prefer standard crypto APIs; do not add a large security library solely for PIN hashing.

minSdk 29. compile/target SDK: latest stable installed SDK; code must account for newer Android background restrictions.

## Packages
`ui`, `data`, `security`, `policy`, `boot`, `service` (only if required), `model`.

## State
`LimitSettings(enabled, maxBrightnessPercent, maxMediaVolumePercent)`.
Repository backed by DataStore.

PIN storage: random salt + PBKDF2-HMAC-SHA256 derived hash, constant-time comparison. Never persist plaintext PIN. Rate-limit repeated failed unlock attempts in UI/session logic.

## Brightness
Use system brightness only when permitted. Convert percent to Android brightness range. Account for automatic/adaptive brightness: enforcement should switch/control the relevant system mode only when required and explain behavior in UI. Required permissions/capabilities must be checked before writes.

Device Owner policy should be used where current Android APIs provide brightness configuration restrictions. Do not assume a restriction alone implements a numeric maximum: the app's own enforcement remains responsible for the cap.

## Volume
MVP controls STREAM_MUSIC/media volume. Convert percent to `AudioManager.getStreamMaxVolume(STREAM_MUSIC)` steps. Numeric maximum enforcement is app logic. Device Owner restriction may disable user volume adjustment only for a future `lock` mode; do not use that restriction for the default cap mode because the product requires lowering to remain possible.

## Enforcement
Create a single `LimitsEnforcer` interface with `applyNow()` and idempotent clamp methods. UI save calls it immediately.

Use the least invasive Android mechanism that can reliably observe/reapply changes. Avoid tight polling loops. Prefer observers/receivers where available. If a foreground service is truly required for numeric cap enforcement on supported Android versions, make it explicit and visible with a persistent notification and document battery implications. Do not hide it.

## Reboot
Register a BootReceiver for `BOOT_COMPLETED` (and locked boot only if storage design supports it safely). On boot, load persisted settings and reapply. Respect modern restrictions on starting foreground services from boot; schedule permissible follow-up work if necessary.

## Device Owner
Implement `DeviceAdminReceiver` and DevicePolicyManager integration. Provide a setup/status screen and ADB provisioning instructions for development. Device Owner provisioning generally requires an eligible/fresh device; never fake success when not provisioned.

## Security boundaries
PIN protects app UI; it is not a substitute for Android OS ownership/security. Do not create overlays that impersonate system UI. Do not collect personal data. INTERNET permission is unnecessary and should not be requested.
