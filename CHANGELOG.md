**English** | [Bahasa Indonesia](CHANGELOG.id.md)

# KomiKita changelog

## Unreleased

- Merge Mihon upstream through `ede052af1`, updating WebGPU viewer to version 57 with fast rendering, final-tile display, and custom upscaler/downscaler settings. Retain KomiKita's newer XML dependency and direct resource imports.
- Protect hidden categories with Android biometrics or device credentials, with automatic locking in the background and protected reader entry points.
- Exclude hidden titles from Continue reading, history, updates, notifications, widgets, and reading recaps, including titles that also belong to visible categories.
- Swipe through up to five Continue reading recommendations and resume from new chapter notifications.
- Export diagnostic logs with app and device details, bounded collection time, and redacted URLs, credentials, and hidden titles.
- Record daily reading activity for current and best streaks, yearly completed chapters, time, top titles, and monthly recaps. Daily activity begins with this version and is not yet included in backups.
- Add original emulator screenshots to both READMEs and a reproducible demo script.

## 1.0.6 — 2026-11-03

### Library

- Continue reading keeps the most recently opened comic selected while it has unread chapters.
- After catching up, the card switches to an unread comic with the newest chapter update. Sources without upload dates use the chapter fetch time as a fallback.
- Suggestions respect the comics visible in the current collection and its filters.

### Fixes

- Manual **Check for updates** now requests fresh release information instead of reusing a cached response.

## 1.0.0 — 2026-11-02

- KomiKita branding for the app name, icons, splash screen, notifications, documentation, store metadata, and releases.
- Separate application ID: `app.komikita`.
- Telemetry disabled by default; GitHub release APKs, including FOSS, enable app updates from the KomiKita repository. Local and CI builds keep the updater disabled by default.
- Hide categories and their comics, and extensions and their sources.
- Optional extension icon badges in the library.
- Rust module for chapter processing with a Kotlin fallback.
- Reader battery saver setting with display refresh rate selection.
- Fixes for version comparisons, chapter number validation, and the telemetry manifest.
- English project documentation with linked Indonesian versions and localized store descriptions.

Changes made before the fork are recorded in the [upstream changelog](docs/UPSTREAM_CHANGELOG.md).
