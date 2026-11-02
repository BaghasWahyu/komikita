[English](../VALIDATION.md) | **Bahasa Indonesia**

# Validasi KomiKita 1.0.0

Pemeriksaan pada 2 November 2026:

- Gradle: spotlessCheck, testDebugUnitTest, verifySqlDelightMigration, assembleDebug, dan assembleRelease.
- 155 unit test Android lulus, tanpa kegagalan atau error.
- 6 test Rust lulus. cargo fmt --check dan cargo clippy --all-targets -- -D warnings bersih.
- JNI diverifikasi pada host menggunakan class Kotlin hasil build dan library Rust hasil build: 100 batch deterministik cocok dengan implementasi referensi.
- Library Rust berhasil dibangun untuk armeabi-v7a, arm64-v8a, x86, dan x86_64.
- Build Android memakai NDK 30 yang sudah tersedia melalui opsi rust-ndk-path; proyek/CI menetapkan NDK 29 secara bawaan.
- Pengujian nomor chapter besar juga mencakup jalur fallback Kotlin saat native host tidak tersedia.
- Uji kategori mencakup komik dalam kategori tersembunyi sekaligus kategori yang terlihat, pemulihan visibilitas, dan ID kategori yang tidak terkait.

Catatan ini merekam pemeriksaan tersebut dan bukan hasil validasi perubahan yang masih dalam pengerjaan setelahnya.

APK release lokal digunakan sebagai APK uji. Untuk distribusi resmi, gunakan keystore KomiKita sesuai [panduan kontribusi](../../CONTRIBUTING.id.md).

Tidak ada perangkat atau emulator terhubung saat pemeriksaan. Penggunaan UI di perangkat nyata dan dampak baterai belum diukur. Lihat [PERFORMANCE.md](PERFORMANCE.md) untuk prosedur dan batas kesimpulan.
