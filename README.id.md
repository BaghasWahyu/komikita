[English](README.md) | **Bahasa Indonesia**

<div align="center">
<img src=".github/assets/logo.png" alt="Logo KomiKita" width="128" />

# KomiKita

**Komik favorit, selalu dekat.** · Versi 1.0.1

Pembaca manga, komik, dan webtoon open-source untuk Android 8.0 ke atas.

[Unduhan](https://github.com/BaghasWahyu/komikita/releases) · [Laporkan masalah](https://github.com/BaghasWahyu/komikita/issues) · [Kontribusi](CONTRIBUTING.id.md)
</div>

## Fitur

- Pembaca untuk halaman dan webtoon dengan arah baca, tema, dan kontrol yang dapat diatur.
- Perpustakaan dengan kategori, pencarian, filter, komik lokal, dan unduhan chapter untuk membaca offline.
- Sembunyikan kategori beserta komiknya; tampilkan kembali melalui pengelolaan kategori.
- Sembunyikan ekstensi dan sumbernya dari Jelajah tanpa menghapus instalasi.
- Opsi badge ikon ekstensi pada komik di perpustakaan.
- Pelacakan progres, pembaruan terjadwal, pencadangan, dan pemulihan data.
- Opsi hemat baterai pembaca yang meminta refresh rate layar hingga 60 Hz.

Lihat [panduan penggunaan KomiKita](docs/id/USER_GUIDE.md) untuk kategori tersembunyi, ekstensi tersembunyi, ikon ekstensi, dan mode hemat baterai.

[Indeks dokumentasi](docs/id/README.md) menyediakan tautan ke versi Inggris dan Indonesia untuk seluruh panduan proyek.

## Instalasi

1. Unduh APK dari [Rilis KomiKita](https://github.com/BaghasWahyu/komikita/releases). Pilih APK sesuai arsitektur perangkat, atau APK universal jika tidak yakin.
2. Instal pada perangkat dengan **Android 8.0 (API 26) atau lebih baru**, lalu izinkan instalasi dari browser atau pengelola berkas saat Android memintanya.

KomiKita memiliki ID aplikasi sendiri, `app.komikita`, sehingga dapat dipasang berdampingan dengan Mihon. Untuk memindahkan perpustakaan, buat cadangan di Mihon lalu pulihkan melalui pengaturan data dan penyimpanan KomiKita. Ekstensi dan login layanan mungkin perlu diatur kembali. Lihat [Berpindah dari Mihon](docs/id/USER_GUIDE.md#berpindah-dari-mihon).

## Pengembangan

KomiKita memakai Kotlin, Jetpack Compose, SQLDelight, dan WorkManager. Modul Rust opsional melalui JNI menghitung chapter yang hilang, dengan implementasi Kotlin sebagai fallback.

### Persyaratan

- Android Studio dan **JDK 21**, sesuai [CI](.github/.java-version). Proyek menargetkan bytecode Java 17.
- Versi Android SDK dan NDK sesuai [katalog versi](gradle/mihon.versions.toml): compile SDK **37.2** dan NDK **29.0.14206865**.
- Untuk build native, Rust stable, target Android, dan cargo-ndk. Ikuti [panduan modul native](docs/id/NATIVE.md) untuk memasang toolchain.

### Build dan pemeriksaan

Clone repositori, lalu atur Android SDK melalui `sdk.dir` pada `local.properties` atau variabel lingkungan `ANDROID_HOME`.

```sh
git clone https://github.com/BaghasWahyu/komikita.git
cd komikita
./gradlew assembleDebug
```

Di Windows, ganti `./gradlew` dengan `.\gradlew.bat`. APK debug dihasilkan pada `app/build/outputs/apk/debug/`.

Untuk build tanpa toolchain Rust, gunakan implementasi Kotlin sebagai fallback:

```sh
./gradlew assembleDebug -Penable-rust=false
```

Jalankan pemeriksaan format, unit test, dan migrasi database proyek:

```sh
./gradlew spotlessCheck testDebugUnitTest verifySqlDelightMigration
```

Tambahkan `-Penable-rust=false` pada perintah pemeriksaan di atas jika toolchain native belum terpasang. Lihat [panduan kontribusi](CONTRIBUTING.id.md) untuk penandatanganan, varian build, terjemahan, dan konfigurasi rilis.

## Dokumentasi

| Panduan | Isi |
| --- | --- |
| [Panduan penggunaan](docs/id/USER_GUIDE.md) | Kategori dan ekstensi tersembunyi, ikon perpustakaan, hemat baterai, dan migrasi |
| [Changelog](CHANGELOG.id.md) | Perubahan pada KomiKita |
| [Struktur proyek](docs/id/PROJECT_ANALYSIS.md) | Modul, arsitektur, dan alur data |
| [Modul native](docs/id/NATIVE.md) | Persiapan toolchain dan fallback Kotlin |
| [Catatan performa](docs/id/PERFORMANCE.md) | Detail pengukuran performa dan baterai |
| [Identitas proyek](docs/id/BRANDING.md) | Branding dan kompatibilitas dengan basis kode upstream |

Buka [indeks dokumentasi](docs/id/README.md) untuk seluruh panduan dalam bahasa Inggris dan Bahasa Indonesia. Laporkan bug atau usulkan fitur melalui [Issues KomiKita](https://github.com/BaghasWahyu/komikita/issues); kontribusi diterima dalam kedua bahasa.

## Privasi dan distribusi

Build bawaan tidak menyertakan Firebase Analytics maupun Crashlytics. APK dari workflow rilis GitHub, termasuk FOSS, mengaktifkan updater bawaan untuk [Rilis KomiKita](https://github.com/BaghasWahyu/komikita/releases); build lokal dan CI tetap menonaktifkannya kecuali diaktifkan secara eksplisit. Lihat [Memeriksa pembaruan aplikasi](docs/id/USER_GUIDE.md#memeriksa-pembaruan-aplikasi) dan [kebijakan privasi](docs/id/PRIVACY.md).

## Asal proyek dan lisensi

KomiKita merupakan fork dari [Mihon](https://github.com/mihonapp/mihon), penerus basis kode Tachiyomi. Kredit penuh diberikan kepada tim Mihon dan seluruh kontributor Tachiyomi; lihat [Kredit dan Penghargaan](CREDITS.id.md). Pemberitahuan hak cipta upstream dipertahankan. Riwayat sebelum fork tersedia di [changelog upstream](docs/UPSTREAM_CHANGELOG.md).

Copyright © 2015 Javier Tomás
Copyright © 2024 Mihon Open Source Project

Dilisensikan di bawah [Apache License 2.0](LICENSE). KomiKita tidak menghosting konten dan tidak berafiliasi dengan penyedia konten.
