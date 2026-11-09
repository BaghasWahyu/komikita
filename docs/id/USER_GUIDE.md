[English](../USER_GUIDE.md) | **Bahasa Indonesia**

# Panduan KomiKita

## Menyembunyikan kategori

Buka **Lainnya → Kategori** dan tekan ikon mata pada kategori. Tab serta seluruh komik yang termasuk kategori itu disembunyikan dari perpustakaan dan pencarian perpustakaan. Komik yang juga ada pada kategori lain tetap disembunyikan selama salah satu kategorinya tersembunyi.

Judul tersembunyi juga hilang dari Lanjutkan membaca, riwayat, pembaruan, notifikasi pembaruan, widget, dan rekap tahunan, termasuk jika judul itu juga ada di kategori yang terlihat. Menyembunyikan tidak menghapus komik atau unduhan.

Kunci kategori aktif secara bawaan di **Pengaturan → Keamanan dan privasi → Kunci kategori tersembunyi**. Siapkan layar kunci Android terlebih dahulu. KomiKita memakai prompt biometrik Android dengan PIN, pola, atau sandi perangkat sebagai alternatif; tidak ada PIN terpisah yang disimpan aplikasi. Jika autentikasi tidak tersedia atau dibatalkan, kategori tetap terkunci.

Untuk membuka sementara, pilih menu titik tiga di pustaka → **Buka kategori tersembunyi**, lalu autentikasi. Menu yang sama juga dapat menguncinya kembali. Kategori terkunci lagi saat aplikasi masuk latar belakang. Screenshot diblokir selama sesi terbuka. Judul tersembunyi tetap tidak ditampilkan di bagian lain yang disebut di atas selama sesi tersebut.

Untuk menampilkan kategori secara permanen, buka **Lainnya → Kategori**, autentikasi, lalu tekan ikon mata lagi. Mengubah pengaturan kunci kategori juga memerlukan autentikasi.

## Antrean pembaruan kategori

Di pustaka, buka kategori lalu pilih **Perbarui kategori** dari menu titik tiga, atau tarik ke bawah untuk memperbarui. Saat pembaruan berjalan, pindah ke kategori lain dan ulangi untuk menambahkannya ke antrean. Kategori diproses sesuai urutan penambahan, dan area progres menampilkan jumlah kategori yang menunggu. Kategori yang sedang berjalan atau menunggu tidak dapat ditambahkan lagi.

Antrean disimpan di perangkat. Tombol **Batal** pada notifikasi pembaruan menghentikan proses saat ini beserta seluruh kategori yang menunggu. Filter pembaruan yang sudah diatur tetap berlaku pada setiap kategori.

## Lanjutkan membaca

Geser carousel di pustaka untuk melihat hingga lima judul yang terlihat dan memiliki chapter belum dibaca. Rekomendasi pertama mempertahankan urutan sebelumnya: judul yang terakhir dibuka selama masih punya chapter belum dibaca, kemudian judul dengan pembaruan terbaru. Kartu berikutnya menampilkan bacaan dan pembaruan lain. Tekan kartu untuk membuka chapter berikutnya yang belum dibaca. Filter pencarian dan kategori juga berlaku pada carousel.

Notifikasi chapter baru menyediakan tombol **Lanjutkan membaca** yang membuka chapter belum dibaca pertama sesuai urutan chapter judul tersebut. Widget pembaruan di layar utama mengikuti aturan kategori tersembunyi yang sama.

## Statistik membaca dan rekap tahunan

Buka **Lainnya → Statistik**. Kartu tahun bacaan menampilkan chapter selesai, waktu baca, jumlah judul, streak saat ini, streak terpanjang pada tahun pilihan, hari aktif, tiga judul teratas berdasarkan waktu baca, dan waktu baca bulanan. Tekan tahun untuk memilih tahun lain yang memiliki data.

Aktivitas harian mulai dicatat dari versi ini; waktu baca lama tidak diberi tanggal perkiraan. Chapter dihitung sekali per tahun ketika diselesaikan di pembaca. Menandai chapter dibaca secara manual tidak membuat sesi membaca. Streak saat ini masih berlanjut jika terakhir membaca kemarin, hingga hari ini berakhir. Sesi melewati tengah malam dibagi menurut tanggal lokal perangkat. Sesi mode penyamaran tidak dicatat; judul tersembunyi dikecualikan dari rekap.

Aktivitas harian disimpan lokal dan belum disertakan dalam format cadangan yang ada. Menghapus seluruh riwayat juga menghapus aktivitas harian beserta rekap.

## Ekspor log diagnostik

Buka **Pengaturan → Lanjutan → Ekspor log diagnostik** setelah mereproduksi bug. Menu berbagi Android menawarkan lampiran `komikita_diagnostics.txt` yang berisi detail aplikasi dan perangkat, diagnostik ekstensi, serta hingga 2.000 entri terbaru dari proses aplikasi. URL, kolom kredensial umum, ID instalasi, dan judul tersembunyi disamarkan. Periksa lampiran sebelum menyertakannya dalam laporan bug.

## Menyembunyikan ekstensi

Buka **Jelajah → Ekstensi**, lalu tekan ikon mata pada ekstensi. Ekstensi serta sumbernya disembunyikan dari daftar sumber dan pencarian global. Instalasi dan komik yang sudah ditambahkan tetap tersedia.

Untuk memulihkan, buka menu titik tiga pada tab Ekstensi, pilih **Tampilkan ekstensi tersembunyi**, lalu tekan ikon mata pada ekstensi yang ingin ditampilkan.

## Ikon ekstensi pada komik

Di perpustakaan, buka pengaturan tampilan → **Tampilan → Tampilkan ikon ekstensi**. Badge tersedia pada grid ringkas, grid nyaman, tampilan sampul, dan daftar. Ikon diambil dari ekstensi yang sudah terpasang. Jika sumber sudah tidak tersedia, ikon sumber bawaan ditampilkan.

## Hemat baterai saat membaca

Buka pengaturan pembaca atau pengaturan umum dari dalam pembaca, lalu aktifkan **Hemat baterai saat membaca**. Opsi aktif secara bawaan dan mengutamakan mode layar hingga 60 Hz. Matikan jika menginginkan scrolling dengan refresh rate tinggi.

## Memeriksa pembaruan aplikasi

APK dari workflow rilis GitHub, termasuk FOSS, memeriksa Rilis KomiKita saat aplikasi dibuka dan setiap enam jam selama tetap digunakan. Untuk memeriksa secara manual, buka **Lainnya → Tentang → Periksa pembaruan**. Saat versi yang lebih baru tersedia, aplikasi menampilkan catatan rilis dan menawarkan unduhan serta instalasi. Konfirmasikan instalasi saat Android memintanya.

Hanya rilis stabil yang sudah dipublikasikan, memiliki versi lebih baru, dan menyediakan APK yang sesuai yang ditawarkan. Jika APK yang terpasang memiliki updater nonaktif, pasang APK dengan updater aktif dari [Rilis KomiKita](https://github.com/BaghasWahyu/komikita/releases) secara manual satu kali.

## Berpindah dari Mihon

KomiKita memiliki ID instalasi tersendiri dan dapat dipasang bersama Mihon. Untuk memindahkan perpustakaan, buat backup di aplikasi lama lalu pulihkan melalui menu data dan penyimpanan KomiKita. Ekstensi privat dan login layanan mungkin perlu disiapkan kembali.

Kredit lengkap basis kode tersedia di [CREDITS.md](../../CREDITS.id.md).
