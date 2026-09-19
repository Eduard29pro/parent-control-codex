# UI / UX design

## Visual direction
Calm parental utility, not a surveillance app. Material 3, light-first, large touch targets, no decorative complexity. One accent color from Material dynamic/default theme; do not hard-code a custom palette until branding exists.

## Screen 1 — first launch / PIN setup
- App title: Parent Control.
- Explanation: PIN protects parent settings on this device.
- 4–8 numeric digits for MVP.
- Enter + confirm PIN before save (Codex must add confirmation; scaffold currently has one field only).
- Never show or log the PIN.

## Screen 2 — unlock
- Numeric PIN field.
- Error state on wrong PIN.
- Rate limit repeated attempts (simple exponential/session delay is sufficient for MVP).
- Settings are not visible until unlock.

## Screen 3 — controls
Header: `Ограничения устройства`.
Status card:
- Device Owner: green/positive status and `Защищённый режим`.
- Not Device Owner: neutral warning and short setup instruction link/section.

Controls:
- Master switch `Ограничения включены`.
- Brightness card: icon, `Максимальная яркость`, current cap `%`, slider 1–100, helper `Можно уменьшить, выше лимита — нельзя`.
- Volume card: icon, `Максимальная громкость медиа`, current cap `%`, slider 0–100, same helper.
- Primary full-width button `Сохранить и применить`.
- Success snackbar after persistence + apply.

## Permission/setup UX
If the app is not Device Owner and needs WRITE_SETTINGS, show a clear button that opens `ACTION_MANAGE_WRITE_SETTINGS`. Never dead-end with explanatory text only.

## Accessibility
Content descriptions for icons, readable contrast, sliders expose percentage semantics, minimum 48dp touch targets, Russian strings in resources (not hard-coded in composables in final implementation).
