**English** | [Bahasa Indonesia](id/NATIVE.md)

# Rust in KomiKita

The `native/` module calculates missing chapters in batches using primitive arrays. `core:native` provides the JNI bridge; `domain` uses the native implementation for lists of 512 chapters or more. Smaller lists, or a failure to load the library, use the Kotlin implementation.

The algorithm ignores negative numbers, NaN, infinity, and values outside the Int range. Fractional chapters share their base chapter number. Totals use 64-bit arithmetic to prevent overflow.

## Toolchain

```sh
rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
cargo install cargo-ndk --version 4.1.2 --locked
cargo test --manifest-path native/Cargo.toml --locked
cargo clippy --manifest-path native/Cargo.toml --all-targets --locked
cargo bench --manifest-path native/Cargo.toml --locked
```

To use another installed NDK, add `-Prust-ndk-path=/path/to/ndk` to the Gradle command.

`BuildRustTask` runs cargo-ndk for four ABIs with a minimum Android API level of 26. JNI libraries are included as generated sources in the Android library. To build without Rust, use `-Penable-rust=false`.

The native module does not run timers, background threads, or network requests. JNI buffers are copied during a call and are not retained. Errors return a sentinel value to trigger the Kotlin fallback. Panics are caught at the FFI boundary.

The Rust benchmark compares sorted arrays with HashSet-based deduplication. These numbers do not establish app speed improvements or battery savings on a device. Measure the JNI path and reading sessions on a physical device before drawing conclusions about their impact.

References: [cargo-ndk](https://github.com/bbqsrc/cargo-ndk), [JNI crate 0.21.1](https://docs.rs/jni/0.21.1/jni/).
