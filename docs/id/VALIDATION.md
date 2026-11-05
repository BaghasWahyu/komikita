[English](../VALIDATION.md) | **Bahasa Indonesia**

# Validasi KomiKita

## Merge Mihon upstream — 5 November 2026

- Gabungkan upstream `ede052af1` (`Bump webgpuviewer (#4242)`) ke `main` bersama fitur privasi dan membaca di bawah. Renderer kini versi 57; KomiKita tetap memakai `xmlutil` 1.0.2.1 serta impor eksplisit untuk resource baru.
- `spotlessApply`, `testDebugUnitTest` seluruh proyek, `verifySqlDelightMigration` seluruh proyek, `:app:assembleDebug`, dan `:app:lintDebug` selesai. Hasil seluruh unit test Android: 211 lulus, 3 dilewati, tanpa kegagalan. Tiga tes integrasi Python/SQLite juga lulus.
- `:app:assembleRelease -Pdist=ci` selesai, termasuk R8. Build debug dan release memakai `-Penable-rust=false`; build release menggunakan cache dependensi dengan `--offline`.
- Smoke test APK debug hasil merge dilakukan pada emulator x86_64 API 37 memakai dua chapter CBZ offline orisinal, masing-masing empat halaman. Pembaca standar merender hingga chapter selesai dan mempertahankan halaman terakhir setelah rotasi. Carousel Lanjutkan membaca membuka chapter 2 dengan benar saat WebGPU diaktifkan.
- WebGPU 57 berhasil menampilkan halaman chapter 2. Pengaturan upscaler/downscaler memuat nama shader bawaan; perubahan **Show all tiles together** dan **Disable tiles** saat membaca, perpindahan halaman, serta masuk latar belakang/kembali ke aplikasi berjalan.
- Pemeriksaan database memastikan kedua chapter selesai hingga indeks halaman 3, dengan durasi baca positif dan catatan aktivitas harian selesai. Opsi renderer dikembalikan setelah pengujian. Buffer crash kosong setelah smoke test; riwayat proses hanya berisi force-stop/pembaruan paket yang disengaja, tanpa crash atau ANR.
- Lint masih melaporkan 5 error lama pada konfigurasi/kalender/preferensi sumber yang tidak diubah, beserta 101 peringatan. Tugas Gradle lint tidak gagal akibat laporan tersebut. Hasil emulator ini tidak menjamin kompatibilitas setiap perangkat fisik atau shader khusus.

## Fitur privasi dan membaca — 5 November 2026

APK debug dibangun dengan pemrosesan native nonaktif menggunakan `-Penable-rust=false --offline`.

- Gradle menyelesaikan `spotlessCheck`, `:app:assembleDebug`, `:app:testDebugUnitTest`, `:domain:testDebugUnitTest`, dan `:data:verifySqlDelightMigration`.
- Tes aplikasi: 55 lulus, 2 dilewati. Tes domain: 122 lulus. Tidak ada kegagalan tes.
- `python -m unittest discover -s tools/tests`: 3 tes integrasi SQLite lulus. Tes menjalankan query riwayat dan pembaruan widget yang sebenarnya, kategori tersembunyi/terlihat yang bertumpang tindih, kategori bawaan tersembunyi, pengecualian durasi baca, migrasi 20 yang mempertahankan riwayat, deduplikasi chapter selesai, serta penghapusan melalui foreign key.
- Unit test mencakup urutan dan deduplikasi carousel, jeda streak, pemilihan tahun, pemisahan sesi tengah malam/zona waktu, konsistensi durasi saat DST, dan penyamaran log diagnostik.
- Emulator Android x86_64 memverifikasi autentikasi PIN perangkat, penolakan akses tanpa kredensial, penguncian otomatis setelah masuk latar belakang, serta hilangnya judul tersembunyi yang juga berada di kategori publik dari pustaka, carousel, riwayat, dan rekap.
- Ekspor log membuka menu berbagi Android dengan `komikita_diagnostics.txt` yang berisi data. Lampiran demo mempertahankan versi aplikasi dan log terbaru; pemeriksaan tidak menemukan URL HTTP mentah, kolom ID instalasi, atau judul demo tersembunyi.
- Screenshot aplikasi asli diambil dan diperiksa untuk kedua README. Skrip penyiapan dan petunjuk pengambilan ada di [tools/demo](../../tools/demo/README.md).
- Android lint selesai, tetapi laporannya masih memuat 5 error dan 98 peringatan. Error berada di berkas yang tidak berubah: satu escape jalur SDK pada `local.properties` yang diabaikan Git, tiga pembacaan locale non-observable pada kalender yang ada, dan satu tombol khusus AppCompat di preferensi sumber. Masalah locale pada rekap baru dan peringatan penamaan layar kunci telah diperbaiki.

Perangkat biometrik, notifikasi pembaruan dari sumber langsung, dan widget yang dipasang di launcher belum diuji pada perangkat fisik. Integrasinya memakai prompt biometrik Android dan query visibilitas bersama; query widget yang sebenarnya sudah diuji dengan SQLite. Validasi ini tidak membangun APK release atau menguji ulang pemrosesan native. Aktivitas rekap harian dimulai dari versi ini dan belum disertakan dalam cadangan.

## Validasi terdahulu — 2 November 2026

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
