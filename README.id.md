[English](README.md) | **Bahasa Indonesia**

<div align="center">

<img src=".github/assets/logo.png" alt="Logo KomiKita" width="112" />

# KomiKita

**Komik favorit, selalu dekat.**

Pembaca manga, komik, dan webtoon open-source untuk Android.<br />
Atur koleksi, baca secara offline, dan lanjutkan bacaan terakhir.

[![Rilis terbaru](https://img.shields.io/github/v/release/BaghasWahyu/komikita?label=rilis&color=0f766e)](https://github.com/BaghasWahyu/komikita/releases/latest)
[![Android 8.0 ke atas](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](#unduh-dan-instal)
[![Build dan pengujian](https://github.com/BaghasWahyu/komikita/actions/workflows/build.yml/badge.svg)](https://github.com/BaghasWahyu/komikita/actions/workflows/build.yml)
[![Lisensi Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue)](LICENSE)

[**Unduh rilis terbaru**](https://github.com/BaghasWahyu/komikita/releases/latest) · [Panduan penggunaan](docs/id/USER_GUIDE.md) · [Changelog](CHANGELOG.id.md) · [Laporkan masalah](https://github.com/BaghasWahyu/komikita/issues)

</div>

KomiKita merupakan fork dari [Mihon](https://github.com/mihonapp/mihon), yang melanjutkan basis kode Tachiyomi dengan identitas sendiri serta tambahan untuk pengelolaan perpustakaan, kelanjutan bacaan, dan pembaruan aplikasi. ID aplikasinya terpisah sehingga dapat dipasang berdampingan dengan Mihon.

## Daftar isi

- [Fitur](#fitur)
- [Unduh dan instal](#unduh-dan-instal)
- [Mulai menggunakan](#mulai-menggunakan)
- [Pembaruan aplikasi](#pembaruan-aplikasi)
- [Berpindah dari Mihon](#berpindah-dari-mihon)
- [Privasi dan sumber konten](#privasi-dan-sumber-konten)
- [Build dari source](#build-dari-source)
- [Struktur proyek](#struktur-proyek)
- [Dokumentasi](#dokumentasi)
- [Kontribusi dan dukungan](#kontribusi-dan-dukungan)
- [Kredit dan lisensi](#kredit-dan-lisensi)

## Fitur

| Bagian | Kemampuan |
| --- | --- |
| **Pembaca** | Pilih mode halaman atau webtoon serta atur arah baca, tema, dan kontrol pembaca. |
| **Perpustakaan** | Kelompokkan komik ke dalam kategori, cari dan filter koleksi, serta gunakan tampilan grid atau daftar. |
| **Lanjutkan Membaca** | Lanjutkan komik yang terakhir dibuka selama masih ada chapter belum dibaca. Setelah selesai, kartu beralih ke komik belum dibaca dengan pembaruan chapter terbaru dalam koleksi yang terlihat. |
| **Konten offline dan lokal** | Unduh chapter untuk dibaca offline dan baca komik yang tersimpan di perangkat. |
| **Pengaturan visibilitas** | Sembunyikan kategori beserta komiknya, atau sembunyikan ekstensi dan sumbernya dari Jelajah tanpa mencopot ekstensi. |
| **Badge sumber** | Tampilkan ikon ekstensi pada komik di perpustakaan jika diinginkan. |
| **Progres dan pelacakan** | Simpan riwayat serta progres membaca, jadwalkan pembaruan perpustakaan, dan hubungkan layanan yang didukung seperti AniList dan MyAnimeList. |
| **Cadangan dan pemulihan** | Cadangkan perpustakaan, progres membaca, dan pengaturan ke lokasi pilihan. |
| **Hemat baterai pembaca** | Prioritaskan refresh rate layar hingga 60 Hz saat membaca; preferensi ini dapat dinonaktifkan. |
| **Pembaruan aplikasi** | Periksa rilis KomiKita yang telah dipublikasikan, baca catatan rilis, dan unduh APK kompatibel yang lebih baru dari aplikasi. |

[Panduan penggunaan](docs/id/USER_GUIDE.md) menjelaskan kategori dan ekstensi tersembunyi, badge sumber, hemat baterai, dan migrasi. Dampak pada baterai bergantung pada perangkat serta kondisi membaca; lihat [catatan performa](docs/id/PERFORMANCE.md) untuk pengukuran dan batas kesimpulannya.

## Unduh dan instal

KomiKita memerlukan **Android 8.0 (API 26) atau lebih baru**. Unduh APK melalui [GitHub Releases](https://github.com/BaghasWahyu/komikita/releases/latest).

### Pilih APK

Pada nama berkas di bawah, `X.Y.Z` merupakan versi rilis.

| Nama APK | Penggunaan |
| --- | --- |
| `komikita-vX.Y.Z.apk` | **APK universal — disarankan jika tidak yakin.** |
| `komikita-arm64-v8a-vX.Y.Z.apk` | Ponsel dan tablet ARM64. |
| `komikita-armeabi-v7a-vX.Y.Z.apk` | Perangkat dengan ARM 32-bit. |
| `komikita-x86-vX.Y.Z.apk` | Perangkat atau emulator x86 32-bit. |
| `komikita-x86_64-vX.Y.Z.apk` | Perangkat atau emulator x86 64-bit. |

Workflow rilis GitHub membangun aplikasi standar (`app.komikita`) dengan updater aplikasi dan secara bawaan tanpa Firebase. Workflow tidak lagi membangun varian FOSS terpisah (`app.komikita.foss`). Instalasi FOSS yang sudah ada menyimpan data aplikasi secara terpisah dan tidak dapat diperbarui dengan APK standar.

### Instal aplikasi

1. Buka rilis terbaru dan unduh APK yang dipilih.
2. Buka APK melalui browser atau pengelola berkas.
3. Izinkan instalasi dari aplikasi tersebut saat Android memintanya, lalu konfirmasikan instalasi.
4. Jalankan KomiKita dan selesaikan penyiapan awal.

Untuk memperbarui instalasi yang sudah ada, pasang APK lebih baru dengan **varian yang sama** di atas aplikasi lama. Pertahankan instalasi aplikasi agar datanya tetap tersimpan.

## Mulai menggunakan

1. **Pilih folder penyimpanan.** Tentukan folder khusus saat penyiapan untuk unduhan, cadangan, dan berkas aplikasi lainnya.
2. **Tambahkan bacaan.** Gunakan komik lokal atau atur ekstensi sumber yang kompatibel beserta repositorinya secara terpisah.
3. **Susun perpustakaan.** Tambahkan judul ke koleksi, buat kategori, serta sesuaikan tampilan dan filter.
4. **Atur cara membaca.** Pilih mode pembaca, arah baca, dan kontrol, atau unduh chapter sebelum membaca offline.
5. **Simpan cadangan.** Atur pencadangan melalui pengaturan data dan penyimpanan sebelum berpindah perangkat atau mengubah instalasi.

KomiKita tidak menyertakan komik atau menyediakan sumber kontennya sendiri. Ketersediaan dan perilaku konten mengikuti berkas lokal serta ekstensi yang dikonfigurasi.

## Pembaruan aplikasi

APK rilis resmi GitHub memeriksa pembaruan saat aplikasi dibuka dan setiap enam jam selama digunakan. Untuk memeriksa secara manual, buka:

**Lainnya → Tentang → Periksa pembaruan**

Jika tersedia pembaruan, KomiKita menampilkan catatan rilis dan menawarkan unduhan APK. Android akan meminta konfirmasi instalasi.

Hanya **rilis stabil yang sudah dipublikasikan**, memiliki **versi lebih tinggi**, dan menyediakan **APK kompatibel** yang ditawarkan. Draft, prerelease, dan versi yang sudah terpasang tidak dianggap sebagai pembaruan baru. Versi `1.0.6` menambahkan permintaan jaringan baru saat pemeriksaan manual agar informasi rilis tidak diambil ulang dari cache.

Jika build lama masih menampilkan **No new updates**, bandingkan versi terpasang pada **Tentang** dengan [rilis GitHub terbaru](https://github.com/BaghasWahyu/komikita/releases/latest). APK lebih baru dapat dipasang secara manual dengan varian yang sama. Build lokal dan CI menonaktifkan updater kecuali diaktifkan secara eksplisit saat build.

Lihat [Memeriksa pembaruan aplikasi](docs/id/USER_GUIDE.md#memeriksa-pembaruan-aplikasi) untuk penjelasan lebih lanjut.

## Berpindah dari Mihon

KomiKita dapat dipasang berdampingan dengan Mihon, sehingga perpustakaan dapat dipindahkan sementara aplikasi asal tetap tersedia.

1. Buat cadangan di Mihon.
2. Instal KomiKita dan pilih folder penyimpanannya.
3. Pulihkan cadangan melalui pengaturan data dan penyimpanan KomiKita.
4. Periksa perpustakaan, ekstensi, progres membaca, dan koneksi tracker. Ekstensi serta login layanan mungkin perlu dikonfigurasi kembali.

Baca [Berpindah dari Mihon](docs/id/USER_GUIDE.md#berpindah-dari-mihon) untuk panduan migrasi dan [identitas serta kompatibilitas proyek](docs/id/BRANDING.md) untuk penjelasan tentang fork ini.

## Privasi dan sumber konten

Data perpustakaan, progres membaca, preferensi, dan kredensial tracker disimpan pada perangkat. Cadangan disimpan di lokasi yang dipilih.

Build bawaan tidak menyertakan Firebase Analytics dan Crashlytics. Sumber konten, ekstensi, tracker, dan penyedia DNS menerima permintaan yang diperlukan untuk fiturnya. APK rilis juga menghubungi GitHub untuk memeriksa dan mengunduh pembaruan aplikasi.

Baca [kebijakan privasi](docs/id/PRIVACY.md) untuk penjelasan lengkap tentang penyimpanan data, permintaan jaringan, dan telemetry opsional.

## Build dari source

KomiKita menggunakan **Kotlin**, **Jetpack Compose**, **SQLDelight**, dan **WorkManager**. Modul **Rust** opsional melalui JNI memproses data chapter, dengan implementasi Kotlin sebagai fallback.

### Persyaratan

| Tool | Konfigurasi proyek |
| --- | --- |
| Android Studio | Gunakan versi yang kompatibel dengan Android Gradle Plugin proyek. |
| JDK | CI dimulai dengan JDK 21 dari [`.github/.java-version`](.github/.java-version). [Kriteria daemon Gradle](gradle/gradle-daemon-jvm.properties) memilih JetBrains JDK 25; Gradle dapat menyediakan JVM daemon yang sesuai. Target bytecode Java adalah versi 17. |
| Android SDK | Compile SDK **37.2**, target SDK **36**, minimum SDK **26**. |
| Android NDK | **29.0.14206865** untuk build native. |
| Gradle | Gunakan wrapper yang disertakan; tidak perlu memasang Gradle secara terpisah. |
| Rust, opsional | Rust stable, target Android, dan `cargo-ndk` untuk build yang menyertakan modul native. |

[Katalog versi](gradle/mihon.versions.toml), [konfigurasi wrapper Gradle](gradle/wrapper/gradle-wrapper.properties), dan [workflow CI](.github/workflows/build.yml) menjadi acuan persyaratan build.

### Build cepat dengan fallback Kotlin

```sh
git clone https://github.com/BaghasWahyu/komikita.git
cd komikita
```

Atur lokasi Android SDK melalui `ANDROID_HOME`, atau buat berkas `local.properties` yang tidak dilacak Git pada root repositori:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

Kemudian jalankan build tanpa memerlukan toolchain Rust:

```sh
./gradlew assembleDebug -Penable-rust=false
```

Di Windows, ganti `./gradlew` dengan `.\gradlew.bat`. APK debug dihasilkan pada `app/build/outputs/apk/debug/` dan menggunakan ID aplikasi terpisah `app.komikita.dev`.

### Build dengan pemrosesan native

Ikuti [panduan modul native](docs/id/NATIVE.md) untuk memasang Rust, target Android, dan `cargo-ndk`, lalu jalankan:

```sh
./gradlew assembleDebug
```

Untuk memakai NDK lain yang sudah terpasang, tambahkan `-Prust-ndk-path=/path/to/ndk`.

### Jalankan pemeriksaan

```sh
./gradlew spotlessCheck testDebugUnitTest verifySqlDelightMigration -Penable-rust=false
```

Hilangkan `-Penable-rust=false` untuk menyertakan build native setelah toolchain-nya siap. [Panduan kontribusi](CONTRIBUTING.id.md) menjelaskan signing, varian rilis, terjemahan, dan pembuatan catatan rilis. Konfigurasikan signing rilis sebelum mendistribusikan APK; pembaruan instalasi yang sudah ada memerlukan ID aplikasi dan signing key yang sama.

## Struktur proyek

| Lokasi | Tanggung jawab |
| --- | --- |
| [`app/`](app) | UI Compose, pembaca, unduhan, tracker, cadangan, dan layanan aplikasi. |
| [`domain/`](domain) | Model, use case, dan kontrak repository. |
| [`data/`](data) | Akses database, implementasi repository, dan metadata rilis. |
| [`core/`](core) | Utilitas bersama, jaringan, arsip, dependency injection, dan penghubung JNI. |
| [`native/`](native) | Implementasi pemrosesan chapter dengan Rust. |
| [`source-api/`](source-api), [`source-local/`](source-local) | Kontrak ekstensi dan dukungan konten lokal. |
| [`presentation-core/`](presentation-core), [`presentation-widget/`](presentation-widget) | Komponen presentasi bersama dan widget. |
| [`i18n/`](i18n), [`icons/`](icons) | Terjemahan dan aset ikon. |

Lihat [struktur proyek dan alur data](docs/id/PROJECT_ANALYSIS.md) untuk gambaran arsitektur.

## Dokumentasi

| Panduan | Isi |
| --- | --- |
| [Panduan penggunaan](docs/id/USER_GUIDE.md) | Visibilitas perpustakaan, ikon ekstensi, hemat baterai, pembaruan, dan migrasi. |
| [Changelog](CHANGELOG.id.md) | Fitur dan perbaikan pada setiap versi. |
| [Kontribusi](CONTRIBUTING.id.md) | Persiapan pengembangan, signing, terjemahan, dan penyiapan rilis. |
| [Kebijakan privasi](docs/id/PRIVACY.md) | Data lokal, permintaan jaringan, dan telemetry. |
| [Modul native](docs/id/NATIVE.md) | Toolchain Rust, integrasi JNI, dan fallback Kotlin. |
| [Catatan performa](docs/id/PERFORMANCE.md) | Metode pengukuran, hasil, dan batas kesimpulannya. |
| [Catatan validasi](docs/id/VALIDATION.md) | Riwayat pemeriksaan build dan pengujian. |

[Indeks dokumentasi](docs/id/README.md) menautkan seluruh panduan proyek dalam **bahasa Inggris dan Bahasa Indonesia**.

## Kontribusi dan dukungan

Gunakan [GitHub Issues](https://github.com/BaghasWahyu/komikita/issues) untuk melaporkan bug atau mengusulkan perbaikan. Sertakan versi KomiKita, versi Android, varian APK, langkah reproduksi, dan log terkait yang telah dibersihkan dari informasi pribadi.

Kontribusi kode, dokumentasi, dan terjemahan diterima dalam bahasa Inggris maupun Bahasa Indonesia. Mulai dari [CONTRIBUTING.id.md](CONTRIBUTING.id.md) dan ikuti [Kode Etik](CODE_OF_CONDUCT.md).

## Kredit dan lisensi

KomiKita dibangun di atas [Mihon](https://github.com/mihonapp/mihon) dan Tachiyomi. Kredit diberikan kepada tim serta kontributornya; pemberitahuan hak cipta dan lisensi upstream dipertahankan. Lihat [Kredit dan Penghargaan](CREDITS.id.md) serta [changelog upstream](docs/UPSTREAM_CHANGELOG.md).

Copyright © 2015 Javier Tomás<br />
Copyright © 2024 Mihon Open Source Project

Dilisensikan di bawah [Apache License 2.0](LICENSE). KomiKita tidak menghosting konten dan tidak berafiliasi dengan penyedia konten.
