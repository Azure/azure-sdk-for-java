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
from typing import Dict, Iterable, List, Set
import xml.etree.ElementTree as ET

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
    def __init__(self, dependency_version: str, current_version: str):
        self.dependency_version = dependency_version
        self.current_version = current_version

    def matches_version(self, version: str, match_any_version: bool = False):
        if match_any_version:
            return version == self.dependency_version or version == self.current_version

        return version == self.current_version

default_project = Project(None, None, None, None)

# azure-client-sdk-parent, azure-perf-test-parent, spring-boot-starter-parent, and azure-spring-boot-test-parent are
# valid parent POMs for Track 2 libraries.
valid_parents = ['com.azure:azure-client-sdk-parent', 'com.azure:azure-perf-test-parent', 'org.springframework.boot:spring-boot-starter-parent', 'com.azure.spring:azure-spring-boot-test-parent', 'com.azure.cosmos.spark:azure-cosmos-spark_3_2-12']

# List of parent POMs that should be retained as projects to create a full from source POM.
parent_pom_identifiers = ['com.azure:azure-sdk-parent', 'com.azure:azure-client-sdk-parent', 'com.azure:azure-perf-test-parent', 'com.azure.spring:azure-spring-boot-test-parent', 'com.azure.cosmos.spark:azure-cosmos-spark_3_2-12']

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
def create_from_source_pom(project_list: str, set_pipeline_variable: str, set_skip_linting_projects: str, match_any_version: bool):
    project_list_identifiers = project_list.split(',')

    # Get the artifact identifiers from client_versions.txt to act as our source of truth.
    artifact_identifier_to_version = load_client_artifact_identifiers()

    projects = create_projects(project_list_identifiers, artifact_identifier_to_version, match_any_version)

    dependent_modules: Set[str] = set()

    # Resolve all projects, including transitively, that are dependent on the projects in the project list.
    for project_identifier in project_list_identifiers:
        dependent_modules = resolve_dependent_project(project_identifier, dependent_modules, projects)

    dependency_modules: Set[str] = set()

    # Resolve all dependencies of the projects in the project list and of the dependent modules.
    for project_identifier in project_list_identifiers:
        dependency_modules = resolve_project_dependencies(project_identifier, dependency_modules, projects)
    for project_identifier in dependent_modules:
        dependency_modules = resolve_project_dependencies(project_identifier, dependency_modules, projects)

    source_projects: Set[Project] = set()

    # Finally map the project identifiers to projects.
    add_source_projects(source_projects, project_list_identifiers, projects)
    add_source_projects(source_projects, dependent_modules, projects)
    add_source_projects(source_projects, dependency_modules, projects)

    modules = sorted(list(set([p.module_path for p in source_projects])))
    with open(file=client_from_source_pom_path, mode='w') as fromSourcePom:
        fromSourcePom.write(pom_file_start)

        for module in modules:
            fromSourcePom.write('    <module>{}</module>\n'.format(module))

        fromSourcePom.write(pom_file_end)

    if set_pipeline_variable:
        # The directory_path is too granular. There are build rules for some libraries that
        # create empty sources/javadocs jars using the README.md. Not every library
        # has a README.md and, in these cases, it uses the README.md from the root service
        # directory. This will also trim the number of paths down considerably.
        service_directories: Set[str] = set()
        for p in source_projects:
            # get the service directory, which is one level up from the library's directory
            service_directory = '/'.join(p.directory_path.split('/')[0:-1])
            service_directories.add(service_directory)

        checkout_paths = list(sorted(service_directories))
        print('setting env variable {} = {}'.format(set_pipeline_variable, checkout_paths))
        print('##vso[task.setvariable variable={};]{}'.format(set_pipeline_variable, json.dumps(checkout_paths)))

    # Sets the DevOps variable that is used to skip certain projects during linting validation.
    if set_skip_linting_projects:
        skip_linting_projects = []
        for maven_identifier in sorted([p.identifier for p in source_projects]):
            if not project_uses_client_parent(projects.get(maven_identifier), projects):
                skip_linting_projects.append('!' + maven_identifier)
        print('setting env variable {} = {}'.format(set_skip_linting_projects, skip_linting_projects))
        print('##vso[task.setvariable variable={};]{}'.format(set_skip_linting_projects, ','.join(list(set(skip_linting_projects)))))


# Function that loads and parses client_versions.txt into a artifact identifier - source version mapping.
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

            # From the split lines create the artifact identifier - source version map entry.
            artifact_identifiers[splitVersionLine[0]] = ArtifactVersion(splitVersionLine[1], splitVersionLine[2])

    return artifact_identifiers

# Function that creates the Projects within the repository.
# Projects contain a Maven identifier, module path, parent POM, its dependency Maven identifiers, and Maven identifiers for projects dependent on it.
def create_projects(project_list_identifiers: list, artifact_identifier_to_version: Dict[str, ArtifactVersion], match_any_version: bool) -> Dict[str, Project]:
    projects: Dict[str, Project] = {}

    for root, _, files in os.walk(root_path):
        # Ignore sdk/resourcemanagerhybrid
        if 'resourcemanagerhybrid' in root:
            continue

        for file_name in files:
            file_path = root + os.sep + file_name

            # Only parse files that are pom.xml files.
            if (file_name.startswith('pom') and file_name.endswith('.xml')):
                project = create_project_for_pom(file_path, project_list_identifiers, artifact_identifier_to_version, match_any_version)
                if project is not None:
                    projects[project.identifier] = project


    # Once all the projects have been loaded inject their dependents by inversion the dependencies.
    for _, project in projects.items():
        for dependency in project.dependencies:
            projects.get(dependency).add_dependent(project.identifier)

    return projects

def create_project_for_pom(pom_path: str, project_list_identifiers: list, artifact_identifier_to_version: Dict[str, ArtifactVersion], match_any_version: bool):
    if 'eng' in pom_path.split(os.sep):
        return

    tree = ET.parse(pom_path)
    tree_root = tree.getroot()

    project_identifier = create_artifact_identifier(tree_root)
    module_path = pom_path.replace(root_path, '').replace('\\', '/')
    directory_path = module_path[:module_path.rindex('/')]
    parent_pom = get_parent_pom(tree_root)

    # If this is one of the parent POMs, retain it as a project.
    if project_identifier in parent_pom_identifiers:
        return Project(project_identifier, directory_path, module_path, parent_pom)

    # If the project isn't a track 2 POM skip it and not one of the project list identifiers.
    if not project_identifier in project_list_identifiers and not is_spring_child_pom(tree_root) and not parent_pom in valid_parents: # Spring pom's parent can be empty.
        return

    project = Project(project_identifier, directory_path, module_path, parent_pom)

    dependencies = {child:parent for parent in tree_root.iter() for child in parent if child.tag == maven_xml_namespace + 'dependency'}

    for dependency in dependencies:
        # not all the <dependency> are maven dependencies, ignore them
        if dependencies[dependency].tag == maven_xml_namespace + 'dependenciesToScan':
            continue

        dependency_identifier = create_artifact_identifier(dependency)
        if not dependency_identifier in artifact_identifier_to_version:
            continue

        dependency_version = get_dependency_version(dependency)

        if not artifact_identifier_to_version[dependency_identifier].matches_version(dependency_version, match_any_version):
            continue

        project.add_dependency(dependency_identifier)

    return project

# Function which resolves the dependent projects of the project.
def resolve_dependent_project(pom_identifier: str, dependent_modules: Set[str], projects: Dict[str, Project]):
    if pom_identifier in projects:
        for dependency in projects[pom_identifier].dependents:
            # Only continue if the project's dependents haven't already been resolved.
            if not dependency in dependent_modules:
                dependent_modules.add(dependency)
                dependent_modules = resolve_dependent_project(dependency, dependent_modules, projects)

    return dependent_modules

# Function which resolves the dependencies of the project.
def resolve_project_dependencies(pom_identifier: str, dependency_modules: Set[str], projects: Dict[str, Project]):
    if pom_identifier in projects:
        for dependency in projects[pom_identifier].dependencies:
            # Only continue if the project's dependencies haven't already been resolved.
            if not dependency in dependency_modules:
                dependency_modules.add(dependency)
                dependency_modules = resolve_project_dependencies(dependency, dependency_modules, projects)

    return dependency_modules

# Get parent POM.
def get_parent_pom(tree_root: ET.Element):
    parent_node = element_find(tree_root, 'parent')

    if parent_node is None:
        return None

    return create_artifact_identifier(parent_node)

# Determines if the passed POM XML is a Spring library.
def is_spring_child_pom(tree_root: ET.Element):
    group_id_node = element_find(tree_root, 'groupId')
    artifact_id_node = element_find(tree_root, 'artifactId')
    return not group_id_node is None and group_id_node.text == 'com.azure.spring' \
           and not artifact_id_node is None \
           and artifact_id_node.text != 'spring-cloud-azure' \
           and artifact_id_node.text != 'spring-cloud-azure-experimental' # Exclude parent pom to fix this error: "Project is duplicated in the reactor"

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

def add_source_projects(source_projects: Set[Project], project_identifiers: Iterable[str], projects: Dict[str, Project]):
    for project_identifier in project_identifiers:
        project = projects[project_identifier]
        source_projects.add(project)

        while project.parent_pom is not None:
            project = projects.get(project.parent_pom, default_project)
            if project.module_path is not None:
                source_projects.add(project)

def project_uses_client_parent(project: Project, projects: Dict[str, Project]) -> bool:
    while project.parent_pom is not None:
        if project.parent_pom == 'com.azure:azure-client-sdk-parent':
            return True
        project = projects.get(project.parent_pom, default_project)

    return False

def main():
    parser = argparse.ArgumentParser(description='Generated an aggregate POM for a From Source run.')
    parser.add_argument('--project-list', '--pl', type=str)
    parser.add_argument('--set-pipeline-variable', type=str)
    parser.add_argument('--set-skip-linting-projects', type=str)
    parser.add_argument('--match-any-version', action='store_true')
    args = parser.parse_args()
    if args.project_list == None:
        raise ValueError('Missing project list.')
    start_time = time.time()
    create_from_source_pom(args.project_list, args.set_pipeline_variable, args.set_skip_linting_projects, args.match_any_version)
    elapsed_time = time.time() - start_time

    print('Effective From Source POM File')
    with open(file=client_from_source_pom_path, mode='r') as fromSourcePom:
        print(fromSourcePom.read())

    print('elapsed_time={}'.format(elapsed_time))
    print('Total time for replacement: {} seconds'.format(str(timedelta(seconds=elapsed_time))))

if __name__ == '__main__':
    main()