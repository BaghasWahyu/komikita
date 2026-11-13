**English** | [Bahasa Indonesia](id/USER_GUIDE.md)

# KomiKita user guide

## Hiding categories

Open **More → Categories** and tap a category's eye icon. The category tab and all comics belonging to that category are hidden from the library and library search. A comic that also belongs to another category remains hidden while any of its categories is hidden.

Hidden titles also disappear from Continue reading, history, updates, update notifications, widgets, and yearly reading recaps, even when a title also belongs to a visible category. Hiding does not delete comics or downloads.

Category locking is enabled by default in **Settings → Security and privacy → Lock hidden categories**. Set up an Android screen lock first. KomiKita uses the system biometric prompt with the device PIN, pattern, or password as a fallback; it does not store a separate PIN. If authentication is unavailable or cancelled, categories remain locked.

To reveal them temporarily, open the library's three-dot menu → **Unlock hidden categories**, then authenticate. The menu also lets you lock them immediately. They lock again when the app enters the background. While revealed, screenshots are blocked. Hidden titles remain excluded from the other surfaces above during the unlocked session.

To make a category visible permanently, open **More → Categories**, authenticate, then tap its eye icon again. Changing the category-lock setting also requires authentication.

## Adding categories to an update

In the library, open a category and choose **Update category** from the three-dot menu, or pull down to refresh. While the update runs, switch to another category and repeat to add its eligible titles to the same update. The progress total grows as new titles are added. Titles shared by multiple categories are checked once.

Up to five sources are checked at once, with one title at a time per source. **Cancel** in the update notification stops the entire session. Categories added during a session are kept in memory and are lost if Android kills the app process.

## Continue reading

Swipe the library carousel to see up to five visible titles with unread chapters. The first recommendation keeps the existing priority: your latest opened title while it has unread chapters, otherwise the most recently updated title. Other cards show recent reads and updates. Tap a card to open its next unread chapter. Search and category filters apply to the carousel.

New chapter notifications include **Continue reading**, which opens the first unread chapter according to the title's chapter order. Home-screen update widgets follow the same hidden-category exclusions.

## Reading statistics and yearly recap

Open **More → Statistics**. The reading-year card shows completed chapters, reading time, titles read, current streak, best streak for the selected year, active reading days, top three titles by time, and monthly reading time. Tap the year to select another recorded year.

Daily activity starts with this version; older aggregate history is not assigned invented reading dates. A chapter is counted once per selected year when completed in the reader. Marking chapters read manually does not create reading sessions. A current streak continues through yesterday until today ends. Sessions crossing midnight are split by the device's local calendar date. Incognito sessions are not recorded; hidden titles are excluded from recaps.

Daily activity is stored locally and is not included in the existing backup format. Clearing all history also clears the daily activity and recap.

## Exporting diagnostic logs

Open **Settings → Advanced → Export diagnostic logs** after reproducing a bug. Android's share sheet offers a `komikita_diagnostics.txt` attachment containing app and device details, extension diagnostics, and up to 2,000 recent entries from the app process. URLs, common credential fields, the installation ID, and hidden titles are redacted. Review the attachment before attaching it to your bug report.

## Hiding extensions

Open **Browse → Extensions**, then tap an extension's eye icon. The extension and its sources are hidden from the source list and global search. The extension remains installed, and comics already added to your library remain available.

To show an extension again, open the three-dot menu on the Extensions tab, choose **Show hidden extensions**, then tap the eye icon on the extension you want to show.

## Extension icons on comics

In the library, open display settings → **Display → Show extension icons**. Badges are available in compact grid, comfortable grid, cover-only, and list layouts. Icons come from installed extensions. If a source is no longer available, a default source icon is shown.

## Saving battery while reading

Open reader settings, or the general settings page inside the reader, and enable **Save battery while reading**. This setting is enabled by default and prefers display modes up to 60 Hz. Turn it off if you prefer scrolling at a higher refresh rate.

## Checking for app updates

APKs built by the GitHub release workflow, including FOSS, check KomiKita Releases when the app opens and every six hours while it remains in use. To check manually, open **More → About → Check for updates**. When a newer version is available, the app shows its release notes and offers to download and install it. Confirm installation when Android prompts you.

Only published stable releases with a newer version and a compatible APK are offered. If your installed APK has the updater disabled, install an updater-enabled APK from [KomiKita Releases](https://github.com/BaghasWahyu/komikita/releases) manually once.

## Moving from Mihon

KomiKita has its own application ID and can be installed alongside Mihon. To move your library, create a backup in the old app and restore it through KomiKita's data and storage settings. Private extensions and service logins may need to be set up again.

Full credits for the codebase are available in [Credits and acknowledgements](../CREDITS.md).
