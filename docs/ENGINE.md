# Enforcement Engine — source of truth

## Product invariant
When limits are enabled, the child may LOWER brightness/media volume. A value ABOVE the parent cap must be clamped back as quickly as Android permits. Never use a policy that mutes audio or freezes a control if that breaks this invariant.

## Brightness engine
1. Parent cap is 1..100.
2. Convert to the platform brightness domain used by the chosen API; keep conversion in one helper and test boundaries.
3. Observe brightness changes with a `ContentObserver` on `Settings.System.SCREEN_BRIGHTNESS` while enforcement process is alive.
4. On change, if current > cap, clamp; if <= cap, do nothing.
5. Device Owner may write allowed system settings through `DevicePolicyManager.setSystemSetting` where supported; fallback to `Settings.System` only when `Settings.System.canWrite` is true.
6. DO NOT enable `DISALLOW_CONFIG_BRIGHTNESS` in default numeric-cap mode because it prevents lowering as well as raising. Reserve it only for a future explicit lock/fixed mode.
7. Adaptive brightness: MVP should switch to manual brightness only when required for deterministic cap behavior, document this UX, and preserve/restore prior mode when limits are disabled if practical.

## Media volume engine
1. MVP scope is `AudioManager.STREAM_MUSIC` only.
2. Convert parent percent to stream steps using `getStreamMaxVolume`.
3. Observe volume changes using supported callbacks/observers available on the target OS. Avoid hidden APIs in release code.
4. If Android exposes no reliable public callback for a given version, use a low-cost fallback that is documented and battery-conscious. Do not busy-loop.
5. `UserManager.DISALLOW_ADJUST_VOLUME` is NOT the numeric-cap implementation. Android documents that restriction as disallowing global volume adjustment and muting global volume; do not use it for this MVP.

## Lifetime
- Apply immediately after Save.
- Reapply on `BOOT_COMPLETED` and `MY_PACKAGE_REPLACED`.
- Prefer DPC-owned lifecycle mechanisms. `DeviceAdminService` is allowed for a device/profile owner on API 26+ if it materially improves reliable event observation; it must use `BIND_DEVICE_ADMIN` and the documented `ACTION_DEVICE_ADMIN_SERVICE` intent filter.
- If a foreground service is required as fallback, it must be visible and documented; no stealth service.

## Idempotence
Every enforcement operation must be safe to call repeatedly. Never write if current value is already <= cap.
