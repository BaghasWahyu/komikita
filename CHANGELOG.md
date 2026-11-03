**English** | [Bahasa Indonesia](CHANGELOG.id.md)

# KomiKita changelog

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
