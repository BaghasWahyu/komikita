**English** | [Bahasa Indonesia](README.id.md)

<div align="center">

<img src=".github/assets/logo.png" alt="KomiKita logo" width="112" />

# KomiKita

**Your comics, always close.**

An open-source manga, comic, and webtoon reader for Android.<br />
Organize your collection, read offline, and pick up where you left off.

[![Latest release](https://img.shields.io/github/v/release/BaghasWahyu/komikita?label=release&color=0f766e)](https://github.com/BaghasWahyu/komikita/releases/latest)
[![Android 8.0 or later](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](#download-and-install)
[![Build and test](https://github.com/BaghasWahyu/komikita/actions/workflows/build.yml/badge.svg)](https://github.com/BaghasWahyu/komikita/actions/workflows/build.yml)
[![Apache License 2.0](https://img.shields.io/badge/License-Apache_2.0-blue)](LICENSE)

[**Download the latest release**](https://github.com/BaghasWahyu/komikita/releases/latest) · [User guide](docs/USER_GUIDE.md) · [Changelog](CHANGELOG.md) · [Report an issue](https://github.com/BaghasWahyu/komikita/issues)

</div>

KomiKita is a fork of [Mihon](https://github.com/mihonapp/mihon), continuing the Tachiyomi codebase with its own identity and additions to library organization, reading continuity, and app updates. It has a separate application ID and can be installed alongside Mihon.

## Contents

- [Features](#features)
- [Download and install](#download-and-install)
- [Getting started](#getting-started)
- [App updates](#app-updates)
- [Moving from Mihon](#moving-from-mihon)
- [Privacy and content sources](#privacy-and-content-sources)
- [Build from source](#build-from-source)
- [Project structure](#project-structure)
- [Documentation](#documentation)
- [Contributing and support](#contributing-and-support)
- [Credits and license](#credits-and-license)

## Features

| Area | What you can do |
| --- | --- |
| **Reading** | Switch between page and webtoon reading modes; customize direction, themes, and reader controls. |
| **Library** | Organize comics into categories, search and filter your collection, and choose grid or list layouts. |
| **Continue reading** | Resume the most recently opened comic while it has unread chapters. Once caught up, move to an unread comic with the newest chapter update in the visible collection. |
| **Offline and local content** | Download chapters for offline reading and read comics stored on your device. |
| **Visibility controls** | Hide categories and their comics, or hide extensions and their sources from Browse without uninstalling them. |
| **Source badges** | Optionally show extension icons on comics in your library. |
| **Progress and tracking** | Keep reading history and progress, schedule library updates, and connect supported services such as AniList and MyAnimeList. |
| **Backup and restore** | Back up your library, reading progress, and settings to a location you choose. |
| **Reader battery saver** | Prefer display refresh rates up to 60 Hz while reading, with the option to disable this preference. |
| **App updates** | Check published KomiKita releases, read release notes, and download a newer compatible APK from the app. |

The [user guide](docs/USER_GUIDE.md) explains hidden categories, hidden extensions, source badges, battery saver, and migration. Battery impact depends on the device and reading conditions; see the [performance notes](docs/PERFORMANCE.md) for measurements and their limits.

## Download and install

KomiKita requires **Android 8.0 (API 26) or later**. Download APKs from [GitHub Releases](https://github.com/BaghasWahyu/komikita/releases/latest).

### Choose an APK

In the filenames below, `X.Y.Z` is the release version.

| APK filename | Choose this for |
| --- | --- |
| `komikita-vX.Y.Z.apk` | **Universal APK — recommended if you are unsure.** |
| `komikita-arm64-v8a-vX.Y.Z.apk` | ARM64 phones and tablets. |
| `komikita-armeabi-v7a-vX.Y.Z.apk` | Devices using 32-bit ARM. |
| `komikita-x86-vX.Y.Z.apk` | Devices or emulators using 32-bit x86. |
| `komikita-x86_64-vX.Y.Z.apk` | Devices or emulators using 64-bit x86. |
| `komikita-vX.Y.Z-foss.apk` | The separate FOSS variant, packaged as a universal APK. |

The standard app uses `app.komikita`; FOSS uses `app.komikita.foss`. They can coexist and keep separate app data. An APK from one variant does not update the other. Both variants from the GitHub release workflow include the app updater, and neither includes Firebase by default.

### Install the app

1. Open the latest release and download the APK you selected.
2. Open the APK from your browser or file manager.
3. Allow installation from that app when Android asks, then confirm installation.
4. Launch KomiKita and complete the initial setup.

To update an existing installation, install the newer APK with the **same variant** over the existing app. Keep the app installed to retain its data.

## Getting started

1. **Choose a storage folder.** Select a dedicated folder during setup for downloads, backups, and other app files.
2. **Add something to read.** Use local comics or configure compatible source extensions and their repositories separately.
3. **Build your library.** Add titles to your collection, create categories, and adjust the display and filters.
4. **Read your way.** Choose a reader mode, set reading direction and controls, or download chapters before going offline.
5. **Keep a backup.** Configure backup creation in the app's data and storage settings before moving devices or changing installations.

KomiKita does not bundle comics or provide its own content sources. Availability and behavior depend on your local files and configured extensions.

## App updates

Official GitHub release APKs check for updates when the app opens and every six hours while it remains in use. To check manually, open:

**More → About → Check for updates**

When an update is available, KomiKita displays its release notes and offers to download the APK. Android asks you to confirm the installation.

Only a **published stable release** with a **higher version** and a **compatible APK** is offered. Drafts, prereleases, and the version already installed do not appear as new updates. Version `1.0.6` added a fresh network check for manual update requests to avoid reusing cached release information.

If an older build still shows **No new updates**, compare the installed version in **About** with [the latest GitHub release](https://github.com/BaghasWahyu/komikita/releases/latest). You can install the newer APK manually using the same variant. Local and CI builds have the updater disabled unless explicitly enabled during the build.

See [Checking for app updates](docs/USER_GUIDE.md#checking-for-app-updates) for more details.

## Moving from Mihon

KomiKita can be installed alongside Mihon, so you can transfer your library while keeping the original app available.

1. Create a backup in Mihon.
2. Install KomiKita and select its storage folder.
3. Restore the backup through KomiKita's data and storage settings.
4. Check your library, extensions, reading progress, and tracker connections. Extensions and service logins may need to be configured again.

Read [Moving from Mihon](docs/USER_GUIDE.md#moving-from-mihon) for the migration guide and [project identity and compatibility](docs/BRANDING.md) for details about the fork.

## Privacy and content sources

Library data, reading progress, preferences, and tracker credentials are stored on your device. Backups are saved to the location you choose.

Default builds omit Firebase Analytics and Crashlytics. Content sources, extensions, trackers, and DNS providers receive the requests needed for their features. Release APKs also contact GitHub to check for and download app updates.

Read the [privacy policy](docs/PRIVACY.md) for the full description of data storage, network requests, and optional telemetry.

## Build from source

KomiKita uses **Kotlin**, **Jetpack Compose**, **SQLDelight**, and **WorkManager**. An optional **Rust** module accessed through JNI processes chapter data, with a Kotlin fallback.

### Requirements

| Tool | Project configuration |
| --- | --- |
| Android Studio | Use an installation compatible with the project's Android Gradle Plugin. |
| JDK | CI starts with JDK 21 from [`.github/.java-version`](.github/.java-version). The [Gradle daemon criteria](gradle/gradle-daemon-jvm.properties) select JetBrains JDK 25; Gradle can provision the matching daemon JVM. Java bytecode targets version 17. |
| Android SDK | Compile SDK **37.2**, target SDK **36**, minimum SDK **26**. |
| Android NDK | **29.0.14206865** for native builds. |
| Gradle | Use the included wrapper; no separate Gradle installation is required. |
| Rust, optional | Stable Rust, Android targets, and `cargo-ndk` for builds that include the native module. |

The [version catalog](gradle/mihon.versions.toml), [Gradle wrapper configuration](gradle/wrapper/gradle-wrapper.properties), and [CI workflow](.github/workflows/build.yml) are the sources of truth for build requirements.

### Quick build with the Kotlin fallback

```sh
git clone https://github.com/BaghasWahyu/komikita.git
cd komikita
```

Configure the Android SDK using `ANDROID_HOME`, or create an untracked `local.properties` file at the repository root:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

Then build without needing the Rust toolchain:

```sh
./gradlew assembleDebug -Penable-rust=false
```

On Windows, replace `./gradlew` with `.\gradlew.bat`. Debug APKs are written to `app/build/outputs/apk/debug/` and use the separate application ID `app.komikita.dev`.

### Build with native processing

Follow the [native module guide](docs/NATIVE.md) to install Rust, the Android targets, and `cargo-ndk`, then run:

```sh
./gradlew assembleDebug
```

For another installed NDK, pass `-Prust-ndk-path=/path/to/ndk`.

### Run checks

```sh
./gradlew spotlessCheck testDebugUnitTest verifySqlDelightMigration -Penable-rust=false
```

Omit `-Penable-rust=false` to include the native build when its toolchain is configured. The [contributor guide](CONTRIBUTING.md) covers signing, release variants, translations, and release-note generation. Configure release signing before distributing APKs; updating an existing installation requires the same application ID and signing key.

## Project structure

| Path | Responsibility |
| --- | --- |
| [`app/`](app) | Compose UI, reader, downloads, tracking, backup, and application services. |
| [`domain/`](domain) | Models, use cases, and repository contracts. |
| [`data/`](data) | Database access, repository implementations, and release metadata. |
| [`core/`](core) | Shared utilities, networking, archives, dependency injection, and the JNI bridge. |
| [`native/`](native) | Rust chapter-processing implementation. |
| [`source-api/`](source-api), [`source-local/`](source-local) | Extension contracts and local content support. |
| [`presentation-core/`](presentation-core), [`presentation-widget/`](presentation-widget) | Shared presentation components and widgets. |
| [`i18n/`](i18n), [`icons/`](icons) | Translations and icon assets. |

See [project structure and data flow](docs/PROJECT_ANALYSIS.md) for the architecture overview.

## Documentation

| Guide | What it covers |
| --- | --- |
| [User guide](docs/USER_GUIDE.md) | Library visibility, extension icons, battery saver, updates, and migration. |
| [Changelog](CHANGELOG.md) | Version-specific features and fixes. |
| [Contributing](CONTRIBUTING.md) | Development setup, signing, translations, and release preparation. |
| [Privacy policy](docs/PRIVACY.md) | Local data, network requests, and telemetry. |
| [Native module](docs/NATIVE.md) | Rust toolchain, JNI integration, and Kotlin fallback. |
| [Performance notes](docs/PERFORMANCE.md) | Measurement methods, results, and their limits. |
| [Validation record](docs/VALIDATION.md) | Previously recorded build and test checks. |

The [documentation index](docs/README.md) links to all project guides in **English and Bahasa Indonesia**.

## Contributing and support

Use [GitHub Issues](https://github.com/BaghasWahyu/komikita/issues) to report bugs or suggest improvements. For a bug report, include your KomiKita version, Android version, APK variant, steps to reproduce, and relevant logs with personal information removed.

Code, documentation, and translation contributions are welcome in English or Bahasa Indonesia. Start with [CONTRIBUTING.md](CONTRIBUTING.md) and follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## Credits and license

KomiKita builds on [Mihon](https://github.com/mihonapp/mihon) and Tachiyomi. Credit belongs to their teams and contributors; upstream copyright and license notices are retained. See [Credits and acknowledgements](CREDITS.md) and the [upstream changelog](docs/UPSTREAM_CHANGELOG.md).

Copyright © 2015 Javier Tomás<br />
Copyright © 2024 Mihon Open Source Project

Licensed under the [Apache License 2.0](LICENSE). KomiKita does not host content and is not affiliated with content providers.
