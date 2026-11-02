**English** | [Bahasa Indonesia](id/PERFORMANCE.md)

# KomiKita performance and battery usage

## Reader inspection

These findings come from code inspection, not battery measurements on a user's device. The reported symptoms occur while reading.

- The reader previously did not request a lower display refresh rate. On 90/120 Hz displays, scrolling and animations can keep a high refresh rate active. This is a possible source of power use, not a cause confirmed by a device trace.
- Screen brightness, white pages on OLED displays, shaders/HDR, animations, and downloading pages over the network can increase power use while reading. The effect depends on the device and settings.
- The WebGPU renderer is not enabled by default. Progress saving is already delayed by 2 seconds, and keeping the screen on is disabled by default. These settings do not establish that continuous polling is taking place.
- Image animations in the WebGPU dependency are already suspended when a page leaves the screen. An animation loop does not establish that all preloaded pages keep rendering.

## Changes

**Save battery while reading** is enabled by default. The reader chooses a mode at the current screen resolution, preferring refresh rates up to 60 Hz. If no mode up to 60 Hz is supported, it uses the lowest available mode. The previous settings are restored when the reader pauses or is disposed. Turn this setting off to use higher refresh rates while scrolling.

The Rust module only processes chapter statistics in batches and does not create background work. The Kotlin implementation also uses primitive arrays to reduce temporary collections. Neither change is claimed to reduce battery use during reading sessions.

## Device verification

Use a physical device with the same comic, brightness, connection, session length, and reader mode. Compare 15–30 minute sessions with battery saver enabled and disabled. Repeat with an offline comic to separate network costs.

Use Android Studio Power Profiler or System Trace/Perfetto to inspect display refresh rates, frames, CPU/GPU activity, and networking. Also check shader/HDR settings and manual brightness. Do not reset the user's battery statistics for routine verification.

Without device traces, the percentage of battery savings and the root cause on a particular device remain unconfirmed.

## Native

See the [native module guide](NATIVE.md) for the Rust toolchain and benchmarks. Host benchmarks only compare algorithms and do not represent the performance of the Android application as a whole.
