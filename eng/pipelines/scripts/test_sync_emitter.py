# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

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


if __name__ == "__main__":
    unittest.main()
