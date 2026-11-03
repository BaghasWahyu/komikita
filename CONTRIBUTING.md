**English** | [Bahasa Indonesia](CONTRIBUTING.id.md)

# Contributing to KomiKita

Report bugs and suggest features through [KomiKita Issues](https://github.com/BaghasWahyu/komikita/issues). Include the app version, Android version, reproduction steps, and logs with personal information removed. Reports and contributions are welcome in English or Bahasa Indonesia.

## Setup

- Android Studio and JDK 21 (Java 17 bytecode target).
- SDK versions from `gradle/mihon.versions.toml`: compile SDK 37.2, target SDK 36, minimum SDK 26; NDK 29.0.14206865.
- Stable Rust, Android targets, and cargo-ndk for native builds; see the [native module guide](docs/NATIVE.md).
- Add `sdk.dir=...` to `local.properties` or set `ANDROID_HOME`.

Use the project's Gradle Wrapper. On Windows, replace `./gradlew` with `gradlew.bat`.

```sh
./gradlew assembleDebug
./gradlew spotlessCheck testDebugUnitTest verifySqlDelightMigration
```

To build without the Rust toolchain, use `-Penable-rust=false`; the Kotlin implementation remains available.

## Distribution

The application ID is `app.komikita`; debug, FOSS, nightly, and benchmark variants have separate suffixes. Configure a KomiKita keystore through `keystore.properties`. Do not commit keystores or credentials.

The GitHub release workflow enables the app updater for both regular and FOSS APKs with `-Penable-updater=true`. Local and CI builds keep it disabled unless this flag is supplied. The update repository is `BaghasWahyu/komikita`.

Firebase is not included by default. If needed, provide your own `app/google-services.json` covering every application ID you build, then use `-Pinclude-telemetry=true`. Data collection follows the user's consent settings.

The release workflow creates draft releases in the KomiKita repository. Configure the `SIGNING_KEY`, `KEY_STORE_PASSWORD`, `ALIAS`, and `KEY_PASSWORD` secrets before running it. For each new release, increase `versionName` and `versionCode` in `app/build.gradle.kts`, use a matching tag such as `v1.0.2`, and keep the same signing key. Publish the draft after checking its APK assets; the updater checks the latest published stable release, so drafts and prereleases are not offered. Users of older APKs with the updater disabled must install an updater-enabled APK manually once.

## Translations and compatibility

English app strings are in `i18n/src/commonMain/moko-resources/base/`; Indonesian strings are in `i18n/src/commonMain/moko-resources/in/`. Existing upstream translations are retained. See the [translation guide](i18n/README.md).

English is the primary language for project documentation. Keep the linked Indonesian version in sync when changing a guide. See the [documentation index](docs/README.md) for both versions.

Source namespaces, Tachiyomi extension contracts, ComicInfo metadata, the backup format, and OAuth callbacks registered with providers are retained. See [project identity and compatibility](docs/BRANDING.md).

## Credits

Retain the [license](LICENSE), Mihon/Tachiyomi credits, and contributor copyright notices when modifying or distributing the fork.
