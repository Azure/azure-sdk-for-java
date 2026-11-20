# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

import os
import tempfile
import unittest

from find_unused_dependencies import remove_unused_external_dependencies


class TestRemoveUnusedExternalDependencies(unittest.TestCase):

    def setUp(self):
        self.temp_dir = tempfile.TemporaryDirectory()
        self.repository_root = self.temp_dir.name
        self.versioning_dir = os.path.join(self.repository_root, "eng", "versioning")
        os.makedirs(self.versioning_dir)
        self.dependency_file = os.path.join(self.versioning_dir, "external_dependencies.txt")

    def tearDown(self):
        self.temp_dir.cleanup()

    def test_removes_only_unreferenced_entries(self):
        contents = (
            "# External dependencies\n"
            "\n"
            "com.example:referenced;1.0.0\n"
            "com.example:unused;2.0.0\n"
            "springboot3_org.springframework.boot:spring-boot-dependencies;3.5.14\n"
            "springboot3_org.springframework.cloud:spring-cloud-dependencies;2025.0.2\n"
        )
        self._write_file(self.dependency_file, contents)
        self._write_pom(
            "sdk/example/pom.xml",
            "<version>1.0.0</version> <!-- {x-version-update;com.example:referenced;external_dependency} -->",
        )

        removed = remove_unused_external_dependencies(self.repository_root, self.dependency_file)

        self.assertEqual(removed, ["com.example:unused"])
        with open(self.dependency_file, encoding="utf-8") as file:
            self.assertEqual(
                file.read(),
                (
                    "# External dependencies\n"
                    "\n"
                    "com.example:referenced;1.0.0\n"
                    "springboot3_org.springframework.boot:spring-boot-dependencies;3.5.14\n"
                    "springboot3_org.springframework.cloud:spring-cloud-dependencies;2025.0.2\n"
                ),
            )

    def test_ignores_references_outside_pom_files(self):
        self._write_file(
            self.dependency_file,
            "com.example:unused;1.0.0\n",
        )
        self._write_file(
            os.path.join(self.repository_root, "sdk", "example", "dependencies.xml"),
            "{x-version-update;com.example:unused;external_dependency}\n",
        )

        removed = remove_unused_external_dependencies(self.repository_root, self.dependency_file)

        self.assertEqual(removed, ["com.example:unused"])
        with open(self.dependency_file, encoding="utf-8") as file:
            self.assertEqual(file.read(), "")

    def test_preserves_include_only_external_dependency(self):
        self._write_file(self.dependency_file, "spring6_com.example:included;1.0.0\n")
        self._write_pom(
            "sdk/example/pom.xml",
            "<include>com.example:included:[1.0.0]</include>"
            " <!-- {x-include-update;spring6_com.example:included;external_dependency} -->",
        )

        self.assertEqual(remove_unused_external_dependencies(self.repository_root, self.dependency_file), [])
        with open(self.dependency_file, encoding="utf-8") as file:
            self.assertEqual(file.read(), "spring6_com.example:included;1.0.0\n")

    def _write_pom(self, relative_path, marker):
        self._write_file(
            os.path.join(self.repository_root, relative_path),
            "<project>\n  {}\n</project>\n".format(marker),
        )

    @staticmethod
    def _write_file(path, contents):
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8") as file:
            file.write(contents)


if __name__ == "__main__":
    unittest.main()
