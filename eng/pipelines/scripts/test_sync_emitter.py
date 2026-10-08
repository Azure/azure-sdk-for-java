# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

import json
import unittest
from unittest.mock import MagicMock, call, patch

import yaml

import sync_emitter


class TestFetchSpecsDevDependencies(unittest.TestCase):
    COMMIT = "a" * 40
    PACKAGE = sync_emitter.OPENAI_TYPESPEC_PACKAGE_NAME
    OTHER_PACKAGE = "@azure-tools/typespec-liftr-base"

    @staticmethod
    def response(content):
        response = MagicMock()
        response.__enter__.return_value.read.return_value = content.encode("utf-8")
        return response

    def fetch(self, dependencies, workspace=None):
        responses = [
            self.response(json.dumps({"sha": self.COMMIT})),
            self.response(json.dumps({"devDependencies": dependencies})),
        ]
        if workspace is not None:
            responses.append(self.response(yaml.safe_dump(workspace)))
        with patch("sync_emitter.urllib.request.urlopen", side_effect=responses) as urlopen:
            result = sync_emitter.fetch_specs_dev_dependencies()
        return result, urlopen

    def test_direct_versions_do_not_fetch_workspace(self):
        dependencies = {self.PACKAGE: "1.28.0", self.OTHER_PACKAGE: "0.13.0", "other": "catalog:"}
        result, urlopen = self.fetch(dependencies)
        self.assertEqual(result, dependencies)
        self.assertEqual(
            urlopen.call_args_list,
            [
                call("https://api.github.com/repos/Azure/azure-rest-api-specs/commits/main", timeout=30),
                call(
                    f"https://raw.githubusercontent.com/Azure/azure-rest-api-specs/{self.COMMIT}/package.json",
                    timeout=30,
                ),
            ],
        )

    def test_default_catalog_uses_same_commit_as_package_json(self):
        result, urlopen = self.fetch(
            {self.PACKAGE: "catalog:", self.OTHER_PACKAGE: "catalog:default"},
            {"catalog": {self.PACKAGE: "1.28.0", self.OTHER_PACKAGE: "0.13.0"}},
        )
        self.assertEqual(result, {self.PACKAGE: "1.28.0", self.OTHER_PACKAGE: "0.13.0"})
        urlopen.assert_any_call(
            f"https://raw.githubusercontent.com/Azure/azure-rest-api-specs/{self.COMMIT}/package.json",
            timeout=30,
        )
        urlopen.assert_called_with(
            f"https://raw.githubusercontent.com/Azure/azure-rest-api-specs/{self.COMMIT}/pnpm-workspace.yaml",
            timeout=30,
        )
        self.assertEqual(urlopen.call_count, 3)

    def test_named_catalog(self):
        result, _ = self.fetch(
            {self.PACKAGE: "catalog:preview", self.OTHER_PACKAGE: "0.13.0"},
            {"catalogs": {"preview": {self.PACKAGE: "^1.28.0"}}},
        )
        self.assertEqual(result, {self.PACKAGE: "^1.28.0", self.OTHER_PACKAGE: "0.13.0"})

    def test_default_catalog_under_catalogs(self):
        result, _ = self.fetch(
            {self.PACKAGE: "catalog:"},
            {"catalogs": {"default": {self.PACKAGE: "1.28.0"}}},
        )
        self.assertEqual(result[self.PACKAGE], "1.28.0")

    def test_missing_catalog_or_package_fails(self):
        for reference, workspace in [
            ("catalog:", {}),
            ("catalog:preview", {"catalog": {self.PACKAGE: "1.28.0"}}),
            ("catalog:", {"catalog": {self.OTHER_PACKAGE: "0.13.0"}}),
            ("catalog:preview", {"catalogs": {"preview": {}}}),
        ]:
            with self.subTest(reference=reference, workspace=workspace):
                with self.assertRaisesRegex(ValueError, self.PACKAGE):
                    self.fetch({self.PACKAGE: reference}, workspace)

    def test_invalid_catalog_versions_fail(self):
        for version in [None, "", 123, "catalog:other", "workspace:^", "link:./local", "file:./local"]:
            with self.subTest(version=version):
                with self.assertRaisesRegex(ValueError, self.PACKAGE):
                    self.fetch({self.PACKAGE: "catalog:"}, {"catalog": {self.PACKAGE: version}})

    def test_invalid_workspace_fails(self):
        with self.assertRaisesRegex(ValueError, "pnpm-workspace.yaml"):
            self.fetch({self.PACKAGE: "catalog:"}, ["not", "a", "mapping"])

    def test_missing_dependency_remains_missing(self):
        result, urlopen = self.fetch({})
        self.assertEqual(result, {})
        self.assertEqual(urlopen.call_count, 2)


class TestAddDesignatedLibraries(unittest.TestCase):
    @patch("sync_emitter.save_emitter_package_json")
    @patch("sync_emitter.load_emitter_package_json", return_value={"devDependencies": {}})
    @patch("sync_emitter.npm_view_version")
    @patch("sync_emitter.fetch_specs_dev_dependencies")
    def test_disabled_upgrades_preserve_versions(self, fetch, npm_view, load, save):
        versions = {library: f"1.0.{index}" for index, library in enumerate(sync_emitter.DESIGNATED_LIBRARIES)}
        sync_emitter.add_designated_libraries(False, versions)
        fetch.assert_not_called()
        npm_view.assert_not_called()
        save.assert_called_once_with({"devDependencies": versions})

    @patch("sync_emitter.save_emitter_package_json")
    @patch("sync_emitter.load_emitter_package_json", return_value={"devDependencies": {}})
    @patch("sync_emitter.npm_view_version", return_value="0.73.0")
    @patch(
        "sync_emitter.fetch_specs_dev_dependencies",
        return_value={sync_emitter.OPENAI_TYPESPEC_PACKAGE_NAME: "1.28.0"},
    )
    def test_missing_specs_dependency_still_uses_npm_latest(self, fetch, npm_view, load, save):
        sync_emitter.add_designated_libraries(True, {})
        self.assertEqual(
            npm_view.call_args_list,
            [
                call("@azure-tools/typespec-liftr-base@latest"),
                call("@azure-tools/typespec-azure-portal-core@latest"),
                call("@typespec/openapi3@latest"),
            ],
        )
        save.assert_called_once_with(
            {
                "devDependencies": {
                    sync_emitter.OPENAI_TYPESPEC_PACKAGE_NAME: "1.28.0",
                    "@azure-tools/typespec-liftr-base": "0.73.0",
                    "@azure-tools/typespec-azure-portal-core": "0.73.0",
                    "@typespec/openapi3": "0.73.0",
                }
            }
        )


if __name__ == "__main__":
    unittest.main()
