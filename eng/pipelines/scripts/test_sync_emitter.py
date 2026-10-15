# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

import io
import json
import os
import tempfile
import unittest
from unittest.mock import patch

import sync_emitter


FOUNDRY_CORE = "@azure-tools/typespec-foundry-core"


class TestSpecsDependencies(unittest.TestCase):
    def fetch_dependencies(self, dependencies, package=None, workspace=None):
        root = f"{sync_emitter.SPECS_RAW_URL}/specs-commit"
        responses = {
            f"{sync_emitter.SPECS_REPO_API_URL}/commits/main": {"sha": "specs-commit"},
            f"{root}/package.json": {"devDependencies": dependencies},
        }
        if package is not None:
            responses[f"{root}/libs/foundry-core/package.json"] = package
        if workspace is not None:
            responses[f"{root}/pnpm-workspace.yaml"] = workspace

        def urlopen(url, timeout):
            self.assertEqual(timeout, 30)
            return io.BytesIO(json.dumps(responses[url]).encode("utf-8"))

        with patch.object(sync_emitter.urllib.request, "urlopen", side_effect=urlopen):
            return sync_emitter.fetch_specs_dev_dependencies()

    def test_foundry_workspace_version_uses_same_specs_commit(self):
        dependencies = self.fetch_dependencies(
            {FOUNDRY_CORE: "workspace:*"},
            package={"name": FOUNDRY_CORE, "version": "0.1.0-beta.1"},
        )
        self.assertEqual(dependencies[FOUNDRY_CORE], "0.1.0-beta.1")

    def test_foundry_explicit_version(self):
        dependencies = self.fetch_dependencies({FOUNDRY_CORE: "0.2.0"})
        self.assertEqual(dependencies[FOUNDRY_CORE], "0.2.0")

    def test_foundry_catalog_version(self):
        for reference, workspace in (
            ("catalog:", {"catalog": {FOUNDRY_CORE: "0.2.0"}}),
            ("catalog:foundry", {"catalogs": {"foundry": {FOUNDRY_CORE: "0.2.0"}}}),
        ):
            with self.subTest(reference=reference):
                dependencies = self.fetch_dependencies({FOUNDRY_CORE: reference}, workspace=workspace)
                self.assertEqual(dependencies[FOUNDRY_CORE], "0.2.0")

    def test_invalid_workspace_manifest_fails(self):
        for package in (
            {"name": "wrong-package", "version": "0.1.0"},
            {"name": FOUNDRY_CORE},
            {"name": FOUNDRY_CORE, "version": 1},
            {"name": FOUNDRY_CORE, "version": ""},
            {"name": FOUNDRY_CORE, "version": "workspace:*"},
            {"name": FOUNDRY_CORE, "version": "catalog:"},
        ):
            with self.subTest(package=package):
                with self.assertRaisesRegex(ValueError, "matching package name and npm version"):
                    self.fetch_dependencies({FOUNDRY_CORE: "workspace:*"}, package=package)

    def test_unknown_workspace_path_fails(self):
        with self.assertRaisesRegex(ValueError, "unknown specs workspace package path"):
            self.fetch_dependencies({sync_emitter.OPENAI_TYPESPEC_PACKAGE_NAME: "workspace:*"})


class TestDesignatedLibraries(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root_patch = patch.object(sync_emitter, "sdk_root", self.directory.name, create=True)
        self.root_patch.start()
        self.addCleanup(self.root_patch.stop)
        os.mkdir(os.path.join(self.directory.name, "eng"))
        sync_emitter.save_emitter_package_json({"devDependencies": {}})

    def test_add_foundry_from_specs(self):
        with patch.object(
            sync_emitter, "fetch_specs_dev_dependencies", return_value={FOUNDRY_CORE: "0.1.0-beta.1"}
        ), patch.object(sync_emitter, "npm_view_version", return_value="1.0.0") as npm_view:
            sync_emitter.add_designated_libraries(True, {})

        self.assertEqual(
            sync_emitter.load_emitter_package_json()["devDependencies"][FOUNDRY_CORE], "0.1.0-beta.1"
        )
        self.assertNotIn(unittest.mock.call(f"{FOUNDRY_CORE}@latest"), npm_view.call_args_list)

    def test_missing_foundry_falls_back_to_npm(self):
        with patch.object(sync_emitter, "fetch_specs_dev_dependencies", return_value={}), patch.object(
            sync_emitter, "npm_view_version", return_value="0.1.0-beta.1"
        ) as npm_view:
            sync_emitter.add_designated_libraries(True, {})

        npm_view.assert_any_call(f"{FOUNDRY_CORE}@latest")
        self.assertEqual(
            sync_emitter.load_emitter_package_json()["devDependencies"][FOUNDRY_CORE], "0.1.0-beta.1"
        )

    def test_preserve_foundry_when_upgrades_disabled(self):
        dependencies = {library: "0.1.0" for library in sync_emitter.DESIGNATED_LIBRARIES}
        dependencies[FOUNDRY_CORE] = "0.1.0-beta.1"
        sync_emitter.save_emitter_package_json({"devDependencies": dependencies})
        cached_versions = sync_emitter.remove_designated_libraries()
        self.assertNotIn(FOUNDRY_CORE, sync_emitter.load_emitter_package_json()["devDependencies"])

        with patch.object(sync_emitter, "fetch_specs_dev_dependencies") as fetch_specs, patch.object(
            sync_emitter, "npm_view_version"
        ) as npm_view:
            sync_emitter.add_designated_libraries(False, cached_versions)

        fetch_specs.assert_not_called()
        npm_view.assert_not_called()
        self.assertEqual(sync_emitter.load_emitter_package_json()["devDependencies"], dependencies)

    def test_resolve_latest_skips_foundry(self):
        sync_emitter.save_emitter_package_json({"devDependencies": {FOUNDRY_CORE: "0.1.0-beta.1"}})
        with patch.object(sync_emitter, "npm_view_version") as npm_view:
            sync_emitter.resolve_dependency_versions_to_latest()
        npm_view.assert_not_called()
        self.assertEqual(
            sync_emitter.load_emitter_package_json()["devDependencies"][FOUNDRY_CORE], "0.1.0-beta.1"
        )


if __name__ == "__main__":
    unittest.main()
