[English](CONTRIBUTING.md) | **Bahasa Indonesia**

# Kontribusi untuk KomiKita

Laporkan bug dan usulkan fitur melalui [Issues KomiKita](https://github.com/BaghasWahyu/komikita/issues). Sertakan versi aplikasi, versi Android, langkah reproduksi, dan log yang telah dibersihkan dari data pribadi.

Laporan dan kontribusi dapat ditulis dalam bahasa Inggris atau Bahasa Indonesia.

## Persiapan

- Android Studio dan JDK 21 (target bytecode Java 17).
- SDK sesuai `gradle/mihon.versions.toml`: compile SDK 37.2, target 36, minimum 26; NDK 29.0.14206865.
- Rust stable, target Android, dan cargo-ndk untuk build native; lihat [panduan Rust](docs/id/NATIVE.md).
- Tambahkan `sdk.dir=...` pada `local.properties` atau atur ANDROID_HOME.

Gunakan Gradle Wrapper proyek. Di Windows ganti `./gradlew` dengan `gradlew.bat`.

```sh
./gradlew assembleDebug
./gradlew spotlessCheck testDebugUnitTest verifySqlDelightMigration
```

Tanpa toolchain Rust, gunakan `-Penable-rust=false`; implementasi Kotlin tetap tersedia.

## Distribusi

Application ID adalah `app.komikita`; varian debug, FOSS, nightly, dan benchmark memiliki suffix terpisah. Gunakan keystore KomiKita melalui `keystore.properties`. Jangan commit keystore atau kredensial.

Workflow rilis GitHub mengaktifkan updater untuk APK biasa dan FOSS dengan `-Penable-updater=true`. Build lokal dan CI tetap menonaktifkannya kecuali flag tersebut diberikan. Repositori pembaruan adalah `BaghasWahyu/komikita`.

Firebase tidak disertakan secara bawaan. Jika diperlukan, sediakan `app/google-services.json` milik sendiri untuk seluruh application ID yang dibangun, lalu gunakan `-Pinclude-telemetry=true`. Pengumpulan mengikuti persetujuan pengguna.

Workflow rilis membuat draft pada repositori KomiKita. Konfigurasikan secret SIGNING_KEY, KEY_STORE_PASSWORD, ALIAS, dan KEY_PASSWORD sebelum menjalankannya. Untuk setiap rilis baru, naikkan `versionName` dan `versionCode` di `app/build.gradle.kts`, gunakan tag yang sesuai seperti `v1.0.2`, dan pertahankan signing key yang sama. Publikasikan draft setelah memeriksa aset APK-nya; updater memeriksa rilis stabil terbaru yang sudah dipublikasikan, sehingga draft dan prerelease tidak ditawarkan. Pengguna APK lama dengan updater nonaktif perlu memasang APK dengan updater aktif secara manual satu kali.

## Terjemahan dan kompatibilitas

String bahasa Inggris berada di `i18n/src/commonMain/moko-resources/base/`, sedangkan Bahasa Indonesia di `i18n/src/commonMain/moko-resources/in/`. Terjemahan upstream yang tersedia dipertahankan. Lihat [panduan terjemahan](i18n/README.id.md).

Bahasa Inggris menjadi bahasa utama dokumentasi proyek. Saat mengubah panduan, perbarui juga pasangan versi Bahasa Indonesianya. Lihat [indeks dokumentasi](docs/id/README.md) untuk kedua versi.

Namespace source, kontrak ekstensi Tachiyomi, metadata ComicInfo, format backup, dan callback OAuth yang terdaftar pada penyedia dipertahankan. Lihat [BRANDING.md](docs/id/BRANDING.md).

## Kredit

Pertahankan [LICENSE](LICENSE), kredit Mihon/Tachiyomi, dan hak cipta kontributor saat mengubah atau mendistribusikan fork.
