**English** | [Bahasa Indonesia](id/VALIDATION.md)

# KomiKita 1.0.0 validation

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
