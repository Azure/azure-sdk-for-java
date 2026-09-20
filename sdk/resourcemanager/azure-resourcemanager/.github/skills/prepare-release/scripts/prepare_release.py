#!/usr/bin/env python3
"""Prepare a stable azure-resourcemanager aggregate release."""

from __future__ import annotations

import argparse
import json
import re
import subprocess
import sys
from dataclasses import dataclass
from datetime import date, datetime, timezone
from pathlib import Path, PurePosixPath
from typing import Dict, Iterable, List, Optional, Sequence, Tuple
import xml.etree.ElementTree as ET


AGGREGATE_COORDINATE = "com.azure.resourcemanager:azure-resourcemanager"
AGGREGATE_DIRECTORY = Path("sdk/resourcemanager/azure-resourcemanager")
VERSION_FILE = Path("eng/versioning/version_client.txt")
ALLOWED_CHANGED_FILES = (
    "eng/versioning/version_client.txt",
    "sdk/resourcemanager/azure-resourcemanager/CHANGELOG.md",
    "sdk/resourcemanager/azure-resourcemanager/README.md",
    "sdk/resourcemanager/azure-resourcemanager/pom.xml",
    "sdk/resourcemanager/azure-resourcemanager-perf/CHANGELOG.md",
    "sdk/resourcemanager/azure-resourcemanager-perf/README.md",
    "sdk/resourcemanager/azure-resourcemanager-perf/pom.xml",
    "sdk/resourcemanager/azure-resourcemanager-samples/CHANGELOG.md",
    "sdk/resourcemanager/azure-resourcemanager-samples/README.md",
    "sdk/resourcemanager/azure-resourcemanager-samples/pom.xml",
)
PROPAGATION_TARGETS = (
    "sdk/resourcemanager/azure-resourcemanager/pom.xml",
    "sdk/resourcemanager/azure-resourcemanager-samples/pom.xml",
    "sdk/resourcemanager/azure-resourcemanager-perf/pom.xml",
    "sdk/resourcemanager/azure-resourcemanager/README.md",
    "sdk/resourcemanager/azure-resourcemanager-samples/README.md",
    "sdk/resourcemanager/azure-resourcemanager-perf/README.md",
)

RELEASE_HEADING = re.compile(
    r"(?m)^## (?!#)(?P<version>\d+\.\d+\.\d+(?:-[0-9A-Za-z.-]+)?) "
    r"\((?P<date>Unreleased|\d{4}-\d{2}-\d{2})\)[ \t]*$"
)
SECTION_HEADING = re.compile(r"(?m)^### (?!#)(?P<name>[^\r\n]+)[ \t]*$")
STABLE_VERSION = re.compile(r"^\d+\.\d+\.\d+$")
ARTIFACT_ID = re.compile(r"^azure-resourcemanager-[a-z0-9-]+$")


class ReleasePreparationError(RuntimeError):
    """Raised when deterministic release preparation cannot continue."""


@dataclass(frozen=True, order=True)
class Version:
    major: int
    minor: int
    patch: int

    @classmethod
    def parse(cls, value: str) -> "Version":
        base = value.split("-", 1)[0]
        parts = base.split(".")
        if len(parts) != 3 or any(not part.isdigit() for part in parts):
            raise ReleasePreparationError(f"Invalid semantic version: {value}")
        return cls(*(int(part) for part in parts))

    def __str__(self) -> str:
        return f"{self.major}.{self.minor}.{self.patch}"


@dataclass(frozen=True)
class Release:
    version_text: str
    version: Version
    prerelease: bool
    release_date: Optional[date]
    content: str


@dataclass(frozen=True)
class BundledLibrary:
    artifact_id: str
    consumed_version: str
    directory: Path
    changelog_path: Path


@dataclass(frozen=True)
class PackageSelection:
    library: BundledLibrary
    source_version: Optional[str]
    qualifying_releases: Tuple[Release, ...]
    latest_release: Optional[Release]
    features_section: Optional[str]
    breaking_sections: Tuple[str, ...]
    api_updates: Tuple[str, ...]
    api_versions: Tuple[str, ...]
    exclude_breaking_changes: bool

    @property
    def has_content(self) -> bool:
        return bool(
            self.features_section
            or (self.breaking_sections and not self.exclude_breaking_changes)
            or self.api_updates
        )


def normalize_newlines(value: str) -> str:
    return value.replace("\r\n", "\n").replace("\r", "\n")


def read_text(path: Path) -> Tuple[str, str]:
    raw = path.read_bytes()
    newline = "\r\n" if b"\r\n" in raw else "\n"
    return normalize_newlines(raw.decode("utf-8")), newline


def write_text_if_changed(path: Path, normalized_text: str, newline: str) -> bool:
    desired = normalized_text.replace("\n", newline).encode("utf-8")
    if path.read_bytes() == desired:
        return False
    path.write_bytes(desired)
    return True


def parse_releases(changelog: str, source: str = "CHANGELOG.md") -> List[Release]:
    matches = list(RELEASE_HEADING.finditer(changelog))
    if not matches:
        raise ReleasePreparationError(f"No release headings found in {source}")

    releases: List[Release] = []
    for index, match in enumerate(matches):
        end = matches[index + 1].start() if index + 1 < len(matches) else len(changelog)
        version_text = match.group("version")
        date_text = match.group("date")
        releases.append(
            Release(
                version_text=version_text,
                version=Version.parse(version_text),
                prerelease="-" in version_text,
                release_date=None if date_text == "Unreleased" else date.fromisoformat(date_text),
                content=changelog[match.end() : end].strip("\n"),
            )
        )
    return releases


def _xml_child_text(element: ET.Element, child_name: str) -> Optional[str]:
    for child in element:
        if child.tag.rsplit("}", 1)[-1] == child_name:
            return child.text.strip() if child.text else None
    return None


def discover_bundled_libraries(repo_root: Path) -> List[BundledLibrary]:
    pom_path = repo_root / AGGREGATE_DIRECTORY / "pom.xml"
    root = ET.parse(pom_path).getroot()
    dependencies = next(
        (child for child in root if child.tag.rsplit("}", 1)[-1] == "dependencies"),
        None,
    )
    if dependencies is None:
        raise ReleasePreparationError(f"No dependencies found in {pom_path}")

    libraries: List[BundledLibrary] = []
    for dependency in dependencies:
        if dependency.tag.rsplit("}", 1)[-1] != "dependency":
            continue
        group_id = _xml_child_text(dependency, "groupId")
        artifact_id = _xml_child_text(dependency, "artifactId")
        consumed_version = _xml_child_text(dependency, "version")
        scope = _xml_child_text(dependency, "scope") or "compile"
        if group_id != "com.azure.resourcemanager" or scope == "test":
            continue
        if not artifact_id or not ARTIFACT_ID.fullmatch(artifact_id):
            raise ReleasePreparationError(
                f"Unexpected bundled ResourceManager artifact ID: {artifact_id!r}"
            )
        if not consumed_version or consumed_version.startswith("${"):
            raise ReleasePreparationError(
                f"Bundled dependency {artifact_id} must use a concrete version"
            )

        matches = [
            candidate
            for candidate in (repo_root / "sdk").glob(f"*/{artifact_id}")
            if (candidate / "CHANGELOG.md").is_file()
        ]
        if len(matches) != 1:
            raise ReleasePreparationError(
                f"Expected one package directory for {artifact_id}, found {len(matches)}"
            )
        libraries.append(
            BundledLibrary(
                artifact_id=artifact_id,
                consumed_version=consumed_version,
                directory=matches[0],
                changelog_path=matches[0] / "CHANGELOG.md",
            )
        )

    if not libraries:
        raise ReleasePreparationError(f"No bundled premium libraries found in {pom_path}")
    return libraries


def find_release_gate_blockers(
    libraries: Sequence[BundledLibrary],
) -> List[Dict[str, str]]:
    blockers: List[Dict[str, str]] = []
    for library in libraries:
        changelog, _ = read_text(library.changelog_path)
        first_release = parse_releases(changelog, str(library.changelog_path))[0]
        if first_release.release_date is not None:
            blockers.append(
                {
                    "artifact_id": library.artifact_id,
                    "version": first_release.version_text,
                    "date": first_release.release_date.isoformat(),
                    "changelog": library.changelog_path.as_posix(),
                }
            )
    return blockers


def read_aggregate_current_version(version_file: Path) -> str:
    for raw_line in version_file.read_text(encoding="utf-8").splitlines():
        if raw_line.startswith(f"{AGGREGATE_COORDINATE};"):
            parts = raw_line.split(";")
            if len(parts) != 3:
                break
            return parts[2]
    raise ReleasePreparationError(
        f"Could not find {AGGREGATE_COORDINATE} in {version_file}"
    )


def resolve_release_version(explicit: Optional[str], current_version: str) -> str:
    candidate = explicit or current_version.split("-", 1)[0]
    if not STABLE_VERSION.fullmatch(candidate):
        raise ReleasePreparationError(
            f"Stable release version must use X.Y.Z format: {candidate}"
        )
    return candidate


def resolve_release_date(explicit: Optional[str], today: Optional[date] = None) -> date:
    if explicit:
        try:
            return date.fromisoformat(explicit)
        except ValueError as error:
            raise ReleasePreparationError(
                f"Release date must use YYYY-MM-DD format: {explicit}"
            ) from error
    return today or datetime.now(timezone.utc).date()


def resolve_target_release_date(
    explicit: Optional[str],
    today: Optional[date],
    aggregate_releases: Sequence[Release],
    target_version: str,
) -> date:
    requested_date = resolve_release_date(explicit, today)
    first_release = aggregate_releases[0]
    if (
        not first_release.prerelease
        and first_release.version_text == target_version
        and first_release.release_date is not None
    ):
        if explicit and requested_date != first_release.release_date:
            raise ReleasePreparationError(
                f"Existing top release {target_version} is dated "
                f"{first_release.release_date.isoformat()}; a rerun cannot change its date"
            )
        return first_release.release_date
    return requested_date


def find_prior_aggregate_release(
    aggregate_releases: Sequence[Release], target_version: str
) -> Release:
    stable_releases = [
        release
        for release in aggregate_releases
        if not release.prerelease and release.release_date is not None
    ]
    if not stable_releases:
        raise ReleasePreparationError("No prior aggregate stable release with a date found")

    matching_indexes = [
        index
        for index, release in enumerate(aggregate_releases)
        if not release.prerelease and release.version_text == target_version
    ]
    if matching_indexes and matching_indexes != [0]:
        raise ReleasePreparationError(
            f"Release version {target_version} already exists in aggregate history"
        )

    if matching_indexes == [0]:
        for release in aggregate_releases[1:]:
            if not release.prerelease and release.release_date is not None:
                return release
        raise ReleasePreparationError(
            "No prior aggregate stable release before the current target found"
        )

    latest_stable = stable_releases[0]
    if Version.parse(target_version) <= latest_stable.version:
        raise ReleasePreparationError(
            f"Release version {target_version} must be newer than "
            f"{latest_stable.version_text}"
        )
    return latest_stable


def validate_release_date_after_cutoff(
    target_date: date, prior_release: Release
) -> None:
    if target_date <= prior_release.release_date:
        raise ReleasePreparationError(
            f"Release date {target_date.isoformat()} must be after aggregate cutoff "
            f"{prior_release.release_date.isoformat()}"
        )


def extract_section(content: str, section_name: str) -> Optional[str]:
    matches = list(SECTION_HEADING.finditer(content))
    for index, match in enumerate(matches):
        if match.group("name").strip() != section_name:
            continue
        end = matches[index + 1].start() if index + 1 < len(matches) else len(content)
        section = content[match.start() : end].strip("\n")
        body = section.split("\n", 1)[1].strip() if "\n" in section else ""
        return section if body else None
    return None


def _bullet_blocks(content: str) -> Iterable[str]:
    lines = content.splitlines()
    index = 0
    while index < len(lines):
        bullet = re.match(r"^(?P<indent>[ \t]*)[-*] ", lines[index])
        if not bullet:
            index += 1
            continue
        indent = len(bullet.group("indent"))
        end = index + 1
        while end < len(lines):
            next_bullet = re.match(r"^(?P<indent>[ \t]*)[-*] ", lines[end])
            if next_bullet and len(next_bullet.group("indent")) <= indent:
                break
            if re.match(r"^#{1,6} ", lines[end]):
                break
            end += 1
        yield "\n".join(lines[index:end]).rstrip()
        index = end


def extract_api_updates(content: str) -> Tuple[str, ...]:
    return tuple(
        block for block in _bullet_blocks(content) if "api-version" in block.lower()
    )


def extract_api_versions(api_updates: Sequence[str]) -> Tuple[str, ...]:
    values: List[str] = []
    for update in api_updates:
        for value in re.findall(r"\b20\d{2}-\d{2}-\d{2}(?:-preview)?\b", update):
            if value not in values:
                values.append(value)
    return tuple(values)


def _section_body(section: str) -> str:
    return section.split("\n", 1)[1].strip("\n") if "\n" in section else ""


def shift_headings(markdown: str) -> str:
    return re.sub(
        r"(?m)^(#{3,5})(?= )",
        lambda match: match.group(1) + "#",
        markdown,
    )


def select_package_changelog(
    library: BundledLibrary,
    changelog: str,
    cutoff: date,
    release_date: date,
    exclude_breaking_changes: bool = False,
) -> PackageSelection:
    releases = parse_releases(changelog, str(library.changelog_path))
    consumed = Version.parse(library.consumed_version)
    stable_minor_releases = [
        release
        for release in releases
        if not release.prerelease
        and release.release_date is not None
        and release.version.patch == 0
        and release.version <= consumed
    ]
    source_release = max(stable_minor_releases, key=lambda item: item.version, default=None)
    if source_release is None:
        return PackageSelection(
            library, None, (), None, None, (), (), (), exclude_breaking_changes
        )

    qualifying = tuple(
        sorted(
            (
                release
                for release in stable_minor_releases
                if cutoff < release.release_date <= release_date
                and release.version <= source_release.version
            ),
            key=lambda item: item.version,
        )
    )
    latest = qualifying[-1] if qualifying else None
    features = extract_section(latest.content, "Features Added") if latest else None
    breaking = tuple(
        section
        for release in qualifying
        if (section := extract_section(release.content, "Breaking Changes"))
    )
    api_updates = extract_api_updates(latest.content) if latest else ()
    return PackageSelection(
        library=library,
        source_version=source_release.version_text,
        qualifying_releases=qualifying,
        latest_release=latest,
        features_section=features,
        breaking_sections=breaking,
        api_updates=api_updates,
        api_versions=extract_api_versions(api_updates),
        exclude_breaking_changes=exclude_breaking_changes,
    )


def render_package_selection(selection: PackageSelection) -> Optional[str]:
    if not selection.has_content:
        return None

    blocks = [f"### {selection.library.artifact_id}"]
    if selection.features_section:
        blocks.append(shift_headings(selection.features_section))

    if selection.breaking_sections and not selection.exclude_breaking_changes:
        if len(selection.qualifying_releases) == 1:
            blocks.append(shift_headings(selection.breaking_sections[0]))
        else:
            breaking_bodies = [
                shift_headings(_section_body(section))
                for section in selection.breaking_sections
            ]
            blocks.append("#### Breaking Changes\n\n" + "\n\n".join(breaking_bodies))

    if selection.api_updates:
        blocks.append(
            "#### Dependency Updates\n\n" + "\n\n".join(selection.api_updates)
        )
    return "\n\n".join(blocks)


def render_aggregate_release(
    release_version: str,
    release_date: date,
    selections: Sequence[PackageSelection],
) -> str:
    package_sections = [
        rendered
        for selection in selections
        if (rendered := render_package_selection(selection))
    ]
    blocks = [f"## {release_version} ({release_date.isoformat()})"]
    blocks.extend(package_sections)
    blocks.append("### Other Changes\n\n- Updated dependencies from resources.")
    return "\n\n".join(blocks) + "\n"


def replace_first_release(changelog: str, release_section: str) -> str:
    matches = list(RELEASE_HEADING.finditer(changelog))
    if not matches:
        raise ReleasePreparationError("Aggregate CHANGELOG has no release heading")
    suffix_start = matches[1].start() if len(matches) > 1 else len(changelog)
    prefix = changelog[: matches[0].start()]
    suffix = changelog[suffix_start:].lstrip("\n")
    return prefix + release_section.rstrip("\n") + "\n\n" + suffix


def update_canonical_version(version_file: Path, release_version: str) -> bool:
    text, newline = read_text(version_file)
    lines = text.splitlines(keepends=True)
    updated = False
    found = False
    for index, line in enumerate(lines):
        if not line.startswith(f"{AGGREGATE_COORDINATE};"):
            continue
        found = True
        ending = "\n" if line.endswith("\n") else ""
        parts = line.rstrip("\n").split(";")
        if len(parts) != 3:
            raise ReleasePreparationError(f"Malformed aggregate version line: {line}")
        replacement = f"{parts[0]};{parts[1]};{release_version}{ending}"
        if replacement != line:
            lines[index] = replacement
            updated = True
        break
    if not found:
        raise ReleasePreparationError(
            f"Could not find {AGGREGATE_COORDINATE} in {version_file}"
        )
    if updated:
        write_text_if_changed(version_file, "".join(lines), newline)
    return updated


def run_version_propagation(repo_root: Path) -> None:
    updater = repo_root / "eng/versioning/update_versions.py"
    for relative in PROPAGATION_TARGETS:
        target = repo_root / relative
        if not target.is_file():
            raise ReleasePreparationError(f"Missing propagation target: {relative}")
        command = [
            sys.executable,
            str(updater),
            "--target-file",
            str(target),
            "--library-list",
            AGGREGATE_COORDINATE,
        ]
        if target.name.startswith("pom") and target.suffix == ".xml":
            command.append("--skip-readme")
        subprocess.run(command, cwd=repo_root, check=True)


def get_changed_files(repo_root: Path) -> List[str]:
    commands = (
        ["git", "diff", "--name-only", "--relative", "--"],
        ["git", "diff", "--cached", "--name-only", "--relative", "--"],
        ["git", "ls-files", "--others", "--exclude-standard"],
    )
    changed = set()
    for command in commands:
        result = subprocess.run(
            command,
            cwd=repo_root,
            check=True,
            text=True,
            stdout=subprocess.PIPE,
        )
        changed.update(line.strip() for line in result.stdout.splitlines() if line.strip())
    return sorted(PurePosixPath(path).as_posix() for path in changed)


def validate_changed_files(
    changed_files: Sequence[str],
    allowed_files: Sequence[str] = ALLOWED_CHANGED_FILES,
) -> Tuple[bool, List[str]]:
    allowed = {PurePosixPath(path).as_posix() for path in allowed_files}
    normalized = [PurePosixPath(path).as_posix() for path in changed_files]
    unexpected = sorted(path for path in normalized if path not in allowed)
    return not unexpected, unexpected


def parse_breaking_exclusions(
    value: Optional[str], libraries: Sequence[BundledLibrary]
) -> List[str]:
    exclusions = sorted(
        {item.strip() for item in (value or "").split(",") if item.strip()}
    )
    bundled = {library.artifact_id for library in libraries}
    unknown = sorted(set(exclusions) - bundled)
    if unknown:
        raise ReleasePreparationError(
            "Breaking-change exclusions are not bundled artifact IDs: "
            + ", ".join(unknown)
        )
    return exclusions


def _selection_summary(selection: PackageSelection) -> Dict[str, object]:
    return {
        "artifact_id": selection.library.artifact_id,
        "consumed_version": selection.library.consumed_version,
        "source_version": selection.source_version,
        "selected_versions": [
            release.version_text for release in selection.qualifying_releases
        ],
        "api_versions": list(selection.api_versions),
        "api_updates": list(selection.api_updates),
        "breaking_changes_excluded": selection.exclude_breaking_changes,
        "included": selection.has_content,
    }


def prepare_release(
    repo_root: Path,
    release_version_arg: Optional[str] = None,
    release_date_arg: Optional[str] = None,
    exclude_breaking_changes_arg: Optional[str] = None,
    dry_run: bool = False,
    today: Optional[date] = None,
) -> Tuple[Dict[str, object], int]:
    repo_root = repo_root.resolve()
    version_file = repo_root / VERSION_FILE
    aggregate_changelog_path = repo_root / AGGREGATE_DIRECTORY / "CHANGELOG.md"
    current_version = read_aggregate_current_version(version_file)
    release_version = resolve_release_version(release_version_arg, current_version)
    aggregate_changelog, aggregate_newline = read_text(aggregate_changelog_path)
    aggregate_releases = parse_releases(
        aggregate_changelog, str(aggregate_changelog_path)
    )
    target_date = resolve_target_release_date(
        release_date_arg, today, aggregate_releases, release_version
    )
    prior_release = find_prior_aggregate_release(aggregate_releases, release_version)
    validate_release_date_after_cutoff(target_date, prior_release)
    libraries = discover_bundled_libraries(repo_root)
    exclusions = parse_breaking_exclusions(
        exclude_breaking_changes_arg, libraries
    )

    summary: Dict[str, object] = {
        "status": "blocked",
        "dry_run": dry_run,
        "release_version": release_version,
        "release_date": target_date.isoformat(),
        "prior_aggregate_release": {
            "version": prior_release.version_text,
            "cutoff_date": prior_release.release_date.isoformat(),
        },
        "cutoff_date": prior_release.release_date.isoformat(),
        "gate": {"passed": False, "blockers": []},
        "selected_packages": [],
        "overrides": {"exclude_breaking_changes": exclusions},
        "allowlist": {
            "passed": True,
            "allowed_files": list(ALLOWED_CHANGED_FILES),
            "unexpected_files": [],
        },
        "changed_files": [],
        "planned_files": [],
    }

    blockers = find_release_gate_blockers(libraries)
    summary["gate"] = {"passed": not blockers, "blockers": blockers}
    if blockers:
        return summary, 2

    selections: List[PackageSelection] = []
    for library in libraries:
        changelog, _ = read_text(library.changelog_path)
        selections.append(
            select_package_changelog(
                library=library,
                changelog=changelog,
                cutoff=prior_release.release_date,
                release_date=target_date,
                exclude_breaking_changes=library.artifact_id in exclusions,
            )
        )
    summary["selected_packages"] = [
        _selection_summary(selection)
        for selection in selections
        if selection.qualifying_releases
    ]

    release_section = render_aggregate_release(
        release_version, target_date, selections
    )
    updated_changelog = replace_first_release(aggregate_changelog, release_section)
    planned_files = []
    if updated_changelog != aggregate_changelog:
        planned_files.append(AGGREGATE_DIRECTORY.joinpath("CHANGELOG.md").as_posix())
    if current_version != release_version:
        planned_files.append(VERSION_FILE.as_posix())
        planned_files.extend(PROPAGATION_TARGETS)
    summary["planned_files"] = sorted(set(planned_files))

    if dry_run:
        summary["status"] = "ready" if planned_files else "no_changes"
        return summary, 0

    preexisting_changes = get_changed_files(repo_root)
    if preexisting_changes:
        allowlist_passed, unexpected = validate_changed_files(preexisting_changes)
        summary["status"] = "blocked"
        summary["changed_files"] = preexisting_changes
        summary["allowlist"] = {
            "passed": allowlist_passed,
            "allowed_files": list(ALLOWED_CHANGED_FILES),
            "unexpected_files": unexpected,
        }
        summary["preexisting_changes"] = preexisting_changes
        return summary, 2

    update_canonical_version(version_file, release_version)
    write_text_if_changed(
        aggregate_changelog_path, updated_changelog, aggregate_newline
    )
    run_version_propagation(repo_root)

    changed_files = get_changed_files(repo_root)
    allowlist_passed, unexpected = validate_changed_files(changed_files)
    summary["changed_files"] = changed_files
    summary["allowlist"] = {
        "passed": allowlist_passed,
        "allowed_files": list(ALLOWED_CHANGED_FILES),
        "unexpected_files": unexpected,
    }
    if not allowlist_passed:
        summary["status"] = "error"
        return summary, 3
    summary["status"] = "ready" if changed_files else "no_changes"
    return summary, 0


def find_repo_root(start: Path) -> Path:
    result = subprocess.run(
        ["git", "rev-parse", "--show-toplevel"],
        cwd=start,
        check=True,
        text=True,
        stdout=subprocess.PIPE,
    )
    return Path(result.stdout.strip())


def write_summary(summary: Dict[str, object], summary_file: Optional[Path]) -> None:
    serialized = json.dumps(summary, indent=2, sort_keys=True) + "\n"
    sys.stdout.write(serialized)
    if summary_file:
        summary_file.parent.mkdir(parents=True, exist_ok=True)
        summary_file.write_text(serialized, encoding="utf-8")


def main(argv: Optional[Sequence[str]] = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--release-version")
    parser.add_argument("--release-date")
    parser.add_argument("--exclude-breaking-changes")
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--summary-file", type=Path)
    parser.add_argument("--repo-root", type=Path)
    args = parser.parse_args(argv)

    try:
        repo_root = args.repo_root or find_repo_root(Path.cwd())
        summary, exit_code = prepare_release(
            repo_root=repo_root,
            release_version_arg=args.release_version,
            release_date_arg=args.release_date,
            exclude_breaking_changes_arg=args.exclude_breaking_changes,
            dry_run=args.dry_run,
        )
    except (OSError, ET.ParseError, ReleasePreparationError, subprocess.SubprocessError) as error:
        summary = {
            "status": "error",
            "dry_run": args.dry_run,
            "gate": {"passed": False, "blockers": []},
            "error": str(error),
            "changed_files": [],
            "allowlist": {
                "passed": False,
                "allowed_files": list(ALLOWED_CHANGED_FILES),
                "unexpected_files": [],
            },
        }
        exit_code = 1
    write_summary(summary, args.summary_file)
    return exit_code


if __name__ == "__main__":
    raise SystemExit(main())
