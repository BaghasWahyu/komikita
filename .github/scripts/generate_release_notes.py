"""Build release notes from the matching changelog entry and release commit range."""

import argparse
import html
import re
import subprocess
from pathlib import Path
from urllib.parse import quote


CATEGORIES = {
    "feat": "Features",
    "fix": "Fixes",
    "perf": "Performance",
    "docs": "Documentation",
    "maintenance": "Build and maintenance",
    "other": "Other changes",
}


def git(root, *arguments):
    return subprocess.check_output(
        ["git", "-C", str(root), *arguments], encoding="utf-8"
    ).strip()


def version_key(tag):
    match = re.fullmatch(r"v?(\d+)\.(\d+)\.(\d+)(?:\.(\d+))?", tag)
    return tuple(map(int, match.groups(default="0"))) if match else None


def previous_release(root, tag, published_tags):
    current = version_key(tag)
    candidates = [
        candidate for candidate in published_tags
        if version_key(candidate) is not None and version_key(candidate) < current
    ]
    for candidate in sorted(candidates, key=version_key, reverse=True):
        result = subprocess.run(
            ["git", "-C", str(root), "merge-base", "--is-ancestor",
             f"refs/tags/{candidate}", f"refs/tags/{tag}"],
            capture_output=True,
        )
        if result.returncode == 0:
            return candidate
    return None


def changelog_entry(path, tag):
    if not path.exists():
        return ""
    version = re.escape(tag.removeprefix("v"))
    heading = re.compile(rf"^##\s+\[?v?{version}\]?(?:\s|$)")
    lines = path.read_text(encoding="utf-8").splitlines()
    for index, line in enumerate(lines):
        if heading.match(line):
            end = next(
                (offset for offset in range(index + 1, len(lines))
                 if lines[offset].startswith("## ")),
                len(lines),
            )
            return "\n".join(lines[index + 1:end]).strip()
    return ""


def commit_notes(root, previous, tag, repository_url):
    # An initial fork release must not list the entire upstream history.
    if previous is None:
        return ""
    commits = git(
        root, "log", "--no-merges", "--reverse", "--format=%H%x00%s",
        f"refs/tags/{previous}..refs/tags/{tag}", "--",
    )
    groups = {key: [] for key in CATEGORIES}
    for line in commits.splitlines():
        sha, subject = line.split("\0", 1)
        if re.match(r"^(?:chore\(version\):|Release v\d)", subject, re.IGNORECASE):
            continue
        conventional = re.match(r"^(\w+)(?:\(([^)]+)\))?(!)?:\s*(.+)$", subject)
        category = "other"
        if conventional:
            kind, scope, breaking, description = conventional.groups()
            category = kind if kind in CATEGORIES else "maintenance"
            subject = f"{scope}: {description}" if scope else description
            if breaking:
                subject = f"Breaking change: {subject}"
        subject = html.escape(subject).replace("[", r"\[").replace("]", r"\]")
        groups[category].append(f"- {subject} ([{sha[:7]}]({repository_url}/commit/{sha}))")
    return "\n\n".join(
        f"### {CATEGORIES[category]}\n\n" + "\n".join(items)
        for category, items in groups.items() if items
    )


def generate(root, tag, repository, previous):
    repository_url = f"https://github.com/{repository}"
    encoded_tag = quote(tag, safe="")
    summary = changelog_entry(root / "CHANGELOG.md", tag)
    indonesian = changelog_entry(root / "CHANGELOG.id.md", tag)
    commits = commit_notes(root, previous, tag, repository_url)
    changes = summary or commits or (
        "Initial KomiKita release." if previous is None
        else f"No additional commit summaries since {previous}. See the full changelog below."
    )
    parts = [f"## What's new in {tag}\n\n{changes}"]
    if indonesian:
        parts.append(f"<details>\n<summary>Bahasa Indonesia</summary>\n\n{indonesian}\n\n</details>")
    if previous:
        comparison = f"{quote(previous, safe='')}...{encoded_tag}"
        parts.append(f"**Full changelog:** [{previous} → {tag}]({repository_url}/compare/{comparison})")

    # The Android updater shows everything before this existing separator.
    parts.append("<!-->")
    if summary and commits:
        parts.append(f"<details>\n<summary>Commit details</summary>\n\n{commits}\n\n</details>")

    download_url = f"{repository_url}/releases/download/{encoded_tag}"
    packages = [
        (f"komikita-{tag}.apk", "Universal APK — recommended if you are unsure"),
        (f"komikita-arm64-v8a-{tag}.apk", "ARM64 phones and tablets"),
        (f"komikita-armeabi-v7a-{tag}.apk", "32-bit ARM devices"),
        (f"komikita-x86-{tag}.apk", "32-bit x86 devices and emulators"),
        (f"komikita-x86_64-{tag}.apk", "64-bit x86 devices and emulators"),
    ]
    downloads = ["## Downloads", "", "| APK | Use this for |", "| --- | --- |"]
    downloads.extend(
        f"| [{name}]({download_url}/{quote(name, safe='')}) | {description} |"
        for name, description in packages
    )
    parts.append("\n".join(downloads))
    parts.append(
        "## Updating\n\n"
        "Install the APK over your existing app to keep your library and settings. "
        "These APKs update the standard app (app.komikita); FOSS builds are no longer "
        "included in GitHub releases. Do not uninstall the app first.\n\n"
        "APK ini memperbarui aplikasi standar (app.komikita); build FOSS tidak lagi "
        "disertakan dalam rilis GitHub. Tidak perlu mencopot aplikasi lama."
    )
    parts.append(
        f"**Documentation:** [English]({repository_url}/blob/{encoded_tag}/docs/README.md) · "
        f"[Bahasa Indonesia]({repository_url}/blob/{encoded_tag}/docs/id/README.md)\n\n"
        f"**Bug reports:** [KomiKita Issues]({repository_url}/issues)"
    )
    return "\n\n".join(parts) + "\n"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--tag", required=True)
    parser.add_argument("--repository", default="BaghasWahyu/komikita")
    parser.add_argument("--output", required=True, type=Path)
    base = parser.add_mutually_exclusive_group()
    base.add_argument("--published-tags", type=Path)
    base.add_argument("--previous-tag")
    arguments = parser.parse_args()
    if version_key(arguments.tag) is None:
        parser.error("--tag must be a stable version such as v1.0.13 or v1.0.13.2")
    root = Path(git(Path.cwd(), "rev-parse", "--show-toplevel"))
    git(root, "rev-parse", "--verify", f"refs/tags/{arguments.tag}^{{commit}}")
    tags = (
        arguments.published_tags.read_text(encoding="utf-8").splitlines()
        if arguments.published_tags else git(root, "tag", "--list").splitlines()
    )
    previous = arguments.previous_tag or previous_release(root, arguments.tag, tags)
    if arguments.previous_tag and previous_release(root, arguments.tag, [previous]) != previous:
        parser.error("--previous-tag must be an older stable tag reachable from --tag")
    arguments.output.parent.mkdir(parents=True, exist_ok=True)
    arguments.output.write_text(
        generate(root, arguments.tag, arguments.repository, previous), encoding="utf-8"
    )
    print(f"Release notes: {previous or 'initial release'} -> {arguments.tag}: {arguments.output}")


if __name__ == "__main__":
    main()
