# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.4 or higher is required to run this script.

# Use case: Creates an aggregate POM which contains all modules that will be required in a "From Source" run for the passed
# project list.
#
# Flags
#   --project-list/--pl: List of project included in the From Source run.
#
# For example: To create an aggregate POM for Azure Storage
#    python eng/scripts/generate_from_source_pom.py --pl com.azure:azure-storage-blob,com.azure:azure-storage-common,...
#
# For example: To create an aggregate POM for Azure Core
#    python eng/scripts/generate_from_source_pom.py --pl com.azure:azure-core,com.azure:azure-core-amqp,com.azure:azure-core-test,...
#
# The script must be run at the root of azure-sdk-for-java.

import argparse
from datetime import timedelta
import os
import time
import json
import xml.etree.ElementTree as ET

# Only azure-client-sdk-parent and spring-boot-starter-parent are valid parent POMs for Track 2 libraries.
valid_parents = ['azure-client-sdk-parent', 'spring-boot-starter-parent', 'azure-spring-boot-test-parent']

# From this file get to the root path of the repo.
root_path = os.path.normpath(os.path.abspath(__file__) + '/../../../')

# From the root of the repo get to the version_client.txt file in eng/versioning.
client_versions_path = os.path.normpath(root_path + '/eng/versioning/version_client.txt')

# File path where the aggregate POM will be written.
client_from_source_pom_path = os.path.join(root_path, 'ClientFromSourcePom.xml')

# Beginning XML for the aggregate POM.
pom_file_start = '''<!-- Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License. -->
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.azure</groupId>
  <artifactId>azure-sdk-from-source</artifactId>
  <packaging>pom</packaging>
  <version>1.0.0</version>
  <modules>
'''

# Closing XML for the aggregate POM.
pom_file_end = '''  </modules>
</project>
'''

maven_xml_namespace = '{http://maven.apache.org/POM/4.0.0}'

# Function that creates the aggregate POM.
def create_from_source_pom(project_list: str, set_pipeline_variable: str):
    project_list_identifiers = project_list.split(',')

    # Get the artifact identifiers from client_versions.txt to act as our source of truth.
    artifact_identifier_to_source_version = load_client_artifact_identifiers()

    project_dependencies_mapping, dependency_to_project_mapping, project_to_pom_path_mapping = create_dependency_and_path_mappings(project_list_identifiers, artifact_identifier_to_source_version)

    dependent_modules = []

    # Resolve all projects, including transitively, that are dependent on the projects in the project list.
    for project_identifier in project_list_identifiers:
        if not project_identifier in project_to_pom_path_mapping:
            continue

        dependent_modules = resolve_dependent_project(project_identifier, dependent_modules, dependency_to_project_mapping)

    # Distinct the dependent modules, even though this should be guarded, to reduce downstream processing requirements.
    dependent_modules = list(set(dependent_modules))

    # Sort for making debugging easier (since it is cheap)
    dependent_modules.sort()

    dependency_modules = []

    # Resolve all dependencies of the projects in the project list and of the dependent modules.
    for project_identifier in project_list_identifiers + dependent_modules:
        dependency_modules = resolve_project_dependencies(project_identifier, dependency_modules, project_dependencies_mapping)

    modules = []
    # Finally map the project identifiers to relative module paths.
    for project_identifier in project_list_identifiers + dependent_modules + dependency_modules:
        if project_identifier in project_to_pom_path_mapping:
            modules.append(project_to_pom_path_mapping[project_identifier])

    # Distinct the modules list.
    modules = list(set(modules))

    # Sort the modules list for easier reading.
    modules.sort()

    with open(file=client_from_source_pom_path, mode='w') as fromSourcePom:
        fromSourcePom.write(pom_file_start)

        for module in modules:
            fromSourcePom.write('    <module>{}</module>\n'.format(module))

        fromSourcePom.write(pom_file_end)

    if set_pipeline_variable:
        print('##vso[task.setvariable variable={};]{}'.format(set_pipeline_variable, json.dumps(modules)))

# Function that loads and parses client_versions.txt into a artifact identifier - source version mapping.
def load_client_artifact_identifiers():
    artifact_identifiers = {}
    with open(file=client_versions_path, mode='r') as f:
        for line in f:
            stripped_line = line.strip()

            # Skip empty, comment, and non-standard version lines.
            if not stripped_line or stripped_line.startswith('#') or line.startswith('beta_') or line.startswith('unreleased_'):
                continue

            # Split the version line on ';' which should create 3 substrings of artifact identifier - released version - source version.
            splitVersionLine = stripped_line.split(";")

            # From the split lines create the artifact identifier - source version map entry.
            artifact_identifiers[splitVersionLine[0]]=splitVersionLine[2]

    return artifact_identifiers

# Function which creates project dependencies mapping, dependencies to dependent projects mapping, and project to module relative path mapping.
def create_dependency_and_path_mappings(project_list_identifiers: list, artifact_identifier_to_source_version: dict):
    project_dependencies_mapping = {}
    dependency_mapping = {}
    module_path_mapping = {}

    for root, _, files in os.walk(root_path):
        for file_name in files:
            file_path = root + os.sep + file_name

            # Only parse files that are pom.xml files.
            if (file_name.startswith('pom') and file_name.endswith('.xml')):
                add_project_to_dependency_and_module_mappings(file_path, project_dependencies_mapping,
                                                              project_list_identifiers,
                                                              artifact_identifier_to_source_version, dependency_mapping,
                                                              module_path_mapping)

    return project_dependencies_mapping, dependency_mapping, module_path_mapping

# Function that constructs the project dependencies map and adds to dependency to project map and project to module relative path map for a track 2 project.
def add_project_to_dependency_and_module_mappings(file_path: str, project_dependencies_mapping: dict,
                                                  project_list_identifiers: list,
                                                  artifact_identifier_to_source_version: dict,
                                                  dependency_mapping: dict, module_path_mapping: dict):
    if 'eng' in file_path.split(os.sep):
        return

    tree = ET.parse(file_path)
    tree_root = tree.getroot()

    project_identifier = create_artifact_identifier(tree_root)

    # If the project isn't a track 2 POM skip it and not one of the project list identifiers.
    if not project_identifier in project_list_identifiers and not is_spring_pom(tree_root) and not is_track_two_pom(tree_root): # Spring pom's parent can be empty.
        return

    module_path_mapping[project_identifier] = os.path.dirname(file_path).replace(root_path, '').replace('\\', '/')

    dependencies = {child:parent for parent in tree_root.iter() for child in parent if child.tag == maven_xml_namespace + 'dependency'}

    # If the project doesn't have a dependencies XML element skip it.
    if dependencies is None:
        return

    if not project_identifier in project_dependencies_mapping:
        project_dependencies_mapping[project_identifier] = []

    for dependency in dependencies:

        # not all the <dependency> are maven dependencies, ignore them 
        if dependencies[dependency].tag == maven_xml_namespace + 'dependenciesToScan':
            continue

        dependency_identifier = create_artifact_identifier(dependency)
        if not dependency_identifier in artifact_identifier_to_source_version:
            continue

        dependency_version = get_dependency_version(dependency)

        if dependency_version != artifact_identifier_to_source_version[dependency_identifier]:
            continue

        if not dependency_identifier in dependency_mapping:
            dependency_mapping[dependency_identifier] = []

        project_dependencies_mapping[project_identifier].append(dependency_identifier)
        dependency_mapping[dependency_identifier].append(project_identifier)

# Function which resolves the dependent projects of the project.
def resolve_dependent_project(pom_identifier: str, dependent_modules: list, dependency_to_project_mapping: dict):
    if pom_identifier in dependency_to_project_mapping:
        for dependency in dependency_to_project_mapping[pom_identifier]:
            # Only continue if the project's dependents haven't already been resolved.
            if not dependency in dependent_modules:
                dependent_modules.append(dependency)
                dependent_modules = resolve_dependent_project(dependency, dependent_modules, dependency_to_project_mapping)

    return dependent_modules

# Function which resolves the dependencies of the project.
def resolve_project_dependencies(pom_identifier: str, dependency_modules: list, project_dependencies_mapping: dict):
    if pom_identifier in project_dependencies_mapping:
        for dependency in project_dependencies_mapping[pom_identifier]:
            # Only continue if the project's dependencies haven't already been resolved.
            if not dependency in dependency_modules:
                dependency_modules.append(dependency)
                dependency_modules = resolve_project_dependencies(dependency, dependency_modules, project_dependencies_mapping)

    return dependency_modules

# Determines if the passed POM XML is a Spring library.
def is_spring_pom(tree_root: ET.Element):
    parent_node = element_find(tree_root, 'groupId')
    return not parent_node is None and element_find(parent_node, 'groupId').text == 'com.azure.spring'

# Determines if the passed POM XML is a track 2 library.
def is_track_two_pom(tree_root: ET.Element):
    parent_node = element_find(tree_root, 'parent')
    return not parent_node is None and element_find(parent_node, 'artifactId').text in valid_parents

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

# Helper function for finding an XML element which handles adding the namespace.
def element_find(element: ET.Element, path: str):
    return element.find(maven_xml_namespace + path)

def main():
    parser = argparse.ArgumentParser(description='Generated an aggregate POM for a From Source run.')
    parser.add_argument('--project-list', '--pl', type=str)
    parser.add_argument('--set-pipeline-variable', type=str)
    args = parser.parse_args()
    if args.project_list == None:
        raise ValueError('Missing project list.')
    start_time = time.time()
    create_from_source_pom(args.project_list, args.set_pipeline_variable)
    elapsed_time = time.time() - start_time

    print('Effective From Source POM File')
    with open(file=client_from_source_pom_path, mode='r') as fromSourcePom:
        print(fromSourcePom.read())

    print('elapsed_time={}'.format(elapsed_time))
    print('Total time for replacement: {} seconds'.format(str(timedelta(seconds=elapsed_time))))

if __name__ == '__main__':
    main()
