"""Seed an EMPTY KomiKita debug installation on an Android emulator for README captures.

Requires Python 3, Pillow, adb, and a running emulator. Never targets a release app or physical device.
Build/install/launch the debug APK once before running this script.
"""

import hashlib
import io
from pathlib import Path
import sqlite3
import subprocess
import tempfile
from datetime import datetime, timedelta, timezone
from xml.etree import ElementTree as ET

from PIL import Image, ImageDraw, ImageFont

PACKAGE = "app.komikita.dev"
MAIN = "eu.kanade.tachiyomi.ui.main.MainActivity"


def adb(*args, data=None):
    return subprocess.run(["adb", "-e", *args], input=data, check=True, capture_output=True, timeout=30).stdout


def cover(title, index):
    colors = [("#174d47", "#e4bc7e"), ("#233a57", "#e8a064"), ("#6d3635", "#f0c99d"), ("#31566e", "#dbd1ac")]
    background, ink = colors[index]
    image = Image.new("RGB", (600, 900), background)
    draw = ImageDraw.Draw(image)
    # Original geometric artwork, with no third-party covers or comic pages.
    draw.ellipse((180, 140, 580, 540), fill=ink)
    for n in range(6):
        draw.polygon([(0, 450 + n * 35), (250, 270 + n * 42), (600, 550 + n * 25), (600, 900), (0, 900)],
                     fill=background if n % 2 == 0 else ink)
    draw.rectangle((0, 620, 600, 900), fill=background)
    font_path = Path("C:/Windows/Fonts/arialbd.ttf")
    font = ImageFont.truetype(str(font_path), 58) if font_path.exists() else ImageFont.load_default(size=50)
    small = ImageFont.truetype(str(font_path), 22) if font_path.exists() else ImageFont.load_default(size=22)
    draw.text((40, 45), "KOMIKITA / DEMO", font=small, fill=ink)
    draw.multiline_text((40, 660), title, font=font, fill="#fff7e9", spacing=8)
    output = io.BytesIO()
    image.save(output, format="PNG")
    return output.getvalue()


def main():
    serial = adb("get-serialno").decode().strip()
    if not serial.startswith("emulator-"):
        raise SystemExit("Use an emulator only.")
    adb("shell", "am", "force-stop", PACKAGE)
    with tempfile.TemporaryDirectory(prefix="komikita-demo-") as temp:
        database_path = Path(temp) / "tachiyomi.db"
        database_path.write_bytes(adb("exec-out", "run-as", PACKAGE, "cat", "databases/tachiyomi.db"))
        try:
            database_path.with_name("tachiyomi.db-wal").write_bytes(
                adb("exec-out", "run-as", PACKAGE, "cat", "databases/tachiyomi.db-wal"))
        except subprocess.CalledProcessError:
            pass
        database = sqlite3.connect(database_path)
        database.execute("PRAGMA wal_checkpoint(TRUNCATE)")
        database.execute("PRAGMA journal_mode=DELETE")
        if not database.execute("SELECT name FROM sqlite_master WHERE name='manga'").fetchone():
            database.close()
            raise SystemExit("Launch the debug app and let it finish initializing before running this script.")
        if database.execute("SELECT count(*) FROM manga").fetchone()[0]:
            database.close()
            raise SystemExit("Refusing to modify a nonempty library. Use a fresh debug installation.")
        now = datetime.now(timezone.utc)
        millis = lambda value: int(value.timestamp() * 1000)
        titles = ["Senja di Stasiun", "Orbit Ketujuh", "Kota Setelah Hujan", "Laut di Atas Awan"]
        cover_titles = ["SENJA DI\nSTASIUN", "ORBIT\nKETUJUH", "KOTA SETELAH\nHUJAN", "LAUT DI\nATAS AWAN"]
        database.execute('INSERT INTO category VALUES (1, "Koleksi", 0, 0)')
        database.execute('INSERT INTO category VALUES (2, "Pribadi", 1, 0)')
        for index, title in enumerate(titles):
            manga_id = index + 1
            database.execute("""INSERT INTO manga VALUES (
                ?, 999999, ?, ?, 'KomiKita Demo', 'KomiKita Demo',
                'Original sample title for app demonstrations.', 'Adventure', 1, ?, 0, ?,
                ?, '', 0, 0, ?, ?, 7, ?, 1)""",
                (manga_id, f"demo/{manga_id}", title, f"https://demo.invalid/{manga_id}.png", b"{}",
                 millis(now - timedelta(days=90)), millis(now), millis(now + timedelta(days=7)), millis(now)))
            database.execute("INSERT INTO manga_category(manga_id, category_id) VALUES (?, 1)", (manga_id,))
            for chapter in range(1, 13):
                chapter_id = manga_id * 100 + chapter
                read = chapter <= [8, 5, 3, 1][index]
                database.execute("""INSERT INTO chapter VALUES (?, ?, ?, ?, NULL, ?, ?, ?, ?, ?, 0, 0, ?)""",
                    (chapter_id, manga_id, f"demo/{manga_id}/{chapter}", f"Chapter {chapter}", float(chapter),
                     millis(now - timedelta(days=12 - chapter)), 12 - chapter, b"{}", int(read), millis(now)))
                if read:
                    day = now - timedelta(days=(8 - chapter) % 5)
                    database.execute("INSERT INTO history(manga_id, chapter_id, read_at, read_duration) VALUES (?, ?, ?, ?)",
                                     (manga_id, chapter_id, millis(day), 240_000))
                    database.execute("INSERT INTO reading_activity VALUES (?, ?, 240000, 1)",
                                     (chapter_id, day.astimezone(timezone(timedelta(hours=7))).date().isoformat()))
            data = cover(cover_titles[index], index)
            target = f"/sdcard/Android/data/{PACKAGE}/files/covers/custom"
            adb("shell", "mkdir", "-p", target)
            image_path = Path(temp) / "cover.png"
            image_path.write_bytes(data)
            adb("push", str(image_path), target + "/" + hashlib.md5(str(manga_id).encode()).hexdigest())
        database.commit()
        database.close()
        adb("shell", "run-as", PACKAGE, "rm", "-f", "databases/tachiyomi.db-wal", "databases/tachiyomi.db-shm")
        adb("push", str(database_path), "/data/local/tmp/komikita-demo.db")
        adb("shell", "run-as", PACKAGE, "cp", "/data/local/tmp/komikita-demo.db", "databases/tachiyomi.db")
    prefs = ET.Element("map")
    ET.SubElement(prefs, "boolean", name="__APP_STATE_onboarding_complete", value="true")
    ET.SubElement(prefs, "string", name="pref_app_theme").text = "TEALTURQUOISE"
    ET.SubElement(prefs, "string", name="pref_theme_mode_key").text = "LIGHT"
    with tempfile.TemporaryDirectory(prefix="komikita-prefs-") as temp:
        prefs_path = Path(temp) / "preferences.xml"
        prefs_path.write_bytes(ET.tostring(prefs, encoding="utf-8", xml_declaration=True))
        adb("push", str(prefs_path), "/data/local/tmp/komikita-demo-prefs.xml")
        adb("shell", "run-as", PACKAGE, "cp", "/data/local/tmp/komikita-demo-prefs.xml",
            f"shared_prefs/{PACKAGE}_preferences.xml")
    adb("shell", "rm", "-f", "/data/local/tmp/komikita-demo.db", "/data/local/tmp/komikita-demo-prefs.xml")
    adb("shell", "am", "start", "-n", PACKAGE + "/" + MAIN)
    print("Seeded four original demo titles in the empty emulator debug app.")


if __name__ == "__main__":
    main()
