"""Exercise the shipped SQL with SQLite, including the upgrade from the previous schema.

Run: python -m unittest discover -s tools/tests
"""

from pathlib import Path
import re
import sqlite3
import unittest

SQL = Path(__file__).resolve().parents[2] / "data/src/main/sqldelight/tachiyomi"


def statements(path):
    return re.sub(r"(?m)^import .*;\n", "", path.read_text(encoding="utf-8"))


def schema(path):
    text = re.split(r"(?m)^\w+:\s*$", statements(path))[0]
    return re.sub(r" AS (?:Instant|Boolean|Int|JsonObject|UpdateStrategy|List<String>)", "", text)


def query(path, name):
    return statements(path).split(name + ":\n", 1)[1].split(";", 1)[0]


def insert_required(database, table, **values):
    for _, column, kind, required, default, _ in database.execute(f"PRAGMA table_info({table})"):
        if column not in values and required and default is None:
            values[column] = b"{}" if kind == "BLOB" else "" if kind == "TEXT" else 0
    columns = ",".join(values)
    database.execute(f"INSERT INTO {table}({columns}) VALUES ({','.join('?' for _ in values)})", list(values.values()))


class ReadingPrivacySqlTest(unittest.TestCase):
    def setUp(self):
        self.database = sqlite3.connect(":memory:")
        self.addCleanup(self.database.close)
        self.database.execute("PRAGMA foreign_keys=ON")
        for table in ["manga", "chapter", "category", "manga_category", "history", "excluded_scanlator"]:
            self.database.executescript(schema(SQL / f"data/{table}.sq"))
        for view in ["historyView", "updatesView"]:
            self.database.executescript(schema(SQL / f"view/{view}.sq"))
        self.database.execute('INSERT INTO category VALUES (1,"Public",0,0),(2,"Private",1,0)')
        for manga_id in [1, 2, 3]:
            insert_required(self.database, "manga", id=manga_id, remote_url=f"/{manga_id}",
                            remote_title=f"Title {manga_id}", user_favorite_at=1)
            insert_required(self.database, "chapter", id=manga_id, manga_id=manga_id,
                            remote_url=f"/{manga_id}/1", remote_date_upload=10, state_date_fetch=20)
            insert_required(self.database, "history", id=manga_id, manga_id=manga_id,
                            chapter_id=manga_id, read_at=30, read_duration=100)
        self.database.execute("INSERT INTO manga_category(manga_id,category_id) VALUES (1,1),(1,2),(2,1)")

    def visible_history(self, hidden):
        sql = query(SQL / "view/historyView.sq", "history").replace(":hiddenCategories", f"({hidden})")
        return [row[1] for row in self.database.execute(sql, {"query": ""})]

    def visible_updates(self, hidden):
        sql = query(SQL / "view/updatesView.sq", "getUnreadUpdates").replace(":hiddenCategories", f"({hidden})")
        return [row[0] for row in self.database.execute(sql, {"after": 0, "limit": 100})]

    def test_hidden_membership_wins_over_public_category_in_history_and_widget_updates(self):
        self.assertEqual(set(self.visible_history("2")), {2, 3})
        self.assertEqual(set(self.visible_updates("2")), {2, 3})
        duration = query(SQL / "data/history.sq", "getReadDuration").replace(":hiddenMangaIds", "(1)")
        self.assertEqual(self.database.execute(duration).fetchone()[0], 200)

    def test_hidden_default_category_excludes_uncategorized_titles(self):
        self.assertEqual(set(self.visible_history("0")), {1, 2})
        self.assertEqual(set(self.visible_updates("0")), {1, 2})
        self.assertEqual(set(self.visible_history("0,2")), {2})
        self.assertEqual(set(self.visible_updates("0,2")), {2})

    def test_migration_preserves_history_and_activity_upsert_is_idempotent_for_completions(self):
        self.database.executescript((SQL / "migrations/20.sqm").read_text(encoding="utf-8"))
        self.assertEqual(self.database.execute("SELECT count(*) FROM history").fetchone()[0], 3)
        upsert = query(SQL / "data/readingActivity.sq", "upsert")
        for duration, completed in [(120, False), (60, True), (0, True)]:
            self.database.execute(upsert, {"chapterId": 1, "readDate": "2026-11-05",
                                          "readDuration": duration, "completed": completed})
        self.assertEqual(self.database.execute("SELECT read_duration,completed FROM reading_activity").fetchone(), (180, 1))
        self.database.execute("DELETE FROM chapter WHERE id=1")
        self.assertEqual(self.database.execute("SELECT count(*) FROM reading_activity").fetchone()[0], 0)


if __name__ == "__main__":
    unittest.main()
