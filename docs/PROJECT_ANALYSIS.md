**English** | [Bahasa Indonesia](id/PROJECT_ANALYSIS.md)

# KomiKita project analysis

An Android manga and comic reader built with Kotlin and Jetpack Compose, based on Mihon/Tachiyomi. The minimum Android API level is 26. The project uses Gradle convention plugins and version catalogs.

## Structure

| Module | Role |
| --- | --- |
| app | Activities, ViewModels, Compose UI, reader, downloads, backup, trackers, and extensions |
| domain | Models, repository contracts, interactors, and chapter/category/library logic |
| data | Repository implementations, SQLite/SQLDelight, migrations, and release services |
| core:common / core:metro / core:archive | Preferences, networking, shared utilities, dependency injection, and archives |
| core:native / native | JNI bridge and Rust processing |
| core-metadata | ComicInfo metadata and comic data |
| source-api / source-local | Extension contracts and local content reading |
| presentation-core / presentation-widget | Shared UI and widgets |
| i18n / icons:* | Localization and icon assets |
| telemetry / baseline-profile | Optional telemetry and startup measurement |

The main data flow is Compose → ViewModel → domain interactor/repository → data implementation → SQLDelight or an extension source. Flow provides reactive updates. Metro provides dependency injection, with an Injekt bridge for extension compatibility.

## Changes addressed

- Separated the upstream identity and updater into KomiKita, including the application ID and distribution metadata.
- Removed upstream Firebase configuration and made telemetry a build-time opt-in.
- Fixed the manifest filename for builds without Firebase telemetry.
- Fixed version comparisons so older major/minor releases are not treated as updates; invalid versions do not crash the app.
- Added primitive arrays, a Kotlin fallback, and Rust batches for missing-chapter calculations, with invalid input and overflow handling.
- Added hidden category/extension preferences and filters, and extension icon badges.
- Added reader refresh rate settings to reduce potential display power use while reading.

## Verification limits

Builds, unit tests, lint, and migrations are separate checks from device measurements. See the [performance notes](PERFORMANCE.md) for battery findings and the limits of those conclusions. No battery improvement is claimed from Rust host benchmarks.

The network configuration allows HTTP and user certificates to support various sources. Further security review needs to consider this compatibility requirement. Legacy OAuth callbacks and source namespaces are retained; see [project identity and compatibility](BRANDING.md).
