**English** | [Bahasa Indonesia](id/VALIDATION.md)

# KomiKita validation

## Mihon upstream merge — 5 November 2026

- Merged upstream `ede052af1` (`Bump webgpuviewer (#4242)`) into `main` together with the privacy and reading features below. The renderer is version 57; KomiKita retains `xmlutil` 1.0.2.1 and imports the new resources explicitly.
- Completed `spotlessApply`, root `testDebugUnitTest`, root `verifySqlDelightMigration`, `:app:assembleDebug`, and `:app:lintDebug`. Full Android unit-test results: 211 passed, 3 skipped, no failures. The 3 Python/SQLite integration tests also passed.
- `:app:assembleRelease -Pdist=ci` completed, including R8. Both debug and release builds used `-Penable-rust=false`; the release build reused cached dependencies with `--offline`.
- Smoke-tested the merged debug APK on an x86_64 API 37 emulator using two original offline CBZ chapters, four pages each. The standard reader rendered a chapter to completion and retained its last page through rotation. The Continue reading carousel correctly resumed chapter 2 with WebGPU enabled.
- WebGPU 57 visibly rendered chapter 2. Its upscaler/downscaler settings loaded with their default shader names; toggling **Show all tiles together** and **Disable tiles** while reading, changing pages, and backgrounding/resuming the app worked.
- Database checks confirmed both chapters were read through page index 3, with positive reading durations and completed daily-activity records. Renderer options were restored afterward. The crash buffer was empty after the smoke test; process exit records contained only deliberate force-stops/package updates and no crash or ANR.
- Lint still reports 5 existing errors in unchanged configuration/calendar/source-preference files, plus 101 warnings. The Gradle lint task does not fail on these reports. No physical-device or arbitrary custom-shader compatibility guarantee is implied by the emulator checks.

## Privacy and reading features — 5 November 2026

The debug APK was built with native processing disabled using `-Penable-rust=false --offline`.

- Gradle completed `spotlessCheck`, `:app:assembleDebug`, `:app:testDebugUnitTest`, `:domain:testDebugUnitTest`, and `:data:verifySqlDelightMigration`.
- App tests: 55 passed, 2 skipped. Domain tests: 122 passed. No test failures.
- `python -m unittest discover -s tools/tests`: 3 SQLite integration tests passed. These exercise the actual history and widget update queries, hidden/visible category overlap, the hidden default category, reading-duration exclusion, migration 20 preserving history, completion deduplication, and foreign-key cleanup.
- Unit tests cover carousel ordering and deduplication, streak gaps, year filtering, midnight/time-zone splitting, duration conservation across DST, and diagnostic redaction.
- The x86_64 Android emulator verified device-PIN authentication, refusal to unlock without credentials, automatic locking after backgrounding, and exclusion of a hidden title that also belonged to a public category from the library, carousel, history, and recap.
- Exporting logs opened Android's share sheet with a nonempty `komikita_diagnostics.txt`. The exported demo attachment retained app version and recent logs; checks found no raw HTTP URLs, installation ID field, or hidden demo title.
- Original app screenshots were captured and inspected for both READMEs. The reproducible seed script and capture instructions are in [tools/demo](../tools/demo/README.md).
- Android lint completed, but its report contains 5 errors and 98 warnings. The errors are in unchanged files: one SDK-path escape in ignored `local.properties`, three non-observable locale reads in the existing calendar, and an AppCompat custom-button issue in source preferences. The new recap's locale issue and lock screen naming warning were corrected.

Biometric hardware, live source update notifications, and a launcher-placed widget were not exercised on a physical device. Their integration uses the existing Android biometric prompt and shared visibility queries; the actual widget queries were covered by SQLite tests. This validation did not build a release APK or re-test native processing. Daily recap activity begins with this version and is not yet included in backups.

## Historical validation — 2 November 2026

Checks recorded on 2 November 2026:

- Gradle: `spotlessCheck`, `testDebugUnitTest`, `verifySqlDelightMigration`, `assembleDebug`, and `assembleRelease`.
- 155 Android unit tests passed, with no failures or errors.
- 6 Rust tests passed. `cargo fmt --check` and `cargo clippy --all-targets -- -D warnings` were clean.
- JNI was verified on the host using the built Kotlin classes and Rust library: 100 deterministic batches matched the reference implementation.
- The Rust library was built successfully for armeabi-v7a, arm64-v8a, x86, and x86_64.
- The Android build used an available NDK 30 installation through the `rust-ndk-path` option; the project and CI default to NDK 29.
- Large chapter-number tests also covered the Kotlin fallback when the host native library was unavailable.
- Category tests covered comics belonging to both hidden and visible categories, restoring visibility, and unrelated category IDs.

This is a historical record of those checks, not validation of subsequent work-in-progress changes.

The locally built release APK was used for testing. For official distribution, use a KomiKita keystore as described in the [contributor guide](../CONTRIBUTING.md).

No device or emulator was connected during these checks. UI behavior on a physical device and battery impact have not been measured. See the [performance notes](PERFORMANCE.md) for the procedure and limits of the conclusions.
