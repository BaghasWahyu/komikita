**English** | [Bahasa Indonesia](README.id.md)

<div align="center">
<img src=".github/assets/logo.png" alt="KomiKita logo" width="128" />

# KomiKita

**Your comics, always close.** · Version 1.0.0

An open-source manga, comic, and webtoon reader for Android 8.0 and later.

[Downloads](https://github.com/BaghasWahyu/komikita/releases) · [Report an issue](https://github.com/BaghasWahyu/komikita/issues) · [Contribute](CONTRIBUTING.md)
</div>

## Features

- Customizable page and webtoon readers with reading direction, themes, and controls.
- A library with categories, search, filters, and downloads for offline reading.
- Hide categories and their comics, and show them again through category management.
- Hide extensions and their sources from Browse without uninstalling them.
- Optional extension icon badges on comics in your library.
- Reading progress tracking, scheduled library updates, backup, and restore.
- A reader battery saver setting that requests a display refresh rate of up to 60 Hz.

See the [user guide](docs/USER_GUIDE.md) for hidden categories, hidden extensions, extension icons, and the battery saver setting. The [documentation index](docs/README.md) links to English and Indonesian versions of every project guide.

## Development

KomiKita uses Kotlin, Jetpack Compose, SQLDelight, WorkManager, and a Rust module accessed through JNI for chapter processing. See the [contributor guide](CONTRIBUTING.md), [project identity](docs/BRANDING.md), and [performance notes](docs/PERFORMANCE.md).

## Privacy and distribution

Application ID: `app.komikita`. Telemetry and the built-in app updater are disabled by default. Releases are distributed through the KomiKita repository. See the [privacy policy](docs/PRIVACY.md).

## Origins and license

KomiKita is a fork of [Mihon](https://github.com/mihonapp/mihon), which continues the Tachiyomi codebase. Full credit goes to the Mihon team and all Tachiyomi contributors; see [Credits and acknowledgements](CREDITS.md). Upstream copyright notices are retained. Changes made before the fork are recorded in the [upstream changelog](docs/UPSTREAM_CHANGELOG.md).

Copyright © 2015 Javier Tomás
Copyright © 2024 Mihon Open Source Project

Licensed under the [Apache License 2.0](LICENSE). KomiKita does not host content and is not affiliated with content providers.
