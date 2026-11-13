# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

import copy
import json
import os
import tempfile
import unittest
from unittest.mock import patch

import sync_emitter


class DesignatedLibraryTests(unittest.TestCase):
    def setUp(self):
        self.package = {"devDependencies": {}}
        self.cached = {
            "@azure-tools/openai-typespec": "1.34.1",
            "@azure-tools/typespec-liftr-base": "0.13.0",
            "@azure-tools/typespec-azure-portal-core": "0.73.0",
            "@typespec/openapi3": "1.17.0",
        }
        self.enterContext(patch.object(sync_emitter, "load_emitter_package_json", return_value=self.package))
        self.save = self.enterContext(patch.object(sync_emitter, "save_emitter_package_json"))
        self.specs = self.enterContext(
            patch.object(
                sync_emitter,
                "fetch_specs_dev_dependencies",
                return_value={
                    "@azure-tools/openai-typespec": "1.35.0",
                    "@azure-tools/typespec-liftr-base": "0.14.0",
                },
            )
        )
        versions = {
            "@azure-tools/typespec-azure-portal-core@latest": "0.74.0",
            "@typespec/openapi3@latest": "1.18.0",
        }
        self.npm = self.enterContext(patch.object(sync_emitter, "npm_view_version", side_effect=versions.__getitem__))

    def test_upgrades_other_libraries_but_preserves_openai(self):
        sync_emitter.add_designated_libraries(True, self.cached)

        self.assertEqual(
            self.package["devDependencies"],
            {
                "@azure-tools/openai-typespec": "1.34.1",
                "@azure-tools/typespec-liftr-base": "0.14.0",
                "@azure-tools/typespec-azure-portal-core": "0.74.0",
                "@typespec/openapi3": "1.18.0",
            },
        )

    def test_disabled_upgrades_preserve_all_libraries(self):
        sync_emitter.add_designated_libraries(False, self.cached)

        self.assertEqual(self.package["devDependencies"], self.cached)
        self.specs.assert_not_called()
        self.npm.assert_not_called()

    def test_missing_openai_pin_does_not_upgrade_it(self):
        del self.cached["@azure-tools/openai-typespec"]

        with self.assertRaisesRegex(ValueError, "Cannot preserve @azure-tools/openai-typespec"):
            sync_emitter.add_designated_libraries(True, self.cached)

        self.save.assert_not_called()


class TcgcPreviewTests(unittest.TestCase):
    def test_preview_preserves_other_dependencies_and_reuses_lock(self):
        package = {
            "dependencies": {"@azure-tools/typespec-java": "0.49.0"},
            "devDependencies": {
                "@azure-tools/typespec-client-generator-core": "0.74.0",
                "@azure-tools/openai-typespec": "1.34.1",
                "@typespec/compiler": "1.18.0",
            },
        }
        expected = copy.deepcopy(package)
        expected["devDependencies"]["@azure-tools/typespec-client-generator-core"] = (
            "https://pkg.pr.new/@azure-tools/typespec-client-generator-core@267ed002645d055e479e042372dc951fdb34eb87"
        )
        existing_lock = {"lockfileVersion": 3, "packages": {"": {}}}
        generated_lock = {"lockfileVersion": 3, "packages": {"": expected}}

        def install(command, cwd):
            with open(os.path.join(cwd, "package.json")) as manifest:
                self.assertEqual(json.load(manifest), expected)
            lock_path = os.path.join(cwd, "package-lock.json")
            with open(lock_path) as lock:
                self.assertEqual(json.load(lock), existing_lock)
            with open(lock_path, "w") as lock:
                json.dump(generated_lock, lock)

        with tempfile.TemporaryDirectory() as root:
            os.mkdir(os.path.join(root, "eng"))
            lock_path = os.path.join(root, "eng", "emitter-package-lock.json")
            with open(lock_path, "w") as lock:
                json.dump(existing_lock, lock)
            with (
                patch.object(sync_emitter, "sdk_root", root, create=True),
                patch.object(sync_emitter, "load_emitter_package_json", return_value=package),
                patch.object(sync_emitter, "save_emitter_package_json") as save,
                patch.object(sync_emitter.subprocess, "check_call", side_effect=install),
            ):
                sync_emitter.update_tcgc_preview()
                save.assert_called_once_with(expected)
            with open(lock_path) as lock:
                self.assertEqual(json.load(lock), generated_lock)

    def test_regeneration_bypasses_emitter_and_dependency_upgrades(self):
        with (
            patch.object(sync_emitter, "parse_args") as args,
            patch.object(sync_emitter, "update_emitter") as emitter,
            patch.object(sync_emitter, "update_tcgc_preview") as preview,
            patch.object(sync_emitter, "update_sdks") as sdks,
            patch.object(sync_emitter, "apply_patches") as patches,
        ):
            args.return_value = sync_emitter.argparse.Namespace(sdk_root="sdk")
            sync_emitter.main()
            emitter.assert_not_called()
            preview.assert_called_once_with()
            sdks.assert_called_once_with()
            patches.assert_called_once_with()


if __name__ == "__main__":
    unittest.main()
