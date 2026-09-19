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
