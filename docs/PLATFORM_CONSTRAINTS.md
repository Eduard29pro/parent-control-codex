# Android platform constraints

1. There is no standard Android policy API for a user-adjustable numeric maximum brightness/volume slider. The app must enforce a cap by observing and clamping changes.
2. `DISALLOW_CONFIG_BRIGHTNESS` (API 28+) blocks brightness configuration; it is useful for a fixed/locked mode but conflicts with the MVP requirement that the child may lower brightness.
3. `DISALLOW_ADJUST_VOLUME` is not a numeric maximum and must not be used for MVP cap mode.
4. Device Owner provisioning is a managed-device capability. Development provisioning with `adb shell dpm set-device-owner ...` requires an eligible device/user state; Codex must document the exact tested prerequisites instead of pretending provisioning always succeeds.
5. Device Owner can use documented device-policy APIs that ordinary apps cannot. Prefer those APIs where they preserve the product invariant.
6. Background execution differs by Android version/OEM. Verification must include at least emulator API 29, a recent API (35/36 if installed), and a real target phone before calling reboot persistence production-ready.
