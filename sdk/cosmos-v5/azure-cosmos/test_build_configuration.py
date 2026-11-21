# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

"""Regression checks for the opt-in v5 build and version configuration."""

import importlib.util
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch
import xml.etree.ElementTree as ET


MODULE = Path(__file__).resolve().parent
ROOT = MODULE.parents[2]
SPEC = importlib.util.spec_from_file_location("verify_build", MODULE / "verify-build.py")
verify_build = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(verify_build)


class BuildConfigurationTests(unittest.TestCase):
    def test_isolated_version_configuration(self):
        verify_build.verify_version_configuration()

    def test_version_drift_is_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            candidate = Path(directory) / "sdk-build.xml"
            version = ET.parse(verify_build.BUILD).getroot().findtext("m:version", namespaces=verify_build.NS)
            candidate.write_text(verify_build.BUILD.read_text().replace(
                f"<version>{version}</version>", "<version>4.0.0</version>", 1))
            with patch.object(verify_build, "BUILD", candidate):
                with self.assertRaisesRegex(RuntimeError, "differs from versions.txt"):
                    verify_build.verify_version_configuration()

    def test_shared_aggregate_discovery_selects_only_v4(self):
        sys.path.insert(0, str(ROOT / "eng/scripts"))
        import generate_aggregate_pom

        with patch.object(generate_aggregate_pom, "include_groups", ["com.azure"]):
            projects = generate_aggregate_pom.create_projects(["com.azure:azure-cosmos"], {})
        self.assertEqual(len(projects), 1)
        self.assertEqual(projects["com.azure:azure-cosmos"].module_path,
                         "/sdk/cosmos/azure-cosmos/pom.xml")
        # Both the shared version updater and scanner enumerate pom*.xml.
        self.assertEqual(list(MODULE.parent.rglob("pom*.xml")), [])

    def test_local_plugin_version_drift_is_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            candidate = Path(directory) / "sdk-build.xml"
            project = ET.parse(verify_build.BUILD).getroot()
            plugin = project.find(
                "m:build/m:plugins/m:plugin[m:artifactId='maven-deploy-plugin']", verify_build.NS)
            version = plugin.findtext("m:version", namespaces=verify_build.NS)
            marker = "{x-version-update;org.apache.maven.plugins:maven-deploy-plugin;external_dependency}"
            candidate.write_text(verify_build.BUILD.read_text().replace(
                f"<version>{version}</version> <!-- {marker} -->",
                f"<version>0.0.0</version> <!-- {marker} -->", 1))
            with patch.object(verify_build, "BUILD", candidate):
                with self.assertRaisesRegex(RuntimeError, "differs from versions.txt"):
                    verify_build.verify_version_configuration()

    def test_local_external_dependencies_cannot_duplicate_entries(self):
        local = verify_build.LOCAL_EXTERNAL_VERSIONS.read_text()
        shared = (ROOT / "eng/versioning/external_dependencies.txt").read_text()
        for label, content in (("local", local), ("shared", shared)):
            entry = next(line for line in content.splitlines() if line.strip() and not line.startswith("#"))
            with self.subTest(source=label), tempfile.TemporaryDirectory() as directory:
                candidate = Path(directory) / "external_dependencies.txt"
                candidate.write_text(local + "\n" + entry + "\n")
                with patch.object(verify_build, "LOCAL_EXTERNAL_VERSIONS", candidate):
                    with self.assertRaisesRegex(ValueError, "duplicate entry"):
                        verify_build.verify_version_configuration()

    def test_shared_version_manifests_have_no_unused_entries(self):
        result = subprocess.run(
            [sys.executable, "-B", str(ROOT / "eng/versioning/find_unused_dependencies.py")],
            cwd=ROOT, capture_output=True, text=True)
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_reactors_never_select_both_cosmos_versions(self):
        def projects_in(path):
            project = ET.parse(path).getroot()
            yield path.resolve(), project
            for module in project.findall("m:modules/m:module", verify_build.NS):
                child = path.parent / module.text
                yield from projects_in(child / "pom.xml" if child.is_dir() else child)

        for reactor, expected in ((ROOT / "pom.xml", ROOT / "sdk/cosmos/azure-cosmos/pom.xml"),
                                  (verify_build.REACTOR, verify_build.BUILD)):
            selected = [
                path for path, project in projects_in(reactor)
                if project.findtext("m:groupId", namespaces=verify_build.NS) == "com.azure"
                and project.findtext("m:artifactId", namespaces=verify_build.NS) == "azure-cosmos"
            ]
            self.assertEqual(selected, [expected.resolve()])


if __name__ == "__main__":
    unittest.main()
