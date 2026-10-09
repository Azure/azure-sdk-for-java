import importlib.util
import io
import sys
import tempfile
import unittest
from contextlib import redirect_stderr
from datetime import date, datetime, timezone
from pathlib import Path
from unittest.mock import patch


TESTS_DIR = Path(__file__).resolve().parent
FIXTURES = TESTS_DIR / "fixtures"
SCRIPT = TESTS_DIR.parent / "scripts" / "prepare_release.py"
SPEC = importlib.util.spec_from_file_location("prepare_release", SCRIPT)
prepare_release = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
sys.modules[SPEC.name] = prepare_release
SPEC.loader.exec_module(prepare_release)


class PrepareReleaseTests(unittest.TestCase):
    def setUp(self):
        self.temp_directory = tempfile.TemporaryDirectory()
        self.repo_root = Path(self.temp_directory.name)

    def tearDown(self):
        self.temp_directory.cleanup()

    def fixture(self, name):
        return (FIXTURES / name).read_text(encoding="utf-8")

    def write(self, relative, content):
        path = self.repo_root / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8")
        return path

    def library(self, artifact_id, version, fixture_name):
        directory = self.repo_root / "sdk" / artifact_id.removeprefix(
            "azure-resourcemanager-"
        ) / artifact_id
        changelog = self.write(
            directory.relative_to(self.repo_root) / "CHANGELOG.md",
            self.fixture(fixture_name),
        )
        return prepare_release.BundledLibrary(
            artifact_id, version, directory, changelog
        )

    def write_release_fixture_repo(self, aggregate_changelog=None):
        self.write(
            "sdk/resourcemanager/azure-resourcemanager/pom.xml",
            self.fixture("premium-pom.xml"),
        )
        self.write(
            "sdk/resourcemanager/azure-resourcemanager/CHANGELOG.md",
            aggregate_changelog or self.fixture("aggregate-changelog.md"),
        )
        self.write(
            "eng/versioning/version_client.txt",
            self.fixture("version-client.txt"),
        )
        self.library(
            "azure-resourcemanager-alpha", "3.2.1", "package-patch-fallback.md"
        )
        self.library(
            "azure-resourcemanager-beta", "4.1.0", "package-multiple-minors.md"
        )

    def test_discovers_bundled_premium_libraries_from_pom(self):
        self.write(
            "sdk/resourcemanager/azure-resourcemanager/pom.xml",
            self.fixture("premium-pom.xml"),
        )
        self.library(
            "azure-resourcemanager-alpha", "3.2.1", "package-patch-fallback.md"
        )
        self.library(
            "azure-resourcemanager-beta", "4.1.0", "package-multiple-minors.md"
        )

        libraries = prepare_release.discover_bundled_libraries(self.repo_root)

        self.assertEqual(
            ["azure-resourcemanager-alpha", "azure-resourcemanager-beta"],
            [library.artifact_id for library in libraries],
        )

    def test_dated_first_section_gate_reports_all_blockers(self):
        alpha = self.library(
            "azure-resourcemanager-alpha", "7.0.0", "package-dated-head.md"
        )
        beta = self.library(
            "azure-resourcemanager-beta", "7.0.0", "package-dated-head.md"
        )

        blockers = prepare_release.find_release_gate_blockers([alpha, beta])

        self.assertEqual(
            ["azure-resourcemanager-alpha", "azure-resourcemanager-beta"],
            [blocker["artifact_id"] for blocker in blockers],
        )
        self.assertTrue(all(blocker["date"] == "2026-08-01" for blocker in blockers))

    def test_version_defaults_and_explicit_values(self):
        version_file = self.write(
            "eng/versioning/version_client.txt",
            self.fixture("version-client.txt"),
        )
        current = prepare_release.read_aggregate_current_version(version_file)

        self.assertEqual("5.1.0", prepare_release.resolve_release_version(None, current))
        self.assertEqual(
            "5.2.0", prepare_release.resolve_release_version("5.2.0", current)
        )

    @patch.object(prepare_release, "datetime")
    def test_release_date_is_current_utc_date(self, clock):
        clock.now.return_value = datetime(2026, 9, 20, 23, 59, tzinfo=timezone.utc)

        self.assertEqual(date(2026, 9, 20), prepare_release.resolve_release_date())
        clock.now.assert_called_once_with(timezone.utc)

    def test_removed_cli_options_are_rejected(self):
        for option, value in (
            ("--release-date", "2026-10-01"),
            ("--exclude-breaking-changes", "azure-resourcemanager-beta"),
        ):
            with self.subTest(option=option):
                stderr = io.StringIO()
                with redirect_stderr(stderr), self.assertRaises(SystemExit) as error:
                    prepare_release.main([option, value])
                self.assertEqual(2, error.exception.code)
                self.assertIn(f"unrecognized arguments: {option}", stderr.getvalue())

    def test_rejects_duplicate_and_downgrade_release_versions(self):
        releases = prepare_release.parse_releases(
            self.fixture("aggregate-changelog.md")
        )

        with self.assertRaisesRegex(
            prepare_release.ReleasePreparationError, "already exists"
        ):
            prepare_release.find_prior_aggregate_release(releases, "5.0.0")
        with self.assertRaisesRegex(
            prepare_release.ReleasePreparationError, "must be newer"
        ):
            prepare_release.find_prior_aggregate_release(releases, "4.9.0")

    def test_release_date_must_be_after_cutoff(self):
        releases = prepare_release.parse_releases(
            self.fixture("aggregate-changelog.md")
        )
        prior = prepare_release.find_prior_aggregate_release(releases, "5.1.0")

        for invalid_date in (date(2026, 5, 10), date(2026, 5, 9)):
            with self.subTest(invalid_date=invalid_date):
                with self.assertRaisesRegex(
                    prepare_release.ReleasePreparationError, "must be after"
                ):
                    prepare_release.validate_release_date_after_cutoff(
                        invalid_date, prior
                    )

    @patch.object(prepare_release, "datetime")
    def test_current_top_rerun_uses_current_utc_date(self, clock):
        changelog = self.fixture("aggregate-changelog.md").replace(
            "## 5.1.0-beta.1 (Unreleased)",
            "## 5.1.0 (2026-07-01)",
        )
        self.write_release_fixture_repo(changelog)
        self.write(
            "eng/versioning/version_client.txt",
            self.fixture("version-client.txt").replace("5.1.0-beta.2", "5.1.0"),
        )
        clock.now.return_value = datetime(2026, 9, 20, tzinfo=timezone.utc)

        summary, exit_code = prepare_release.prepare_release(
            self.repo_root, dry_run=True
        )

        self.assertEqual(0, exit_code)
        self.assertEqual("ready", summary["status"])
        self.assertEqual("2026-09-20", summary["release_date"])
        self.assertEqual("2026-05-10", summary["cutoff_date"])
        self.assertEqual(
            ["sdk/resourcemanager/azure-resourcemanager/CHANGELOG.md"],
            summary["planned_files"],
        )
        self.assertEqual(
            ["3.2.0", "3.3.0"],
            summary["selected_packages"][1]["selected_versions"],
        )
        clock.now.assert_called_once_with(timezone.utc)

    def test_patch_dependency_falls_back_to_closest_minor_release(self):
        library = self.library(
            "azure-resourcemanager-alpha", "3.2.1", "package-patch-fallback.md"
        )

        selection = prepare_release.select_package_changelog(
            library,
            self.fixture("package-patch-fallback.md"),
            cutoff=date(2026, 5, 10),
            release_date=date(2026, 7, 1),
        )

        self.assertEqual("3.2.0", selection.source_version)
        self.assertEqual(
            ["3.2.0"],
            [release.version_text for release in selection.qualifying_releases],
        )
        rendered = prepare_release.render_package_selection(selection)
        self.assertNotIn("Patch-only prose", rendered)

    def test_single_minor_preserves_feature_breaking_and_api_update(self):
        library = self.library(
            "azure-resourcemanager-alpha", "3.2.1", "package-patch-fallback.md"
        )
        selection = prepare_release.select_package_changelog(
            library,
            self.fixture("package-patch-fallback.md"),
            cutoff=date(2026, 5, 10),
            release_date=date(2026, 7, 1),
        )

        rendered = prepare_release.render_package_selection(selection)

        self.assertIn("- Feature wording must remain exactly intact.", rendered)
        self.assertIn("- Breaking wording must remain exactly intact.", rendered)
        self.assertIn("- Updated `api-version` to `2026-04-01`.", rendered)
        self.assertEqual(("2026-04-01",), selection.api_versions)

    def test_multiple_minors_keep_all_features_breaking_and_latest_api(self):
        library = self.library(
            "azure-resourcemanager-beta", "3.3.0", "package-multiple-minors.md"
        )
        selection = prepare_release.select_package_changelog(
            library,
            self.fixture("package-multiple-minors.md"),
            cutoff=date(2026, 5, 10),
            release_date=date(2026, 7, 20),
        )

        rendered = prepare_release.render_package_selection(selection)

        self.assertEqual(
            ["3.2.0", "3.3.0"],
            [release.version_text for release in selection.qualifying_releases],
        )
        self.assertIn("Latest feature prose.", rendered)
        self.assertIn("Intermediate feature prose.", rendered)
        self.assertIn("Earlier breaking prose.", rendered)
        self.assertIn("Latest breaking prose.", rendered)
        self.assertNotIn("Before-cutoff breaking prose.", rendered)
        self.assertIn("2026-07-01", rendered)
        self.assertNotIn("2026-05-01", rendered)

    def test_breaking_changes_only_package_is_included(self):
        library = self.library(
            "azure-resourcemanager-beta", "3.3.0", "package-multiple-minors.md"
        )
        selection = prepare_release.select_package_changelog(
            library,
            "## 3.3.0 (2026-07-10)\n\n"
            "### Breaking Changes\n\n"
            "- Breaking-only prose.\n",
            cutoff=date(2026, 5, 10),
            release_date=date(2026, 7, 20),
        )

        rendered = prepare_release.render_package_selection(selection)

        self.assertTrue(selection.has_content)
        self.assertIn("#### Breaking Changes\n\n- Breaking-only prose.", rendered)
        self.assertNotIn("Features Added", rendered)

    def test_changed_file_allowlist_rejects_any_other_path(self):
        allowed = [
            "eng/versioning/version_client.txt",
            "sdk/resourcemanager/azure-resourcemanager/pom.xml",
        ]
        passed, unexpected = prepare_release.validate_changed_files(allowed)
        self.assertTrue(passed)
        self.assertEqual([], unexpected)

        passed, unexpected = prepare_release.validate_changed_files(
            allowed + ["sdk/compute/azure-resourcemanager-compute/CHANGELOG.md"]
        )
        self.assertFalse(passed)
        self.assertEqual(
            ["sdk/compute/azure-resourcemanager-compute/CHANGELOG.md"], unexpected
        )

    @patch.object(prepare_release, "datetime")
    def test_release_writes_current_date_and_all_breaking_changes(self, clock):
        self.write_release_fixture_repo()
        clock.now.return_value = datetime(2026, 7, 20, tzinfo=timezone.utc)
        changelog_relative = (
            "sdk/resourcemanager/azure-resourcemanager/CHANGELOG.md"
        )
        changed_files = ["eng/versioning/version_client.txt", changelog_relative]

        with (
            patch.object(prepare_release, "run_version_propagation") as propagate,
            patch.object(
                prepare_release, "get_changed_files",
                side_effect=[[], changed_files],
            ),
        ):
            summary, exit_code = prepare_release.prepare_release(self.repo_root)

        self.assertEqual(0, exit_code)
        self.assertEqual("ready", summary["status"])
        self.assertEqual("2026-07-20", summary["release_date"])
        propagate.assert_called_once_with(self.repo_root.resolve())
        changelog = (self.repo_root / changelog_relative).read_text(encoding="utf-8")
        self.assertIn("## 5.1.0 (2026-07-20)", changelog)
        self.assertIn("- Breaking wording must remain exactly intact.", changelog)
        self.assertIn("- Earlier breaking prose.", changelog)
        self.assertIn("- Latest breaking prose.", changelog)
        self.assertNotIn("Before-cutoff breaking prose.", changelog)
        self.assertNotIn("Patch-only prose", changelog)

        rerun, exit_code = prepare_release.prepare_release(
            self.repo_root, dry_run=True
        )

        self.assertEqual(0, exit_code)
        self.assertEqual("no_changes", rerun["status"])
        self.assertEqual("2026-07-20", rerun["release_date"])
        self.assertEqual([], rerun["planned_files"])

    @patch.object(prepare_release, "datetime")
    def test_dry_run_uses_defaults_and_does_not_edit_fixture_repo(self, clock):
        self.write_release_fixture_repo()
        before = {
            path: path.read_bytes()
            for path in self.repo_root.rglob("*")
            if path.is_file()
        }
        clock.now.return_value = datetime(2026, 7, 1, tzinfo=timezone.utc)

        summary, exit_code = prepare_release.prepare_release(
            self.repo_root, dry_run=True
        )

        self.assertEqual(0, exit_code)
        self.assertEqual("ready", summary["status"])
        self.assertEqual("5.1.0", summary["release_version"])
        self.assertEqual("2026-07-01", summary["release_date"])
        self.assertEqual("2026-05-10", summary["cutoff_date"])
        self.assertEqual([], summary["changed_files"])
        self.assertNotIn("overrides", summary)
        self.assertTrue(
            all(
                "breaking_changes_excluded" not in package
                for package in summary["selected_packages"]
            )
        )
        clock.now.assert_called_once_with(timezone.utc)
        for path, content in before.items():
            self.assertEqual(content, path.read_bytes())


if __name__ == "__main__":
    unittest.main()
