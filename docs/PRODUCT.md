# Product specification

## Product
Working name: Parent Control Limits.

## Problem
A parent wants to cap a child's device screen brightness and sound level. The child may lower either value, but must not be able to retain a value above the parent's configured maximum. Parent settings are protected by PIN and survive reboot.

## MVP
1. First launch: create 4–8 digit parent PIN and confirm it.
2. Subsequent launch: PIN unlock.
3. Control screen:
   - master switch `Restrictions enabled`;
   - maximum brightness slider 1–100%;
   - maximum media-volume slider 0–100%;
   - Save button;
   - status showing whether Device Owner/admin capabilities are active.
4. When enabled, immediately clamp current brightness/volume to maxima.
5. If the user later raises a controlled value over the maximum, return it to the maximum as reliably and quickly as Android permits.
6. Values below maximum remain allowed.
7. Persist configuration and reapply after boot.
8. PIN change is available after authentication.

## Out of scope
Remote parent phone, backend, accounts, app blocking, screen-time quotas, GPS, content monitoring, calls/messages monitoring, hidden operation, uninstall resistance beyond documented Android Device Owner policy capabilities.

## UX
Language: Russian first. Material 3 / Compose. One-column phone UI. Calm neutral appearance. No childish graphics.

Screens:
- Setup PIN
- Unlock
- Limits
- Change PIN
- Device management/setup info

Limits screen layout:
Top app bar: `Ограничения устройства`
Status card: `Защита активна` / `Требуется настройка управления устройством`
Switch: `Ограничения включены`
Card `Яркость экрана`: percentage + slider + helper `Можно уменьшать яркость, но не выше установленного значения.`
Card `Громкость мультимедиа`: percentage + slider + helper.
Primary button: `Сохранить ограничения`
Secondary navigation: `Изменить PIN`, `Настройка устройства`.

Accessibility: minimum touch targets, semantic labels, percentage announced as text, adequate contrast, no color-only status.
