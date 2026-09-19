# CODEX EXECUTION CONTRACT

You are implementing the Android application described by this repository. Treat the Markdown documents as the product and engineering specification.

## Operating rules
1. Read this file and all files under `docs/` before editing code.
2. Do not redesign the product unless an Android platform constraint makes a requirement impossible. If so, implement the closest safe behavior and document the limitation in `docs/DECISIONS.md`.
3. Work autonomously through the entire implementation. Do not stop after scaffolding and do not ask for routine confirmations.
4. Keep the solution small. Avoid unnecessary abstractions, libraries, services, and token-heavy rewrites.
5. Prefer Android/Jetpack APIs over third-party dependencies.
6. After each milestone, compile/test. Fix failures before continuing.
7. Never store the parent PIN in plaintext.
8. Do not implement stealth, concealment, surveillance, keylogging, location tracking, message interception, or data exfiltration.
9. The app must clearly identify itself as parental-control software.
10. Finish only when acceptance criteria in `docs/ACCEPTANCE.md` pass or remaining platform limitations are explicitly documented.

## Build loop
- Inspect repository.
- Implement smallest coherent milestone.
- Run unit tests / lint / assembleDebug as applicable.
- Fix errors.
- Continue to next milestone.
- At the end run the full verification checklist.

## Token/efficiency rules
- Do not repeatedly reread large files unless necessary.
- Do not generate long explanations during implementation.
- Reuse existing components.
- Keep source files focused and names explicit.
- Avoid speculative features not listed in MVP.
- Maintain `docs/STATUS.md` with short checkbox updates rather than verbose progress logs.

## Definition of done
A clean checkout opens in Android Studio, builds, launches, supports initial PIN setup, authenticated settings changes, persists limits, applies brightness/volume limits, restores enforcement after reboot to the extent permitted by Android, and contains setup documentation for Device Owner testing.
