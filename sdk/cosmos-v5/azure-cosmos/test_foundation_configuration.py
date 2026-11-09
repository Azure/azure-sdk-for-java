# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

"""Regression checks for the opt-in v5 build and version configuration."""

import importlib.util
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch
import xml.etree.ElementTree as ET


MODULE = Path(__file__).resolve().parent
ROOT = MODULE.parents[2]
SPEC = importlib.util.spec_from_file_location("foundation", MODULE / "verify-foundation.py")
foundation = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(foundation)


class FoundationConfigurationTests(unittest.TestCase):
    def test_isolated_version_configuration(self):
        foundation.verify_version_configuration()

    def test_version_drift_is_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            candidate = Path(directory) / "foundation.xml"
            version = ET.parse(foundation.BUILD).getroot().findtext("m:version", namespaces=foundation.NS)
            candidate.write_text(foundation.BUILD.read_text().replace(
                f"<version>{version}</version>", "<version>4.0.0</version>", 1))
            with patch.object(foundation, "BUILD", candidate):
                with self.assertRaisesRegex(RuntimeError, "differs from versions.txt"):
                    foundation.verify_version_configuration()

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

    def test_reactors_never_select_both_cosmos_versions(self):
        def projects_in(path):
            project = ET.parse(path).getroot()
            yield path.resolve(), project
            for module in project.findall("m:modules/m:module", foundation.NS):
                child = path.parent / module.text
                yield from projects_in(child / "pom.xml" if child.is_dir() else child)

        for reactor, expected in ((ROOT / "pom.xml", ROOT / "sdk/cosmos/azure-cosmos/pom.xml"),
                                  (foundation.REACTOR, foundation.BUILD)):
            selected = [
                path for path, project in projects_in(reactor)
                if project.findtext("m:groupId", namespaces=foundation.NS) == "com.azure"
                and project.findtext("m:artifactId", namespaces=foundation.NS) == "azure-cosmos"
            ]
            self.assertEqual(selected, [expected.resolve()])


if __name__ == "__main__":
    unittest.main()
