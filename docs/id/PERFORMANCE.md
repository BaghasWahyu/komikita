[English](../PERFORMANCE.md) | **Bahasa Indonesia**

# Performa dan penggunaan baterai KomiKita

## Pemeriksaan pembaca

Pemeriksaan ini berasal dari kode, bukan pengukuran konsumsi baterai pada perangkat pengguna. Gejala yang dilaporkan terjadi saat membaca.

- Pembaca sebelumnya tidak meminta mode layar dengan refresh rate rendah. Pada layar 90/120 Hz, scrolling dan animasi dapat mempertahankan mode refresh tinggi. Ini kandidat penguras daya, bukan penyebab yang telah dibuktikan oleh trace perangkat.
- Kecerahan layar, halaman putih pada OLED, shader/HDR, animasi, serta pengunduhan halaman melalui jaringan dapat menambah konsumsi saat membaca. Pengaruhnya bergantung perangkat dan pengaturan.
- Renderer WebGPU tidak aktif secara bawaan. Menyimpan progres sudah ditunda 2 detik, dan opsi menjaga layar menyala juga mati secara bawaan. Jangan menyimpulkan bahwa semuanya menyebabkan polling tanpa jeda.
- Animasi gambar pada dependensi WebGPU sudah ditahan saat halaman keluar dari layar; loop animasi bukan bukti bahwa semua halaman preload terus bekerja.

## Perubahan

Opsi **Hemat baterai saat membaca** aktif secara bawaan. Pembaca memilih mode pada resolusi layar saat ini, mengutamakan refresh rate hingga 60 Hz. Jika tidak ada mode hingga 60 Hz yang didukung, mode terendah yang tersedia dipakai. Pengaturan semula dipulihkan saat pembaca pause atau dilepas. Opsi dapat dimatikan untuk scrolling pada refresh rate tinggi.

Modul Rust hanya memproses batch statistik chapter dan tidak membuat pekerjaan latar belakang. Implementasi Kotlin juga memakai array primitif untuk mengurangi koleksi sementara. Ini tidak diklaim menghemat baterai sesi membaca.

## Verifikasi perangkat

Gunakan perangkat nyata dengan komik, kecerahan, koneksi, durasi, dan mode pembaca yang sama. Bandingkan opsi hemat baterai aktif/nonaktif dalam sesi 15–30 menit. Ulangi dengan komik offline untuk memisahkan biaya jaringan.

Gunakan Android Studio Power Profiler atau System Trace/Perfetto untuk memeriksa frekuensi layar, frame, CPU/GPU, dan jaringan. Periksa juga konfigurasi shader/HDR serta kecerahan manual. Jangan mereset statistik baterai pengguna untuk verifikasi rutin.

Tanpa trace perangkat, persentase penghematan dan akar masalah spesifik perangkat belum dapat dipastikan.

## Native

Lihat [NATIVE.md](NATIVE.md) untuk toolchain dan benchmark Rust. Benchmark host hanya membandingkan algoritma dan tidak mewakili kecepatan seluruh aplikasi Android.
