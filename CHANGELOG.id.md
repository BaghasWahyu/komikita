[English](CHANGELOG.md) | **Bahasa Indonesia**

# Changelog KomiKita

## Belum dirilis

- Gabungkan Mihon upstream hingga `ede052af1`, memperbarui WebGPU viewer ke versi 57 dengan pengaturan rendering cepat, tampilan tiles akhir, serta upscaler/downscaler khusus. Pertahankan dependensi XML KomiKita yang lebih baru dan impor resource langsung.
- Lindungi kategori tersembunyi dengan biometrik atau kredensial perangkat Android, dengan penguncian otomatis di latar belakang dan perlindungan akses langsung ke pembaca.
- Kecualikan judul tersembunyi dari Lanjutkan membaca, riwayat, pembaruan, notifikasi, widget, dan rekap membaca, termasuk judul yang juga berada di kategori terlihat.
- Geser hingga lima rekomendasi Lanjutkan membaca dan lanjutkan bacaan dari notifikasi chapter baru.
- Ekspor log diagnostik berisi detail aplikasi dan perangkat, dengan batas waktu pengumpulan serta penyamaran URL, kredensial, dan judul tersembunyi.
- Catat aktivitas membaca harian untuk streak saat ini dan terpanjang, chapter selesai tahunan, waktu baca, judul teratas, dan rekap bulanan. Aktivitas harian dimulai dari versi ini dan belum disertakan dalam cadangan.
- Tambahkan screenshot emulator orisinal pada kedua README dan skrip demo yang dapat dijalankan ulang.

## 1.0.6 — 2026-11-03

### Perpustakaan

- Kartu Lanjutkan Membaca tetap memilih komik yang terakhir dibuka selama masih memiliki chapter yang belum dibaca.
- Setelah seluruh chapter selesai dibaca, kartu beralih ke komik belum dibaca dengan pembaruan chapter terbaru. Sumber tanpa tanggal unggah menggunakan waktu pengambilan chapter sebagai cadangan.
- Rekomendasi mengikuti komik yang terlihat pada koleksi dan filter yang sedang digunakan.

### Perbaikan

- **Periksa pembaruan** secara manual kini meminta informasi rilis terbaru dan tidak memakai ulang respons cache.

## 1.0.0 — 2026-11-02

- Identitas KomiKita untuk nama aplikasi, ikon, splash, notifikasi, dokumentasi, metadata toko, dan rilis.
- Application ID terpisah: app.komikita.
- Telemetry mati secara bawaan; APK rilis GitHub, termasuk FOSS, mengaktifkan pembaruan aplikasi dari repositori KomiKita. Build lokal dan CI tetap menonaktifkan updater secara bawaan.
- Sembunyikan kategori beserta komiknya dan ekstensi beserta sumbernya.
- Opsi badge ikon ekstensi di perpustakaan.
- Modul Rust untuk pemrosesan chapter dengan fallback Kotlin.
- Opsi hemat baterai pembaca dengan pemilihan refresh rate layar.
- Perbandingan versi, validasi nomor chapter, dan manifest telemetry diperbaiki.
- Dokumentasi proyek berbahasa Inggris dengan tautan versi Bahasa Indonesia serta deskripsi toko dalam kedua bahasa.

Riwayat sebelum fork tersedia pada [changelog upstream](docs/UPSTREAM_CHANGELOG.md).
