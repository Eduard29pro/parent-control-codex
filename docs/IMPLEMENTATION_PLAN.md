# Implementation plan

## M1 — Project foundation
Create Android Studio project, Compose theme, navigation/state, DataStore models/repository. Build.

## M2 — Parent authentication
First-run PIN creation/confirmation, PBKDF2 storage, unlock screen, session authentication, change PIN. Unit-test hashing/verification and validation.

## M3 — Limits UI
Implement Russian Material 3 limits screen, sliders, master switch, validation, save state, management-status card. Add Compose previews where useful.

## M4 — Controllers
Implement brightness controller, media-volume controller, conversions and permission/capability checks. Implement `LimitsEnforcer`. Unit-test pure conversion/clamping functions.

## M5 — Device administration
Add DeviceAdminReceiver XML/manifest setup and DevicePolicyManager wrapper. Surface actual owner/admin status. Add only policies justified by the specification.

## M6 — Continuous enforcement
Implement event-driven re-clamping where Android permits. If reliable numeric-cap enforcement requires a foreground service, implement minimal visible service with notification channel; no high-frequency busy loop. Verify child can lower values but raising above cap is corrected.

## M7 — Boot persistence
BootReceiver loads configuration and reapplies. Handle platform-specific service/work restrictions. Test simulated reboot/receiver invocation.

## M8 — Hardening
No plaintext PIN, no INTERNET permission, lifecycle correctness, error/status messages, accessibility labels, edge cases for zero/max volume and brightness ranges.

## M9 — Verification
Run tests, lint, `assembleDebug`. Update README with exact build/provision/test steps and update STATUS/DECISIONS.
