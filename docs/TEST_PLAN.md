# Test plan

## Unit
- Percent-to-brightness boundaries: 1, 40, 100.
- Percent-to-volume-step boundaries: 0, 30, 100 for representative stream maxima.
- Settings coercion.
- PIN: correct PIN succeeds, wrong PIN fails, no plaintext stored.

## Manual — unlocked parent flow
1. Fresh install -> create + confirm PIN.
2. Reopen -> settings hidden behind PIN.
3. Set brightness 40%, volume 30%, save.
4. Values persist after process death.

## Manual — child behavior
- Lower brightness below 40% -> remains lower.
- Raise brightness above 40% -> returns to <=40% promptly.
- Lower media volume below 30% -> remains lower.
- Raise media volume above 30% with hardware key and system slider -> returns to <=30% promptly.

## Lifecycle
- Reboot with limits enabled -> limits restored without opening UI.
- Update/reinstall-over-existing debug APK -> settings retained where Android allows and enforcement reapplied.
- Disable limits -> app stops clamping.

## Modes
- Test Device Owner path.
- Test non-owner fallback and WRITE_SETTINGS permission UX.
- Record OEM-specific limitations rather than hiding them.

## Screen-time cycle (Device Owner only)
- Set small active/break/hard-lock/reduction/cooldown values for fast iteration; enable the feature.
- Use the device past the active budget -> lock screen appears unprompted; home/recents/notification shade are all blocked.
- During the hard-lock window, confirm no challenge is offered.
- After the hard-lock window, confirm the challenge appears; a correct answer reduces the remaining break by the configured amount; an incorrect answer is rejected and rapid resubmission is blocked until the cooldown elapses.
- Let the break reach zero naturally -> automatic return to normal use.
- Long-press the lock screen title -> PIN field appears; wrong PIN is rejected with an error and the lock stays; correct PIN ends the current lock only (feature stays enabled).
- Turn the screen off during active use -> that time is not counted against the budget.
- `adb reboot` while locked -> the lock reappears automatically after boot with the correct remaining time, without opening the app UI.
- `adb reboot` while active (not locked) -> no lock appears; existing brightness/volume reapply is unaffected.
- On a non-Device-Owner build, confirm the settings UI shows "requires Device Owner" and the feature cannot be enabled.
