**English** | [Bahasa Indonesia](id/BRANDING.md)

# KomiKita identity

- Product name: **KomiKita**.
- Repository: [BaghasWahyu/komikita](https://github.com/BaghasWahyu/komikita).
- Application ID: `app.komikita`.
- Logo: an open book with a K monogram; mint green `#83E8BE` and ink `#102D2B`. Debug variants use blue.
- Vector source: `.github/assets/logo.svg`; the launcher uses a VectorDrawable, adaptive icon, and monochrome icon.
- The logo is used for the splash screen, notifications, About page, IDE, README, and store metadata.

Project, issue, and privacy links, user agents, and release artifacts identify KomiKita. Shared technical help still links to Mihon's upstream documentation.

## Documentation languages

English is the primary language for project descriptions and documentation. Indonesian versions are available through the language links at the top of each translated document. Store metadata has separate English (`en-US`) and Indonesian (`id`) directories. Keep product names, package names, identifiers, and command-line options unchanged in translations.

## Compatibility

The source packages `eu.kanade.*`, `tachiyomi.*`, and `mihon.*` are not application IDs. KomiKita can be installed alongside Mihon.

The `mihon://` and `app.mihon://comick-auth` callbacks remain accepted because they are registered with OAuth providers. Replacing them requires a KomiKita client registration with each provider. The `komikita://extension-store` scheme is available for KomiKita links.

SourceMihon metadata, extension contracts, preference keys, and the backup format are retained. Upstream copyright notices, dependencies, and changelog entries do not claim to be KomiKita's identity.
