# README screenshot demo

The screenshots in `.github/assets/screenshots/` are captures of the Android application. The four sample titles and geometric covers are original demonstration material, not downloaded comics.

1. Start an Android emulator and install a debug APK built with `./gradlew :app:assembleDebug -Penable-rust=false`.
2. Launch the debug app once and let the welcome screen load, so the database and preference directories exist.
3. With Python 3, Pillow, and `adb` on the PATH, run `python tools/demo/seed_demo.py`. The script only targets `app.komikita.dev` on an emulator and refuses a nonempty library. It preserves the empty installation's schema and adds four sample titles, reading history, and daily activity.
4. To match the captures, set the app language to Indonesian using Android's app-language settings or `adb shell cmd locale set-app-locales app.komikita.dev --user 0 --locales id`.
5. Capture the library and **More → Statistics**. For the privacy screen, add a title to the **Pribadi** category, hide that category under **More → Categories**, then return after putting the app in the background. Device credentials are configured through Android; the script does not set a PIN.

Capture with `android screen capture -o <path>`. Keep screenshots unmodified and inspect every capture before adding it to the README. Demo activity uses the capture date, so recap totals will vary when the script is run again.
