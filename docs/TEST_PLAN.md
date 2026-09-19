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
