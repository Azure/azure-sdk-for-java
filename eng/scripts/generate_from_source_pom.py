# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.4 or higher is required to run this script.

# Use case: Update all the versions in README.md and pom.xml files based on
# the versions in versions_[client|data|management].txt, external_dependencies.txt
#
# It's worth noting that there are 3 update types, library, external_dependencies and all. 'All' means update both the libraries
# for the track selected as well as the external_dependencies.
#
#    python eng/versioning/update_versions.py --update-type [library|external_dependency|all] --build-type [client|data|management]
# For example: To update the library versions for the client track without touching the README files
#    python eng/versioning/update_versions.py --ut library --bt client --sr
#
# Use case: Update the versions in a particular file
#
#    python utilities/update_versions.py --update-type [library|external_dependency|all] -build-type [client|data|management] --target-file pom-file-to-update
# For example: To update all versions for the client track for a given pom file
#    python eng/versioning/update_versions.py --ut all --bt client --tf <pathToPomFile>\pom.xml
#
# Use case: Update the external_dependencies
#
#    python utilities/update_versions.py --update-type [library|external_dependency|all] -build-type [client|data|management] --target-file pom-file-to-update
# For example: To update all versions for the client track for a given pom file. While the skip readme flag isn't entirely
# necessary here, since our README.md files don't contain externaly dependency versions, there's no point in scanning files
# that shouldn't require changes.
#    python eng/versioning/update_versions.py --ut external_dependency --sr
#
# The script must be run at the root of azure-sdk-for-java.

import argparse
from datetime import timedelta
import os
import time
import xml.etree.ElementTree as ET

# Only azure-client-sdk-parent and spring-boot-starter-parent are valid parent POMs for Track 2 libraries.
valid_parents = {"azure-client-sdk-parent": True, "spring-boot-starter-parent": True}
root_path = os.path.normpath(os.path.abspath(__file__) + '/../../../')
client_versions_path = os.path.normpath(root_path + '/eng/versioning/version_client.txt')
client_from_source_pom_path = os.path.join(root_path, 'ClientFromSourcePom.xml')

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

pom_file_end = '''  </modules>
</project>
'''

def create_from_source_pom(service_directory: str):
    client_service_directory = os.path.normpath(root_path + "/sdk/" + service_directory)
    artifact_identifier_to_source_version = load_client_artifact_identifiers()
    dependency_to_project_mapping, project_to_pom_path_mapping = create_dependency_and_path_mappings(artifact_identifier_to_source_version)

    modules = []
    for root, _, files in os.walk(client_service_directory):
        for file_name in files:
            file_path = root + os.sep + file_name
            if (file_name.startswith('pom') and file_name.endswith('.xml')):
                modules = add_modules_to_pom(file_path, modules, dependency_to_project_mapping, project_to_pom_path_mapping)

    with open(file=client_from_source_pom_path, mode='w') as fromSourcePom:
        fromSourcePom.write(pom_file_start)

        for module in modules:
            fromSourcePom.write('    <module>{}</module>\n'.format(module))

        fromSourcePom.write(pom_file_end)
    

def load_client_artifact_identifiers():
    artifact_identifiers = {}
    with open(file=client_versions_path, mode='r') as f:
        for line in f:
            stripped_line = line.strip()
            if not stripped_line or stripped_line.startswith('#') or line.startswith('beta_') or line.startswith('unreleased_'):
                continue
            splitVersionLine = stripped_line.split(";")
            artifact_identifiers[splitVersionLine[0]]=splitVersionLine[2]

    return artifact_identifiers

def create_dependency_and_path_mappings(artifact_identifier_to_source_version: dict):
    dependency_mapping = {}
    module_path_mapping = {}

    for root, _, files in os.walk(root_path):
        for file_name in files:
            file_path = root + os.sep + file_name
            if (file_name.startswith('pom') and file_name.endswith('.xml')):
                add_project_to_dependency_and_module_mappings(file_path, artifact_identifier_to_source_version, dependency_mapping, module_path_mapping)

    return dependency_mapping, module_path_mapping

def add_project_to_dependency_and_module_mappings(file_path: str, artifact_identifier_to_source_version: dict, dependency_mapping: dict, module_path_mapping: dict):
    if 'eng' in file_path.split(os.sep):
        return

    tree = ET.parse(file_path)
    tree_root = tree.getroot()

    if not is_track_two_pom(tree_root):
        return

    project_identifier = create_artifact_identifier(tree_root)
    module_path_mapping[project_identifier] = os.path.dirname(file_path).replace(root_path, '').replace('\\', '/')

    dependencies = element_find(tree_root, 'dependencies')
    if dependencies is None:
        return

    for dependency in dependencies.getchildren():
        dependency_identifier = create_artifact_identifier(dependency)
        if not dependency_identifier in artifact_identifier_to_source_version:
            continue

        if not dependency_identifier in dependency_mapping:
            dependency_mapping[dependency_identifier] = []

        dependency_mapping[dependency_identifier].append(project_identifier)

def add_modules_to_pom(pom_path: str, modules: list, dependency_to_project_mapping: dict, project_to_pom_path_mapping: dict):
    tree = ET.parse(pom_path)
    tree_root = tree.getroot()

    if not is_track_two_pom(tree_root):
        return modules
    
    pom_identifier = create_artifact_identifier(tree_root)
    if project_to_pom_path_mapping[pom_identifier] in modules:
        return modules

    modules.append(project_to_pom_path_mapping[pom_identifier])
        
    if not pom_identifier in dependency_to_project_mapping:
        return modules

    for dependency in dependency_to_project_mapping[pom_identifier]:
        if not project_to_pom_path_mapping[dependency] in modules:
            modules.append(project_to_pom_path_mapping[dependency])
            modules = add_modules_to_pom(os.path.normpath(root_path + project_to_pom_path_mapping[dependency] + '/pom.xml'), modules, dependency_to_project_mapping, project_to_pom_path_mapping)

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
    if args.service_directory == None:
        print('Missing service directory.')
        #raise ValueError('Missing service directory.')
    start_time = time.time()
    create_from_source_pom('storage')
    elapsed_time = time.time() - start_time
    print('elapsed_time={}'.format(elapsed_time))
    print('Total time for replacement: {}'.format(str(timedelta(seconds=elapsed_time))))

if __name__ == '__main__':
    main()
