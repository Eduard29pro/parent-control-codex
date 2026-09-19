Implement and finish this Android project autonomously.

FIRST read, in this order: `CODEX.md`, `docs/PRODUCT.md`, `docs/ENGINE.md`, `docs/PLATFORM_CONSTRAINTS.md`, `docs/ARCHITECTURE.md`, `docs/UI_DESIGN.md`, `docs/ACCEPTANCE.md`, `docs/TEST_PLAN.md`, `docs/STATUS.md`, `docs/DECISIONS.md`.

A Kotlin/Compose skeleton already exists. Do not restart the project from scratch unless it is genuinely broken. Treat `docs/ENGINE.md` as the source of truth for runtime behavior.

Priority:
1. Make Gradle sync/build work with the stable Android SDK/JDK available in the environment. Adjust dependency/plugin versions only if necessary and record material changes.
2. Finish first-run PIN setup with confirmation, unlock errors, and failed-attempt throttling. Keep plaintext PIN out of storage/logs.
3. Refactor percentage conversion into testable pure helpers.
4. Implement reliable numeric-cap enforcement: child may lower values; values above the parent cap are clamped promptly. Do not use `DISALLOW_CONFIG_BRIGHTNESS` or `DISALLOW_ADJUST_VOLUME` as the default cap mechanism.
5. Complete Device Owner integration and documented setup. Prefer documented DPC APIs. If `DeviceAdminService` is the best supported lifecycle mechanism, implement it correctly; otherwise document the chosen mechanism.
6. Complete boot/package-replaced restore behavior.
7. Complete permission/status UX and move all user strings to resources.
8. Add tests from `docs/TEST_PLAN.md`.
9. Run tests, lint, and `assembleDebug`; fix all build failures you can reproduce.
10. Update `docs/STATUS.md`, `docs/DECISIONS.md`, and README with exact run/provision/test commands.

Do not pause for routine approval. Do not add unrelated features, backend, analytics, accounts, internet permission, tracking, app blocking, location, or remote control. Do not implement stealth behavior.

Before finishing, inspect the diff for accidental complexity and remove dead code. Final response should be short: what was implemented, exact build/test result, APK path if built, and any Android/OEM limitation that remains.
