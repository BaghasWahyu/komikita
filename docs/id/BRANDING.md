[English](../BRANDING.md) | **Bahasa Indonesia**

# Identitas KomiKita

- Nama produk: **KomiKita**.
- Repository: https://github.com/BaghasWahyu/komikita
- Application ID: `app.komikita`.
- Logo: buku terbuka dengan monogram K; hijau mint #83E8BE dan tinta #102D2B. Varian debug memakai biru.
- Sumber vektor: `.github/assets/logo.svg`; launcher memakai VectorDrawable, adaptive icon, dan monochrome icon.
- Logo dipakai untuk splash, notifikasi, Tentang, IDE, README, dan metadata toko.

Tautan proyek, Issues, privasi, user agent, serta artefak rilis mengarah ke KomiKita. Bantuan teknis bersama masih memakai dokumentasi Mihon sebagai dokumentasi upstream.

## Bahasa dokumentasi

Bahasa Inggris menjadi bahasa utama deskripsi dan dokumentasi proyek. Versi Bahasa Indonesia tersedia melalui tautan bahasa di bagian atas setiap dokumen terjemahan. Metadata toko memakai direktori terpisah untuk bahasa Inggris (`en-US`) dan Bahasa Indonesia (`id`). Nama produk, nama package, identifier, dan opsi command-line tidak diubah saat menerjemahkan.

## Kompatibilitas

Package source `eu.kanade.*`, `tachiyomi.*`, dan `mihon.*` bukan application ID. KomiKita dapat dipasang berdampingan dengan Mihon.

Callback `mihon://` dan `app.mihon://comick-auth` tetap diterima karena telah terdaftar pada penyedia OAuth. Menggantinya memerlukan client KomiKita pada masing-masing penyedia. Skema `komikita://extension-store` tersedia untuk tautan KomiKita.

Metadata SourceMihon, kontrak ekstensi, kunci preferensi, dan format backup dipertahankan. Hak cipta, dependensi, dan changelog upstream bukan klaim identitas KomiKita.
