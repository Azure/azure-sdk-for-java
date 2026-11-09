import copy
import os
import shutil
import tempfile
import unittest
import yaml

from generate import update_revapi_skip
from generate_utils import should_remove_generated_source_code
from utils import is_first_release, update_ci_path_filters, update_service_files_for_new_lib

POM_WITH_REVAPI_TRUE = """\
<project>
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <jacoco.min.linecoverage>0</jacoco.min.linecoverage>
    <revapi.skip>true</revapi.skip>
  </properties>
</project>
"""

POM_WITH_REVAPI_FALSE = """\
<project>
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <jacoco.min.linecoverage>0</jacoco.min.linecoverage>
    <revapi.skip>false</revapi.skip>
  </properties>
</project>
"""

POM_WITHOUT_REVAPI = """\
<project>
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <jacoco.min.linecoverage>0</jacoco.min.linecoverage>
  </properties>
</project>
"""


class TestUpdateRevapiSkip(unittest.TestCase):

    def _write_and_update(self, content: str, beta: bool) -> str:
        fd, path = tempfile.mkstemp(suffix=".xml")
        try:
            with os.fdopen(fd, "w") as f:
                f.write(content)
            update_revapi_skip(path, beta)
            with open(path, "r") as f:
                return f.read()
        finally:
            os.unlink(path)

    # --- beta=True cases ---

    def test_beta_already_true_no_change(self):
        result = self._write_and_update(POM_WITH_REVAPI_TRUE, beta=True)
        self.assertEqual(result, POM_WITH_REVAPI_TRUE)

    def test_beta_false_flipped_to_true(self):
        result = self._write_and_update(POM_WITH_REVAPI_FALSE, beta=True)
        self.assertIn("<revapi.skip>true</revapi.skip>", result)
        self.assertNotIn("<revapi.skip>false</revapi.skip>", result)

    def test_beta_missing_added_true(self):
        result = self._write_and_update(POM_WITHOUT_REVAPI, beta=True)
        self.assertIn("<revapi.skip>true</revapi.skip>", result)
        self.assertIn("</properties>", result)

    # --- beta=False (stable) cases ---

    def test_stable_true_flipped_to_false(self):
        result = self._write_and_update(POM_WITH_REVAPI_TRUE, beta=False)
        self.assertIn("<revapi.skip>false</revapi.skip>", result)
        self.assertNotIn("<revapi.skip>true</revapi.skip>", result)

    def test_stable_already_false_no_change(self):
        result = self._write_and_update(POM_WITH_REVAPI_FALSE, beta=False)
        self.assertEqual(result, POM_WITH_REVAPI_FALSE)

    def test_stable_missing_not_added(self):
        result = self._write_and_update(POM_WITHOUT_REVAPI, beta=False)
        self.assertNotIn("revapi.skip", result)
        self.assertEqual(result, POM_WITHOUT_REVAPI)


class TestIsFirstRelease(unittest.TestCase):

    GROUP = "com.azure.resourcemanager"
    MODULE = "azure-resourcemanager-foo"

    def _make_sdk_root(self, version_file_content):
        sdk_root = tempfile.mkdtemp()
        versioning_dir = os.path.join(sdk_root, "eng", "versioning")
        os.makedirs(versioning_dir, exist_ok=True)
        if version_file_content is not None:
            with open(os.path.join(versioning_dir, "version_client.txt"), "w") as f:
                f.write(version_file_content)
        return sdk_root

    def test_entry_missing_returns_true(self):
        content = (
            "# comment line\n"
            "com.azure.resourcemanager:azure-resourcemanager-other;1.2.0;1.2.0\n"
        )
        sdk_root = self._make_sdk_root(content)
        self.assertTrue(is_first_release(sdk_root, self.GROUP, self.MODULE))

    def test_entry_with_default_versions_returns_true(self):
        content = (
            "com.azure.resourcemanager:azure-resourcemanager-foo;1.0.0-beta.1;1.0.0-beta.1\n"
        )
        sdk_root = self._make_sdk_root(content)
        self.assertTrue(is_first_release(sdk_root, self.GROUP, self.MODULE))

    def test_entry_with_published_stable_returns_false(self):
        content = (
            "com.azure.resourcemanager:azure-resourcemanager-foo;1.2.0;1.3.0-beta.1\n"
        )
        sdk_root = self._make_sdk_root(content)
        self.assertFalse(is_first_release(sdk_root, self.GROUP, self.MODULE))

    def test_entry_with_bumped_beta_returns_false(self):
        content = (
            "com.azure.resourcemanager:azure-resourcemanager-foo;1.0.0-beta.2;1.0.0-beta.2\n"
        )
        sdk_root = self._make_sdk_root(content)
        self.assertFalse(is_first_release(sdk_root, self.GROUP, self.MODULE))

    def test_malformed_entry_returns_false(self):
        content = "com.azure.resourcemanager:azure-resourcemanager-foo;1.0.0-beta.1\n"
        sdk_root = self._make_sdk_root(content)
        self.assertFalse(is_first_release(sdk_root, self.GROUP, self.MODULE))

    def test_missing_file_returns_false(self):
        sdk_root = self._make_sdk_root(None)
        self.assertFalse(is_first_release(sdk_root, self.GROUP, self.MODULE))


class TestGeneratedSourceCleanup(unittest.TestCase):

    def test_resources_module_cleanup_is_disabled(self):
        self.assertFalse(should_remove_generated_source_code("azure-resourcemanager-resources"))

    def test_other_module_cleanup_is_enabled(self):
        self.assertTrue(should_remove_generated_source_code("azure-resourcemanager-compute"))


class TestUpdateCiPathFilters(unittest.TestCase):

    SERVICE = "network"
    MODULE = "azure-resourcemanager-network-extra"
    INCLUDE_PATH = "sdk/network/azure-resourcemanager-network-extra/"
    EXCLUDE_PATH = "sdk/network/azure-resourcemanager-network-extra/pom.xml"

    def setUp(self):
        self.ci_yml = {
            "trigger": {
                "paths": {
                    "include": ["sdk/network/existing/"],
                    "exclude": ["sdk/network/existing/pom.xml"],
                }
            },
            "pr": {
                "paths": {
                    "include": ["sdk/network/existing/"],
                    "exclude": ["sdk/network/existing/pom.xml"],
                }
            },
        }

    def test_adds_package_filters_and_preserves_existing_entries(self):
        self.assertTrue(update_ci_path_filters(self.ci_yml, self.SERVICE, self.MODULE))

        for trigger_type in ("trigger", "pr"):
            paths = self.ci_yml[trigger_type]["paths"]
            self.assertEqual(paths["include"], ["sdk/network/existing/", self.INCLUDE_PATH])
            self.assertEqual(paths["exclude"], ["sdk/network/existing/pom.xml", self.EXCLUDE_PATH])

    def test_is_idempotent(self):
        update_ci_path_filters(self.ci_yml, self.SERVICE, self.MODULE)

        self.assertFalse(update_ci_path_filters(self.ci_yml, self.SERVICE, self.MODULE))
        for trigger_type in ("trigger", "pr"):
            paths = self.ci_yml[trigger_type]["paths"]
            self.assertEqual(paths["include"].count(self.INCLUDE_PATH), 1)
            self.assertEqual(paths["exclude"].count(self.EXCLUDE_PATH), 1)

    def test_initializes_missing_filter_lists_for_each_trigger_type(self):
        expected_paths = {"include": self.INCLUDE_PATH, "exclude": self.EXCLUDE_PATH}
        for trigger_type in ("trigger", "pr"):
            for filter_type, expected_path in expected_paths.items():
                with self.subTest(trigger_type=trigger_type, filter_type=filter_type):
                    ci_yml = copy.deepcopy(self.ci_yml)
                    del ci_yml[trigger_type]["paths"][filter_type]

                    self.assertTrue(
                        update_ci_path_filters(ci_yml, self.SERVICE, self.MODULE)
                    )
                    self.assertEqual(
                        ci_yml[trigger_type]["paths"][filter_type], [expected_path]
                    )

    def test_initializes_absent_paths_mapping(self):
        del self.ci_yml["trigger"]["paths"]

        self.assertTrue(update_ci_path_filters(self.ci_yml, self.SERVICE, self.MODULE))
        self.assertEqual(
            self.ci_yml["trigger"]["paths"],
            {"include": [self.INCLUDE_PATH], "exclude": [self.EXCLUDE_PATH]},
        )

    def test_preserves_each_disabled_trigger_and_updates_the_other(self):
        for disabled_trigger in ("trigger", "pr"):
            with self.subTest(disabled_trigger=disabled_trigger):
                ci_yml = copy.deepcopy(self.ci_yml)
                ci_yml[disabled_trigger] = "none"

                self.assertTrue(update_ci_path_filters(ci_yml, self.SERVICE, self.MODULE))
                self.assertEqual(ci_yml[disabled_trigger], "none")

                enabled_trigger = "pr" if disabled_trigger == "trigger" else "trigger"
                self.assertIn(
                    self.INCLUDE_PATH, ci_yml[enabled_trigger]["paths"]["include"]
                )
                self.assertIn(
                    self.EXCLUDE_PATH, ci_yml[enabled_trigger]["paths"]["exclude"]
                )

    def test_preserves_both_disabled_triggers(self):
        ci_yml = {"trigger": "none", "pr": "none"}

        self.assertFalse(update_ci_path_filters(ci_yml, self.SERVICE, self.MODULE))
        self.assertEqual(ci_yml, {"trigger": "none", "pr": "none"})

    def test_is_idempotent_with_initialized_filters_and_disabled_trigger(self):
        ci_yml = copy.deepcopy(self.ci_yml)
        ci_yml["trigger"] = "none"
        del ci_yml["pr"]["paths"]["include"]
        del ci_yml["pr"]["paths"]["exclude"]

        self.assertTrue(update_ci_path_filters(ci_yml, self.SERVICE, self.MODULE))
        self.assertFalse(update_ci_path_filters(ci_yml, self.SERVICE, self.MODULE))
        self.assertEqual(ci_yml["trigger"], "none")
        self.assertEqual(ci_yml["pr"]["paths"]["include"], [self.INCLUDE_PATH])
        self.assertEqual(ci_yml["pr"]["paths"]["exclude"], [self.EXCLUDE_PATH])

    def test_skips_present_non_list_filter_and_updates_sibling(self):
        for malformed_value in (None, "sdk/network/pom.xml", {}):
            with self.subTest(malformed_value=malformed_value):
                ci_yml = copy.deepcopy(self.ci_yml)
                ci_yml["pr"]["paths"]["exclude"] = malformed_value

                with self.assertLogs(level="WARNING") as logs:
                    update_ci_path_filters(ci_yml, self.SERVICE, self.MODULE)
                self.assertIs(ci_yml["pr"]["paths"]["exclude"], malformed_value)
                self.assertIn(self.INCLUDE_PATH, ci_yml["pr"]["paths"]["include"])
                self.assertIn(self.INCLUDE_PATH, ci_yml["trigger"]["paths"]["include"])
                self.assertTrue(
                    any("pr.paths.exclude" in message for message in logs.output)
                )

    def test_skips_omitted_and_non_mapping_triggers(self):
        for malformed_value in (None, "None", "disabled", ["main"]):
            with self.subTest(malformed_value=malformed_value):
                ci_yml = copy.deepcopy(self.ci_yml)
                if malformed_value is None:
                    del ci_yml["trigger"]
                else:
                    ci_yml["trigger"] = malformed_value

                self.assertTrue(update_ci_path_filters(ci_yml, self.SERVICE, self.MODULE))
                if malformed_value is None:
                    self.assertNotIn("trigger", ci_yml)
                else:
                    self.assertIs(ci_yml["trigger"], malformed_value)
                self.assertIn(self.INCLUDE_PATH, ci_yml["pr"]["paths"]["include"])

    def test_skips_present_non_mapping_paths_and_updates_other_trigger(self):
        for malformed_value in (None, "sdk/network/", []):
            with self.subTest(malformed_value=malformed_value):
                ci_yml = copy.deepcopy(self.ci_yml)
                ci_yml["trigger"]["paths"] = malformed_value

                with self.assertLogs(level="WARNING") as logs:
                    self.assertTrue(
                        update_ci_path_filters(ci_yml, self.SERVICE, self.MODULE)
                    )
                self.assertIs(ci_yml["trigger"]["paths"], malformed_value)
                self.assertIn(self.INCLUDE_PATH, ci_yml["pr"]["paths"]["include"])
                self.assertTrue(
                    any("trigger.paths" in message for message in logs.output)
                )


class TestUpdateServiceFilesForNewLib(unittest.TestCase):

    def setUp(self):
        self.sdk_root = tempfile.mkdtemp()
        self.service = "network"
        self.module = "azure-resourcemanager-network-extra"
        self.service_dir = os.path.join(self.sdk_root, "sdk", self.service)
        self.module_dir = os.path.join(self.service_dir, self.module)
        os.makedirs(self.module_dir)

        with open(os.path.join(self.service_dir, "pom.xml"), "w") as f:
            f.write("<project><modules><module>existing</module></modules></project>")

        self.ci_file = os.path.join(self.service_dir, "ci.yml")
        with open(self.ci_file, "w") as f:
            yaml.safe_dump(
                {
                    "trigger": {
                        "paths": {
                            "include": ["sdk/network/existing/"],
                            "exclude": ["sdk/network/existing/pom.xml"],
                        }
                    },
                    "pr": {
                        "paths": {
                            "include": ["sdk/network/existing/"],
                            "exclude": ["sdk/network/existing/pom.xml"],
                        }
                    },
                    "extends": {
                        "parameters": {
                            "Artifacts": [
                                {
                                    "name": self.module,
                                    "groupId": "com.azure.resourcemanager",
                                    "safeName": "azureresourcemanagernetworkextra",
                                }
                            ]
                        }
                    },
                },
                f,
                sort_keys=False,
            )

    def tearDown(self):
        shutil.rmtree(self.sdk_root)

    def test_updates_filters_when_artifact_already_exists(self):
        update_service_files_for_new_lib(
            self.sdk_root, self.service, "com.azure.resourcemanager", self.module
        )

        with open(self.ci_file, "r") as f:
            ci_yml = yaml.safe_load(f)
        for trigger_type in ("trigger", "pr"):
            paths = ci_yml[trigger_type]["paths"]
            self.assertIn("sdk/network/azure-resourcemanager-network-extra/", paths["include"])
            self.assertIn(
                "sdk/network/azure-resourcemanager-network-extra/pom.xml", paths["exclude"]
            )

    def test_malformed_filters_do_not_block_artifact_update(self):
        with open(self.ci_file, "r") as f:
            ci_yml = yaml.safe_load(f)
        ci_yml["trigger"]["paths"] = None
        ci_yml["pr"]["paths"]["exclude"] = None
        ci_yml["extends"]["parameters"]["Artifacts"] = []
        with open(self.ci_file, "w") as f:
            yaml.safe_dump(ci_yml, f, sort_keys=False)

        update_service_files_for_new_lib(
            self.sdk_root, self.service, "com.azure.resourcemanager", self.module
        )

        with open(self.ci_file, "r") as f:
            updated_ci_yml = yaml.safe_load(f)
        self.assertIsNone(updated_ci_yml["trigger"]["paths"])
        self.assertIsNone(updated_ci_yml["pr"]["paths"]["exclude"])
        self.assertIn(
            "sdk/network/azure-resourcemanager-network-extra/",
            updated_ci_yml["pr"]["paths"]["include"],
        )
        self.assertEqual(
            updated_ci_yml["extends"]["parameters"]["Artifacts"][0]["name"], self.module
        )


if __name__ == "__main__":
    unittest.main()
