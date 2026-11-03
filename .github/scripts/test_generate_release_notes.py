import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from generate_release_notes import changelog_entry, generate, git, previous_release


class ReleaseNotesTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        git(self.root, "init", "--initial-branch=main")
        git(self.root, "config", "user.name", "Release test")
        git(self.root, "config", "user.email", "release-test@example.com")
        self.commit("Initial upstream history")
        git(self.root, "tag", "v1.0.5")

    def commit(self, subject):
        with (self.root / "changes.txt").open("a", encoding="utf-8") as stream:
            stream.write(subject + "\n")
        git(self.root, "add", ".")
        git(self.root, "commit", "--quiet", "-m", subject)

    def test_selects_previous_published_release_and_includes_unreleased_tag_changes(self):
        self.commit("fix: first unpublished fix")
        git(self.root, "tag", "v1.0.6")
        self.commit("feat: new collection")
        git(self.root, "tag", "v1.0.7")
        previous = previous_release(
            self.root, "v1.0.7", ["v1.0.5", "v1.0.7", "v9.0.0", "v1.0.7-rc.1"]
        )
        self.assertEqual(previous, "v1.0.5")
        body = generate(self.root, "v1.0.7", "example/comics", previous)
        self.assertIn("first unpublished fix", body)
        self.assertIn("new collection", body)
        self.assertIn("/compare/v1.0.5...v1.0.7", body)

    def test_ignores_newer_release_on_an_unrelated_branch(self):
        git(self.root, "checkout", "--quiet", "-b", "other-release")
        self.commit("fix: unrelated fix")
        git(self.root, "tag", "v1.0.6")
        git(self.root, "checkout", "--quiet", "main")
        self.commit("feat: main branch update")
        git(self.root, "tag", "v1.0.7")
        self.assertEqual(
            previous_release(self.root, "v1.0.7", ["v1.0.5", "v1.0.6"]), "v1.0.5"
        )

    def test_matching_bilingual_changelog_is_the_in_app_summary(self):
        (self.root / "CHANGELOG.md").write_text(
            "# Changelog\n\n## 1.0.7 — Unreleased\n\nFuture changes\n\n"
            "## [1.0.6] — 2026-11-03\n\n### Library\n\n- Better continue reading.\n\n"
            "## 1.0.5\n\nOld changes\n", encoding="utf-8"
        )
        (self.root / "CHANGELOG.id.md").write_text(
            "## 1.0.6 — 2026-11-03\n\n- Perbaikan pembaruan.\n", encoding="utf-8"
        )
        self.commit("fix(updater): request fresh release information")
        git(self.root, "tag", "v1.0.6")
        body = generate(self.root, "v1.0.6", "example/comics", "v1.0.5")
        summary, details = body.split("<!-->")
        self.assertIn("Better continue reading.", summary)
        self.assertIn("Perbaikan pembaruan.", summary)
        self.assertNotIn("Future changes", body)
        self.assertNotIn("Old changes", body)
        self.assertNotIn("## Downloads", summary)
        self.assertIn("request fresh release information", details)
        self.assertIn("Commit details", details)
        self.assertIn("komikita-arm64-v8a-v1.0.6.apk", details)
        self.assertNotIn("komikita-v1.0.6-foss.apk", details)
        self.assertEqual(details.count("/releases/download/v1.0.6/"), 5)
        self.assertIn("/blob/v1.0.6/docs/README.md", details)

    def test_fallback_groups_direct_commits_without_reusing_old_changelog(self):
        (self.root / "CHANGELOG.md").write_text(
            "## 1.0.5\n\nOld summary must not be reused.\n", encoding="utf-8"
        )
        self.commit("feat(library): add a collection")
        self.commit("fix(updater): refresh cached release data")
        self.commit("perf: reduce allocations")
        self.commit("chore(version): bump to 1.0.6")
        git(self.root, "tag", "v1.0.6")
        body = generate(self.root, "v1.0.6", "example/comics", "v1.0.5")
        self.assertIn("### Features", body)
        self.assertIn("### Fixes", body)
        self.assertIn("### Performance", body)
        self.assertIn("library: add a collection", body)
        self.assertNotIn("Old summary", body)
        self.assertNotIn("bump to 1.0.6", body)
        self.assertEqual(body.count("/commit/"), 3)

    def test_first_release_does_not_dump_inherited_history(self):
        self.commit("Old upstream feature that should not appear")
        git(self.root, "tag", "v1.0.6")
        previous = previous_release(self.root, "v1.0.6", [])
        self.assertIsNone(previous)
        body = generate(self.root, "v1.0.6", "example/comics", previous)
        self.assertIn("Initial KomiKita release.", body)
        self.assertNotIn("Old upstream feature", body)
        self.assertNotIn("/compare/", body)

    def test_tags_on_same_commit_do_not_invent_changes(self):
        git(self.root, "tag", "v1.0.6")
        previous = previous_release(self.root, "v1.0.6", ["v1.0.5", "v1.0.6"])
        self.assertEqual(previous, "v1.0.5")
        body = generate(self.root, "v1.0.6", "example/comics", previous)
        self.assertIn("No additional commit summaries", body)
        self.assertNotIn("Initial upstream history", body)

    def test_cli_uses_published_tag_file_and_writes_a_release_body(self):
        self.commit("fix: handle [labels] and <markup>")
        git(self.root, "tag", "v1.0.6")
        published = self.root / "published-tags.txt"
        published.write_text("v1.0.5\nv1.0.6\n", encoding="utf-8")
        output = self.root / "output" / "release-notes.md"
        subprocess.run(
            [sys.executable, str(Path(__file__).with_name("generate_release_notes.py")),
             "--tag", "v1.0.6", "--repository", "example/comics",
             "--published-tags", str(published), "--output", str(output)],
            cwd=self.root, capture_output=True, check=True,
        )
        body = output.read_text(encoding="utf-8")
        self.assertIn(r"handle \[labels\] and &lt;markup&gt;", body)
        self.assertIn("/compare/v1.0.5...v1.0.6", body)
        self.assertEqual(changelog_entry(self.root / "missing.md", "v1.0.6"), "")


if __name__ == "__main__":
    unittest.main()
