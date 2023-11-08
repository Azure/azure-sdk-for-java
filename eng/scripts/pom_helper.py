# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

import xml.etree.ElementTree as ET
from typing import List

# Beginning XML for the aggregate POM.
pom_file_start = '''<!-- Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License. -->
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.azure</groupId>
  <artifactId>{}</artifactId>
  <packaging>pom</packaging>
  <version>1.0.0</version>
'''

# Closing XML for the aggregate POM.
pom_file_end = '''
</project>
'''

start_modules = '''
  <modules>
'''

end_modules = '  </modules>'
start_dependencies = '''
  <dependencies>
'''
end_dependencies = '  </dependencies>'

maven_xml_namespace = '{http://maven.apache.org/POM/4.0.0}'

dependency_template = '''
        <dependency>
          <groupId>{}</groupId>
          <artifactId>{}</artifactId>
          <version>{}</version>
        </dependency>
'''

distribution_management = '''
    <distributionManagement>
        <snapshotRepository>
            <id>ossrh</id>
            <name>Sonatype Snapshots</name>
            <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
            <uniqueVersion>true</uniqueVersion>
            <layout>default</layout>
        </snapshotRepository>
        <site>
            <id>azure-java-build-docs</id>
            <url>$\{site.url\}/site/</url>
        </site>
    </distributionManagement>
'''

# Creates an artifacts identifier.
def create_artifact_identifier(element: ET.Element):
    group_id = element_find(element, 'groupId')

    # POMs allow the groupId to be inferred from the parent POM.
    # This is a guard to prevent this from raising an error.
    if group_id is None:
        group_id = element_find(element_find(element, 'parent'), 'groupId')

    return group_id.text + ':' + element_find(element, 'artifactId').text

# Gets the dependency version.
def get_dependency_version(element: ET.Element):
    dependency_version = element_find(element, 'version')

    if dependency_version is None:
        return None

    return dependency_version.text


# Get parent POM.
def get_parent_pom(tree_root: ET.Element):
    parent_node = element_find(tree_root, 'parent')

    if parent_node is None:
        return None

    return create_artifact_identifier(parent_node)


# Helper function for finding an XML element which handles adding the namespace.
def element_find(element: ET.Element, path: str):
    return element.find(maven_xml_namespace + path)


class Project:
    def __init__(self, identifier: str, directory_path: str, module_path: str, parent_pom: str):
        self.identifier = identifier
        self.directory_path = directory_path
        self.module_path = module_path
        self.parent_pom = parent_pom
        self.dependencies: List[str] = []
        self.dependents: List[str] = []

    def add_dependency(self, dependency: str):
        if dependency not in self.dependencies:
            self.dependencies.append(dependency)

    def add_dependent(self, dependent: str):
        if dependent not in self.dependents:
            self.dependents.append(dependent)


class ArtifactVersion:
    def __init__(self, group_id: str, artifact_id: str, dependency_version: str, current_version: str):
        self.dependency_version = dependency_version
        self.current_version = current_version
        self.group_id = group_id
        self.artifact_id = artifact_id

    def matches_version(self, version: str, match_any_version: bool = False):
        if match_any_version:
            return version == self.dependency_version or version == self.current_version

        return version == self.current_version
