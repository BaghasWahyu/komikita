[English](../PROJECT_ANALYSIS.md) | **Bahasa Indonesia**

# Analisis Proyek KomiKita

Aplikasi Android pembaca manga/komik dengan Kotlin dan Jetpack Compose, berbasis Mihon/Tachiyomi. Android minimum API 26. Proyek memakai Gradle convention plugins dan version catalog.

## Struktur

| Modul | Peran |
| --- | --- |
| app | Activity, ViewModel, Compose UI, pembaca, unduhan, backup, tracker, dan ekstensi |
| domain | Model, kontrak repository, interactor, dan logika chapter/kategori/perpustakaan |
| data | Implementasi repository, SQLite/SQLDelight, migrasi, serta layanan release |
| core:common / core:metro / core:archive | Preferensi, jaringan, utilitas bersama, DI, dan arsip |
| core:native / native | Bridge JNI dan pemrosesan Rust |
| core-metadata | Metadata ComicInfo dan data komik |
| source-api / source-local | Kontrak ekstensi serta pembacaan konten lokal |
| presentation-core / presentation-widget | UI bersama dan widget |
| i18n / icons:* | Lokalisasi dan aset ikon |
| telemetry / baseline-profile | Telemetry opsional dan pengukuran startup |

Alur data utama: Compose → ViewModel → interactor/repository domain → implementasi data → SQLDelight atau sumber ekstensi. Flow digunakan untuk pembaruan reaktif. Metro menyediakan DI, dengan jembatan Injekt untuk kompatibilitas ekstensi.

## Hasil yang ditangani

- Identitas dan updater upstream dipisahkan ke KomiKita, termasuk application ID dan metadata distribusi.
- Konfigurasi Firebase upstream dikeluarkan dan telemetry menjadi opt-in saat build.
- Salah nama manifest telemetry tanpa Firebase diperbaiki.
- Perbandingan versi diperbaiki agar rilis mayor/minor lama tidak dianggap pembaruan; versi tidak valid tidak menyebabkan crash.
- Perhitungan chapter hilang memakai array primitif, fallback Kotlin, dan batch Rust; input tidak valid serta overflow ditangani.
- Preferensi dan filter kategori/ekstensi tersembunyi serta badge ikon ekstensi ditambahkan.
- Pembaca mendapat pengaturan refresh rate untuk mengurangi potensi beban layar saat membaca.

## Batas pemeriksaan

Build, unit test, lint, dan migrasi diverifikasi secara terpisah dari pengukuran perangkat. Lihat [PERFORMANCE.md](PERFORMANCE.md) untuk temuan baterai dan batas kesimpulannya. Tidak ada klaim peningkatan baterai berdasarkan benchmark host Rust.

Konfigurasi jaringan yang mengizinkan HTTP dan sertifikat pengguna berasal dari dukungan berbagai sumber. Audit keamanan lebih lanjut perlu mempertimbangkan kompatibilitas tersebut. Callback OAuth lama dan namespace source dipertahankan; lihat [BRANDING.md](BRANDING.md).
