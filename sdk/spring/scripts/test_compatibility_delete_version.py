# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import xml.etree.ElementTree as elementTree

import compatibility_delete_version as cleanup
from compatibility_insert_dependencymanagement import update_spring_boot_starter_parent_for_file


NAMESPACE = {'maven': 'http://maven.apache.org/POM/4.0.0'}
MANAGED_ARTIFACTS = {
    'springboot3_org.apache.maven.plugins:maven-compiler-plugin',
    'springboot3_org.springframework:spring-core',
    'springboot3_org.springframework.boot:spring-boot-starter-parent',
    'com.github.tomakehurst:wiremock-jre8'
}
COMPILER_PLUGIN = """<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>3.14.1</version> <!-- {x-version-update;springboot3_org.apache.maven.plugins:maven-compiler-plugin;external_dependency} -->
</plugin>"""
DEPENDENCY = """<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-core</artifactId>
  <version>6.2.18</version> <!-- {x-version-update;springboot3_org.springframework:spring-core;external_dependency} -->
</dependency>"""


def project(content):
    return '<project xmlns="http://maven.apache.org/POM/4.0.0">\n{}</project>\n'.format(content)


class CompatibilityDeleteVersionTests(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.pom = Path(self.directory.name) / 'pom.xml'

    def clean(self, content, ignored_artifacts=None):
        self.pom.write_text(content, encoding='utf-8')
        cleanup.delete_dependency_version(str(self.pom), ignored_artifacts or set(), MANAGED_ARTIFACTS)
        return self.pom.read_text(encoding='utf-8')

    def test_preserves_plugin_versions_in_all_plugin_sections(self):
        sections = [
            '<build><plugins>{}</plugins></build>',
            '<build><pluginManagement><plugins>{}</plugins></pluginManagement></build>',
            '<profiles><profile><id>java17</id><build><plugins>{}</plugins></build></profile></profiles>',
            '<profiles><profile><id>java17</id><build><pluginManagement><plugins>{}</plugins>'
            '</pluginManagement></build></profile></profiles>',
            '<reporting><plugins>{}</plugins></reporting>'
        ]
        for section in sections:
            with self.subTest(section=section):
                content = project(section.format(COMPILER_PLUGIN))
                self.assertEqual(self.clean(content), content)

    def test_preserves_plugin_dependencies_but_removes_project_dependency_versions(self):
        plugin = COMPILER_PLUGIN.replace('</plugin>', '<dependencies>\n' + DEPENDENCY + '\n</dependencies></plugin>')
        content = project(
            '<!-- A comment containing <plugin> must not affect XML context. -->\n'
            '<dependencies>\n' + DEPENDENCY + '\n</dependencies>\n'
            '<build><plugins>\n' + plugin + '\n</plugins></build>\n'
        )
        result = self.clean(content)
        root = elementTree.fromstring(result)
        self.assertIsNone(root.find('maven:dependencies/maven:dependency/maven:version', NAMESPACE))
        self.assertEqual(
            root.findtext('maven:build/maven:plugins/maven:plugin/maven:dependencies/'
                          'maven:dependency/maven:version', namespaces=NAMESPACE),
            '6.2.18'
        )
        self.assertIn(plugin, result)
        self.assertIn('<!-- A comment containing <plugin> must not affect XML context. -->', result)

    def test_preserves_namespaced_plugins(self):
        plugin = COMPILER_PLUGIN
        for tag in ['plugin', 'groupId', 'artifactId', 'version']:
            plugin = plugin.replace('<' + tag + '>', '<m:' + tag + '>').replace('</' + tag + '>', '</m:' + tag + '>')
        content = '<m:project xmlns:m="http://maven.apache.org/POM/4.0.0"><m:build><m:plugins>\n' + plugin
        content += '\n</m:plugins></m:build></m:project>\n'
        self.assertEqual(self.clean(content), content)

    def test_keeps_ignored_unmanaged_and_unmarked_dependency_versions(self):
        preserved = """<dependency>
  <groupId>com.github.tomakehurst</groupId>
  <artifactId>wiremock-jre8</artifactId>
  <version>2.35.0</version> <!-- {x-version-update;com.github.tomakehurst:wiremock-jre8;external_dependency} -->
</dependency>
<dependency>
  <groupId>com.example</groupId>
  <artifactId>unmanaged</artifactId>
  <version>1.0.0</version> <!-- {x-version-update;com.example:unmanaged;external_dependency} -->
</dependency>
<dependency>
  <groupId>com.example</groupId>
  <artifactId>unmarked</artifactId>
  <version>1.0.0</version>
</dependency>"""
        content = project('<dependencies>\n' + DEPENDENCY + '\n' + preserved + '\n</dependencies>\n')
        result = self.clean(content, cleanup.IGNORED_ARTIFACTS)
        self.assertIn(preserved, result)
        dependency = elementTree.fromstring(result).find('maven:dependencies/maven:dependency', NAMESPACE)
        self.assertIsNone(dependency.find('maven:version', NAMESPACE))

    def test_keeps_starter_parent_version_replacement(self):
        content = project("""<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.5.14</version> <!-- {x-version-update;springboot3_org.springframework.boot:spring-boot-starter-parent;external_dependency} -->
</parent>
<build><plugins>
""" + COMPILER_PLUGIN + '\n</plugins></build>\n')
        self.pom.write_text(content, encoding='utf-8')
        update_spring_boot_starter_parent_for_file(str(self.pom), '3.5.13')
        cleanup.delete_dependency_version(str(self.pom), set(), MANAGED_ARTIFACTS)
        result = self.pom.read_text(encoding='utf-8')
        root = elementTree.fromstring(result)
        versions = root.findall('maven:parent/maven:version', NAMESPACE)
        self.assertEqual([version.text for version in versions], ['3.5.13'])
        self.assertIn(COMPILER_PLUGIN, result)

    def test_preserves_plugins_in_both_cleanup_workflows(self):
        module = Path(self.directory.name) / 'spring-cloud-azure-integration-tests'
        module.mkdir()
        pom = module / 'pom.xml'
        content = project('<dependencies>\n' + DEPENDENCY + '\n</dependencies>\n'
                          '<build><plugins>\n' + COMPILER_PLUGIN + '\n</plugins></build>\n')
        for usage in ['compatibility', 'integration']:
            with self.subTest(usage=usage), patch.object(
                    cleanup, 'external_dependencies_managed_list', return_value=MANAGED_ARTIFACTS):
                pom.write_text(content, encoding='utf-8')
                if usage == 'compatibility':
                    cleanup.find_all_poms_do_version_control_for_compatibility(
                        str(module), '3.5.14', 'springboot3_', set())
                else:
                    cleanup.find_all_poms_do_version_control_for_integration(str(module), 'springboot3_', set())
                result = pom.read_text(encoding='utf-8')
                self.assertIn(COMPILER_PLUGIN, result)
                self.assertIsNone(elementTree.fromstring(result).find(
                    'maven:dependencies/maven:dependency/maven:version', NAMESPACE))


if __name__ == '__main__':
    unittest.main()
