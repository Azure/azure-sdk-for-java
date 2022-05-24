# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.4 or higher is required to run this script.

# Use case: Creates an aggregate POM which contains all modules for which aggregate code coverage will be reported.
#
# Flags
#   --project-list/--pl: List of projects included in the aggregate coverage report.
#
# For example: To create an aggregate POM for Azure Storage
#    python eng/scripts/generate_aggregate_coverage_pom.py --pl com.azure:azure-storage-blob,com.azure:azure-storage-common,...
#
# The script must be run at the root of azure-sdk-for-java.

import argparse
from datetime import timedelta
import os
import time
from typing import Dict
from pom_helper import *

# azure-client-sdk-parent valid parent POMs for Track 2 libraries.
valid_parents = ['com.azure:azure-client-sdk-parent']

# List of parent POMs that should be retained as projects to create POM.
parent_pom_identifiers = ['com.azure:azure-sdk-parent', 'com.azure:azure-client-sdk-parent']

include_groups = []

# From this file get to the root path of the repo.
root_path = os.path.normpath(os.path.abspath(__file__) + '/../../../')

# From the root of the repo get to the version_client.txt file in eng/versioning.
client_versions_path = os.path.normpath(root_path + '/eng/versioning/version_client.txt')

external_dependency_versions_path = os.path.normpath(root_path + '/eng/versioning/external_dependencies.txt')

# File path where the aggregate POM will be written.
client_aggregate_pom_path = os.path.join(root_path, 'aggregate-coverage-pom.xml')

jacoco_artifact_id = 'org.jacoco:jacoco-maven-plugin'

jacoco_build = '''
  <build>
    <plugins>
      <plugin>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>{}</version>
        <configuration>
          <outputDirectory>${{project.baseDir}}/target/site/test-coverage</outputDirectory>
          <excludes>
             <exclude>META-INF/**</exclude>
          </excludes>
        </configuration>
      </plugin>
    </plugins>
  </build>
'''


def create_aggregate_coverage_pom(project_list: str, groups: str):

    if groups is None:
        include_groups.append('com.azure')
        include_groups.append('com.azure.spring')
        include_groups.append('com.azure.resourcemanager')
    else:
        for group in groups.split(','):
            include_groups.append(group)

    # Get the artifact identifiers from client_versions.txt to act as our source of truth.
    artifact_identifier_to_version = load_client_artifact_identifiers()

    if project_list is None:
        project_list = ','.join(artifact_identifier_to_version.keys())

    project_list_identifiers = project_list.split(',')

    external_dependency_version = load_external_dependency_version()

    projects = create_projects(project_list_identifiers, artifact_identifier_to_version)

    with open(file=client_aggregate_pom_path, mode='w') as aggregateCoveragePom:
        aggregateCoveragePom.write(pom_file_start.format('azure-sdk-aggregate-coverage'))
        aggregateCoveragePom.write(start_modules)
        dependencies = ''
        for project_id in project_list_identifiers:
            if project_id not in projects:
                continue

            project = projects[project_id]
            aggregateCoveragePom.write('    <module>{}</module>\n'.format(project.module_path))

            dependency_id = artifact_identifier_to_version[project_id]
            dependencies += dependency_template.format(dependency_id.group_id, dependency_id.artifact_id, dependency_id.current_version)

        aggregateCoveragePom.write(end_modules)
        aggregateCoveragePom.write(start_dependencies)
        aggregateCoveragePom.write(dependencies)
        aggregateCoveragePom.write(end_dependencies)
        aggregateCoveragePom.write(jacoco_build.format(external_dependency_version[jacoco_artifact_id]))
        aggregateCoveragePom.write(pom_file_end)


# Function that creates the Projects within the repository.
# Projects contain a Maven identifier, module path, parent POM
def create_projects(project_list_identifiers: list, artifact_identifier_to_version: Dict[str, ArtifactVersion]) -> Dict[str, Project]:
    projects: Dict[str, Project] = {}

    for root, _, files in os.walk(root_path):

        for file_name in files:
            file_path = root + os.sep + file_name

            # Only parse files that are pom.xml files.
            if (file_name.startswith('pom') and file_name.endswith('.xml')):
                project = create_project_for_pom(file_path, project_list_identifiers)
                if project is not None:
                    projects[project.identifier] = project

    return projects


def create_project_for_pom(pom_path: str, project_list_identifiers: list):
    if 'eng' in pom_path.split(os.sep):
        return

    tree = ET.parse(pom_path)
    tree_root = tree.getroot()

    project_identifier = create_artifact_identifier(tree_root)
    module_path = pom_path.replace(root_path, '').replace('\\', '/')
    directory_path = module_path[:module_path.rindex('/')]
    parent_pom = get_parent_pom(tree_root)

    group_id = element_find(tree_root, 'groupId')

    # If the project isn't a track 2 POM skip it and not one of the project list identifiers.
    if not project_identifier in project_list_identifiers \
        or parent_pom not in valid_parents \
        or group_id.text not in include_groups:
        return

    project = Project(project_identifier, directory_path, module_path, parent_pom)
    return project


# Function that loads and parses client_versions.txt into an artifact identifier - source version mapping.
def load_client_artifact_identifiers() -> Dict[str, ArtifactVersion]:
    artifact_identifiers: Dict[str, str] = {}
    with open(file=client_versions_path, mode='r') as f:
        for line in f:
            stripped_line = line.strip()

            # Skip empty, comment, and non-standard version lines.
            if not stripped_line or stripped_line.startswith('#') or line.startswith('beta_') or line.startswith('unreleased_'):
                continue

            # Split the version line on ';' which should create 3 substrings of artifact identifier - released version - source version.
            splitVersionLine = stripped_line.split(";")


            group_artifact = splitVersionLine[0].split(":")
            # From the split lines create the artifact identifier - source version map entry.
            artifact_identifiers[splitVersionLine[0]] = ArtifactVersion(group_artifact[0], group_artifact[1], splitVersionLine[1], splitVersionLine[2])

    return artifact_identifiers


def load_external_dependency_version() -> Dict[str, str]:
    dependency_versions: Dict[str, str] = {}
    with open(file=external_dependency_versions_path, mode='r') as f:
        for line in f:
            stripped_line = line.strip()

            # Skip empty, comment, and non-standard version lines.
            if not stripped_line or stripped_line.startswith('#'):
                continue

            splitVersionLine = stripped_line.split(";")
            dependency_versions[splitVersionLine[0]] = splitVersionLine[1]

    return dependency_versions


def main():
    parser = argparse.ArgumentParser(description='Generated a POM for creating an aggregate code coverage report.')
    parser.add_argument('--project-list', '--pl', type=str)
    parser.add_argument('--group', '--g', type=str)
    args = parser.parse_args()
    start_time = time.time()
    create_aggregate_coverage_pom(args.project_list, args.group)
    elapsed_time = time.time() - start_time

    print('Effective POM File for aggregate code coverage')
    with open(file=client_aggregate_pom_path, mode='r') as aggregateCoveragePom:
        print(aggregateCoveragePom.read())

    print('elapsed_time={}'.format(elapsed_time))
    print('Total time for replacement: {} seconds'.format(str(timedelta(seconds=elapsed_time))))

if __name__ == '__main__':
    main()
