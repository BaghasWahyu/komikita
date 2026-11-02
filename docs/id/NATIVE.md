[English](../NATIVE.md) | **Bahasa Indonesia**

# Rust pada KomiKita

Modul `native/` menghitung chapter yang hilang secara batch menggunakan array primitif. `core:native` menyediakan bridge JNI; `domain` memakai native mulai 512 chapter. Daftar kecil atau kegagalan memuat library memakai implementasi Kotlin.

Algoritma mengabaikan nomor negatif, NaN, infinity, dan angka di luar rentang Int. Pecahan memiliki nomor dasar yang sama. Penjumlahan memakai 64 bit untuk mencegah overflow.

## Toolchain

```sh
rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
cargo install cargo-ndk --version 4.1.2 --locked
cargo test --manifest-path native/Cargo.toml --locked
cargo clippy --manifest-path native/Cargo.toml --all-targets --locked
cargo bench --manifest-path native/Cargo.toml --locked
```

Jika memakai NDK lain yang sudah terpasang, tambahkan `-Prust-ndk-path=/path/to/ndk` pada perintah Gradle.

`BuildRustTask` menjalankan cargo-ndk untuk empat ABI dan minimum Android API 26. JNI libraries dimasukkan sebagai generated source pada Android library. Untuk build tanpa Rust, gunakan `-Penable-rust=false`.

Native tidak menjalankan timer, thread latar belakang, atau permintaan jaringan. Buffer JNI disalin selama pemanggilan, tidak disimpan; error mengembalikan sentinel untuk fallback Kotlin. Panic ditahan pada batas FFI.

Benchmark Rust membandingkan array berurutan dengan deduplikasi berbasis HashSet. Angka ini tidak menyatakan peningkatan kecepatan aplikasi atau penghematan baterai di perangkat. Ukur jalur JNI dan sesi membaca pada perangkat nyata sebelum menyimpulkan dampak.

Referensi: [cargo-ndk](https://github.com/bbqsrc/cargo-ndk), [JNI crate 0.21.1](https://docs.rs/jni/0.21.1/jni/).
