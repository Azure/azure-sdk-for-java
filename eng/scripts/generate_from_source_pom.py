# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.4 or higher is required to run this script.

# Use case: Creates an aggregate POM which contains all modules that will be required in a "From Source" run for the passed service directory.
#
# Flags
#   --service-directory/--sd: Short-hand name of an Azure SDK service group, such as 'core' or 'storage'. 
#                             This must be a valid directory when used in the form '/sdk/{value}/'.
#
# For example: To create an aggregate POM for Azure Storage
#    python eng/scripts/generate_from_source_pom.py --sd storage
#
# For example: To create an aggregate POM for Azure Core
#    python eng/scripts/generate_from_source_pom.py --sd core
#
# The script must be run at the root of azure-sdk-for-java.

import argparse
from datetime import timedelta
import os
import time
import xml.etree.ElementTree as ET

# Only azure-client-sdk-parent and spring-boot-starter-parent are valid parent POMs for Track 2 libraries.
valid_parents = {"azure-client-sdk-parent": True, "spring-boot-starter-parent": True}

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
  <version>1.0.0</version> <!-- Need not change for every release-->
  <modules>
'''

# Closing XML for the aggregate POM.
pom_file_end = '''  </modules>
</project>
'''

# Function that creates the aggregate POM.
def create_from_source_pom(service_directory: str):
    # First the service directory needs to be determined.
    client_service_directory = os.path.normpath(root_path + '/sdk/' + service_directory)

    if not os.path.isdir(client_service_directory):
        raise NotADirectoryError('{} is not a valid service directory.'.format(client_service_directory))

    # Get the artifact identifiers from client_versions.txt to act as our source of truth.
    artifact_identifier_to_source_version = load_client_artifact_identifiers()


    project_dependencies_mapping, dependency_to_project_mapping, project_to_pom_path_mapping = create_dependency_and_path_mappings(artifact_identifier_to_source_version)

    modules = []
    for root, _, files in os.walk(client_service_directory):
        for file_name in files:
            file_path = root + os.sep + file_name
            if (file_name.startswith('pom') and file_name.endswith('.xml')):
                modules = add_modules_to_pom(file_path, modules, project_dependencies_mapping, dependency_to_project_mapping, project_to_pom_path_mapping)

    modules.sort()
    
    with open(file=client_from_source_pom_path, mode='w') as fromSourcePom:
        fromSourcePom.write(pom_file_start)

        for module in modules:
            fromSourcePom.write('    <module>{}</module>\n'.format(module))

        fromSourcePom.write(pom_file_end)
    
# Function that loads and parses client_versions.txt into a artifact identifier - source version mapping.
def load_client_artifact_identifiers():
    artifact_identifiers = {}
    with open(file=client_versions_path, mode='r') as f:
        for line in f:
            stripped_line = line.strip()
            
            # Skip empty lines, comments, and non-standard version lines.
            if not stripped_line or stripped_line.startswith('#') or line.startswith('beta_') or line.startswith('unreleased_'):
                continue

            # Split the version line on ';' which should create 3 substrings of artifact identifier - released version - source version.
            splitVersionLine = stripped_line.split(";")

            # From the split lines create the artifact identitifer - source version map entry.
            artifact_identifiers[splitVersionLine[0]]=splitVersionLine[2]

    return artifact_identifiers

# Function which creates project dependencies mapping, dependencies to dependent projects mapping, and project to module relative path mapping.
def create_dependency_and_path_mappings(artifact_identifier_to_source_version: dict):
    project_dependencies_mapping = {}
    dependency_mapping = {}
    module_path_mapping = {}

    for root, _, files in os.walk(root_path):
        for file_name in files:
            file_path = root + os.sep + file_name

            # Only parse files that are pom.xml files.
            if (file_name.startswith('pom') and file_name.endswith('.xml')):
                add_project_to_dependency_and_module_mappings(file_path, project_dependencies_mapping, artifact_identifier_to_source_version, dependency_mapping, module_path_mapping)

    return project_dependencies_mapping, dependency_mapping, module_path_mapping

# Function that constructs the project dependencies map and adds to dependency to project map and project to module relative path map for a track 2 project. 
def add_project_to_dependency_and_module_mappings(file_path: str, project_dependencies_mapping: dict, artifact_identifier_to_source_version: dict, dependency_mapping: dict, module_path_mapping: dict):
    if 'eng' in file_path.split(os.sep):
        return

    tree = ET.parse(file_path)
    tree_root = tree.getroot()

    # If the project isn't a track 2 POM skip it.
    if not is_track_two_pom(tree_root):
        return

    project_identifier = create_artifact_identifier(tree_root)
    module_path_mapping[project_identifier] = os.path.dirname(file_path).replace(root_path, '').replace('\\', '/')

    dependencies = element_find(tree_root, 'dependencies')

    # If the project doesn't have a dependencies XML element skip it.
    if dependencies is None:
        return

    if not project_identifier in project_dependencies_mapping:
        project_dependencies_mapping[project_identifier] = []

    for dependency in dependencies:
        dependency_identifier = create_artifact_identifier(dependency)
        if not dependency_identifier in artifact_identifier_to_source_version:
            continue

        if not dependency_identifier in dependency_mapping:
            dependency_mapping[dependency_identifier] = []

        project_dependencies_mapping[project_identifier].append(dependency_identifier)
        dependency_mapping[dependency_identifier].append(project_identifier)

# Function which finds and adds all dependencies required for a project in the service directory.
def add_modules_to_pom(pom_path: str, modules: list, project_dependencies_mapping: dict, dependency_to_project_mapping: dict, project_to_pom_path_mapping: dict):
    tree = ET.parse(pom_path)
    tree_root = tree.getroot()

    # If the project isn't a track 2 POM skip it.
    if not is_track_two_pom(tree_root):
        return modules
    
    pom_identifier = create_artifact_identifier(tree_root)

    # If the project has already been included skip it.
    if project_to_pom_path_mapping[pom_identifier] in modules:
        return modules

    modules.append(project_to_pom_path_mapping[pom_identifier])
    
    # Add all project relative paths that require this library as a dependency.
    if pom_identifier in dependency_to_project_mapping:
        for dependency in dependency_to_project_mapping[pom_identifier]:
            if not project_to_pom_path_mapping[dependency] in modules:
                modules = add_modules_to_pom(os.path.normpath(root_path + project_to_pom_path_mapping[dependency] + '/pom.xml'), modules, project_dependencies_mapping, dependency_to_project_mapping, project_to_pom_path_mapping)
                modules.append(project_to_pom_path_mapping[dependency])

    # Add all dependencies of this library.
    if pom_identifier in project_dependencies_mapping: # This should be guaranteed based on earlier code but check anyway
        for dependency in project_dependencies_mapping[pom_identifier]:
            if not project_to_pom_path_mapping[dependency] in modules:
                modules.append(project_to_pom_path_mapping[dependency])

    return modules

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

# Helper function for finding an XML element which handles adding the namespace.
def element_find(element: ET.Element, path: str):
    return element.find('{http://maven.apache.org/POM/4.0.0}' + path)

def main():
    parser = argparse.ArgumentParser(description='Replace version numbers in poms and READMEs.')
    parser.add_argument('--service-directory', '--sd', type=str)
    args = parser.parse_args()
    #if args.service_directory == None:
        #raise ValueError('Missing service directory.')
    start_time = time.time()
    create_from_source_pom('appconfiguration') #args.service_directory)
    elapsed_time = time.time() - start_time

    print('Effective From Source POM File')
    with open(file=client_from_source_pom_path, mode='r') as fromSourcePom:
        print(fromSourcePom.read())

    print('elapsed_time={}'.format(elapsed_time))
    print('Total time for replacement: {}'.format(str(timedelta(seconds=elapsed_time))))

if __name__ == '__main__':
    main()
