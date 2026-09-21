# Acceptance criteria

- [ ] Fresh install asks parent to create and confirm a 4–8 digit PIN.
- [ ] PIN is not stored in plaintext.
- [ ] Wrong PIN cannot open settings; repeated attempts are rate-limited.
- [ ] Parent can set brightness max and media-volume max as percentages.
- [ ] Save persists values and immediately clamps current values.
- [ ] Child can reduce brightness and media volume below maxima.
- [ ] Raising either controlled value above its max is corrected as reliably as supported Android APIs allow.
- [ ] Reboot does not erase settings; enforcement is reapplied after boot.
- [ ] App reports actual Device Owner/admin status.
- [ ] Device Owner development provisioning instructions are documented.
- [ ] App requests no INTERNET permission.
- [ ] Any foreground enforcement is visible to the user.
- [ ] `assembleDebug` succeeds.
- [ ] Unit tests succeed.
- [ ] Lint has no unaddressed fatal errors.

## Screen-time cycle (Phase 1, Device Owner only)
- [ ] Feature is unavailable (clearly labeled "requires Device Owner") when not provisioned as Device Owner.
- [ ] Active-use time, break duration, per-answer reduction, hard-lock duration, and attempt cooldown are all parent-configurable.
- [ ] Screen is locked automatically once the configured active-use budget is exhausted, with no user action required.
- [ ] The lock screen cannot be dismissed, minimized, or bypassed via home, recents, or the notification shade.
- [ ] During the hard-lock window, no challenge is offered and the break cannot be shortened.
- [ ] After the hard-lock window, a math challenge is offered; a correct answer reduces (does not fully clear) the remaining break; wrong answers are rejected with a clear message; repeated attempts are rate-limited.
- [ ] The break ends automatically and the lock is released once the remaining time reaches zero.
- [ ] A parent can end the current lock early via a hidden PIN-gated control on the lock screen; the feature otherwise remains enabled.
- [ ] Reboot while locked restores the lock screen automatically, with the correct remaining time, without opening the app UI.
- [ ] Time spent with the screen off is not counted against the active-use budget.
