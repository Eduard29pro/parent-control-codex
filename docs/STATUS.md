# Status

- [x] Product scope and architecture defined
- [x] Android Studio skeleton created
- [x] Gradle Kotlin DSL + Compose scaffold
- [x] DataStore settings repository
- [x] PBKDF2 PIN storage scaffold
- [x] DeviceAdminReceiver + Device Owner controller scaffold
- [x] Immediate brightness/media-volume clamping
- [x] BOOT_COMPLETED reapply scaffold
- [x] Parent settings UI scaffold
- [x] PIN confirmation + failed-attempt throttling
- [x] Continuous event-driven cap enforcement engine
- [x] Correct adaptive-brightness handling
- [x] Permission/setup UX completion
- [x] DeviceAdminService lifecycle for Device Owner mode
- [x] Unit tests for PIN validation and conversion helpers
- [x] Gradle wrapper added; `assembleDebug`, unit tests, and lint all pass
- [x] Android 36 emulator verification: PIN setup/unlock, settings persistence, WRITE_SETTINGS flow, brightness/volume clamp-on-save all confirmed manually
- [x] Real-device (emulator) reboot verification: BootReceiver reapplies the configured caps after `adb reboot` without opening the UI

## Screen-time cycle (Phase 1)
- [x] Configurable active-use / break cycle (`ScreenTimeSettings`, `ScreenTimeCycleState`, `ScreenTimeRepository`), Device-Owner-only
- [x] Screen-on usage tracking via `EnforcementAdminService` (dynamic SCREEN_ON/OFF receiver + 15s ticker), self-healing screen-on anchor
- [x] Lock Task-based `LockActivity`: unremovable (no home/recents), survives reboot via `BootReceiver` + `ScreenTimeEngine.reconcileOnStart()`
- [x] Math challenge (addition, 1-10) reduces remaining break time; hard-lock window and per-attempt cooldown prevent guessing
- [x] Parent PIN emergency override (long-press lock screen title) ends the current lock without disabling the feature
- [x] Settings UI card with 5 configurable sliders + master switch, gated on Device Owner status
- [x] Unit tests for `MathChallenge` and `ScreenTimeMath` (phase transitions, boundaries, reduction clamping)
- [x] Manually verified end-to-end on the Android 36 emulator: auto-lock on budget exhaustion, Lock Task escape resistance, hard-lock gating, correct-answer reduction, wrong-PIN rejection, correct-PIN override, natural break completion, and **real `adb reboot` while locked** (lock reappears automatically with the correct remaining time, no UI opened)
- [ ] Day-of-week / time-window scheduling (Phase 2)
- [ ] Per-app-category budgets (Phase 3)
- [ ] QR-code Device Owner provisioning for install without ADB (Phase 4)
