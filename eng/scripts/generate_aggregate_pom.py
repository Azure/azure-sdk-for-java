# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.4 or higher is required to run this script.

# Use case: Creates an aggregate POM which contains all modules for which aggregate JavaDoc or code coverage reports are generated. 
# Note: This script must be run from the root of the azure-sdk-for-java repository
#
# Flags
#   --project-list/--pl: List of projects included in the generated pom. If no projects are specified all projects defined in version_client.txt will be included
#   --groups/--g: List of comma-separate Maven groups to include in generated pom. If no groups are specified all track 2 groups (com.azure, com.azure.resourcemanager and com.azure.spring) will be included.
#
# For example: To create an aggregate POM for Azure Storage
#    python eng/scripts/generate_aggregate_coverage_pom.py --pl com.azure:azure-storage-blob,com.azure:azure-storage-common,...

import argparse
from datetime import timedelta
from io import TextIOWrapper
import os
import time
from typing import Dict
from pom_helper import *

# azure-client-sdk-parent valid parent POMs for Track 2 libraries.
valid_parents = ['com.azure:azure-client-sdk-parent']

# List of parent POMs that should be retained as projects to create POM.
parent_pom_identifiers = ['com.azure:azure-sdk-parent', 'com.azure:azure-client-sdk-parent', 'com.azure:azure-perf-test-parent']

include_groups = []

exclude_projects = []

# From this file get to the root path of the repo.
root_path = os.path.normpath(os.path.abspath(__file__) + '/../../../')

# From the root of the repo get to the version_client.txt file in eng/versioning.
client_versions_path = os.path.normpath(root_path + '/eng/versioning/version_client.txt')

external_dependency_versions_path = os.path.normpath(root_path + '/eng/versioning/external_dependencies.txt')

# File path where the aggregate POM will be written.
client_aggregate_pom_path = os.path.join(root_path, 'aggregate-pom.xml')

jacoco_artifact_id = 'org.jacoco:jacoco-maven-plugin'
javadoc_artifact_id = 'org.apache.maven.plugins:maven-javadoc-plugin'
indent_1 = ' ' * 24
indent_2 = ' ' * 28
indent_3 = ' ' * 32

jacoco_build = '''
  <build>
    <plugins>
      <plugin>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>{}</version>
        <configuration>
          <outputDirectory>target/site/test-coverage</outputDirectory>
          <excludes>
             <exclude>META-INF/**</exclude>
          </excludes>
        </configuration>
      </plugin>
    </plugins>
  </build>
'''


start_javadoc_build = '''
    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-javadoc-plugin</artifactId>
                    <version>{}</version>
                    <configuration>
                        <source>1.8</source>
                        <doctitle>Azure SDK for Java Reference Documentation</doctitle>
                        <windowtitle>Azure SDK for Java Reference Documentation</windowtitle>
                        <detectJavaApiLink>false</detectJavaApiLink>
                        <isOffline>true</isOffline>
                        <linksource>false</linksource>
                        <failOnError>true</failOnError>
                        <failOnWarnings>true</failOnWarnings>
                        <doclint>all</doclint>
                        <quiet>true</quiet>
'''

end_javadoc_build = '''
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
'''

def create_aggregate_pom(project_list: str, groups: str, exclude_project_list: str, type: str):

    if groups is None:
        include_groups.append('com.azure')
        include_groups.append('com.azure.spring')
        include_groups.append('com.azure.resourcemanager')
    else:
        for group in groups.split(','):
            include_groups.append(group)

    if exclude_project_list is not None:
        for exclude_project in exclude_project_list.split(','):
            exclude_projects.append(exclude_project)

    # Get the artifact identifiers from client_versions.txt to act as our source of truth.
    artifact_identifier_to_version = load_client_artifact_identifiers()

    if project_list is None:
        project_list = ','.join(artifact_identifier_to_version.keys())

    project_list_identifiers = project_list.split(',')

    external_dependency_version = load_external_dependency_version()

    projects = create_projects(project_list_identifiers, artifact_identifier_to_version)

    with open(file=client_aggregate_pom_path, mode='w') as aggregatePom:
        aggregatePom.write(pom_file_start.format('azure-sdk-aggregate-report'))
        aggregatePom.write(start_modules)
        dependencies = ''

        for project in sorted(projects.values(), key=lambda x: x.module_path):
            project_id = project.identifier
            if project_id not in project_list_identifiers:
                continue

            aggregatePom.write('    <module>{}</module>\n'.format(project.module_path.removeprefix("/").replace("/pom.xml", "")))

            dependency_id = artifact_identifier_to_version[project_id]
            dependencies += dependency_template.format(dependency_id.group_id, dependency_id.artifact_id, dependency_id.current_version)

        aggregatePom.write(end_modules)

        if type == 'coverage':
            aggregatePom.write(start_dependencies)
            aggregatePom.write(dependencies)
            aggregatePom.write(end_dependencies)
            aggregatePom.write(jacoco_build.format(external_dependency_version[jacoco_artifact_id]))

        if type == 'javadoc':
            aggregatePom.write(distribution_management)
            aggregatePom.write(start_javadoc_build.format(external_dependency_version[javadoc_artifact_id]))
            writeJavadocConfiguration(aggregatePom)
            aggregatePom.write(end_javadoc_build)

        aggregatePom.write(pom_file_end)


def writeJavadocConfiguration(aggregatePom: TextIOWrapper):
    with open(file='eng/scripts/aggregate_javadoc_configuration.txt', mode='r') as config:
        links = []
        excludedPackages = []
        excludedFiles = []
        groups = {}
        offlineLinks = {}

        for line in config:
            stripped_line = line.strip()
            
            if not stripped_line or stripped_line.startswith('#'):
                continue

            splits = stripped_line.split(';')
            if splits[0] == 'Link' and len(splits) == 2:
                links.append(splits[1])
            elif splits[0] == 'ExcludePackage' and len(splits) == 2:
                excludedPackages.append(splits[1])
            elif splits[0] == 'ExcludeFile' and len(splits) == 2:
                excludedFiles.append(splits[1])
            elif splits[0] == 'Group' and len(splits) == 3:
                groups[splits[1]] = splits[2]
            elif splits[0] == 'OfflineLink' and len(splits) == 3:
                offlineLinks[splits[1]] = splits[2]

        # Write external JavaDoc links
        aggregatePom.write(indent_1 + '<links>\n')
        for link in links:
            aggregatePom.write(indent_2 + '<link>')
            aggregatePom.write(link)
            aggregatePom.write('</link>\n')
        aggregatePom.write(indent_1 + '</links>\n')

        # Write excluded packages
        aggregatePom.write(indent_1 + '<excludePackageNames>\n' + indent_2)
        aggregatePom.write((':\n' + indent_2).join(excludedPackages))
        aggregatePom.write(indent_2 + '\n' + indent_1 + '</excludePackageNames>\n')


        # Write excluded files
        aggregatePom.write(indent_1 + '<sourceFileExcludes>\n')
        for excludedFile in excludedFiles:
            aggregatePom.write(indent_2 + '<sourceFileExclude>')
            aggregatePom.write(excludedFile)
            aggregatePom.write('</sourceFileExclude>\n')
        aggregatePom.write(indent_1 + '</sourceFileExcludes>\n')

        # Write groups
        aggregatePom.write(indent_1 + '<groups>\n')
        for name, packages in groups.items():
            aggregatePom.write(indent_2 + '<group>\n')
            aggregatePom.write(indent_3 + '<title>')
            aggregatePom.write(name)
            aggregatePom.write('</title>\n')
            aggregatePom.write(indent_3 + '<packages>')
            aggregatePom.write(packages)
            aggregatePom.write('</packages>\n')
            aggregatePom.write(indent_2 + '</group>\n')
        aggregatePom.write(indent_1 + '</groups>\n')

        # Write offlink links
        aggregatePom.write(indent_1 + '<offlineLinks>\n')
        for url, location in offlineLinks.items():
            aggregatePom.write(indent_2 + '<offlineLink>\n')
            aggregatePom.write(indent_3 + '<url>')
            aggregatePom.write(url)
            aggregatePom.write('</url>\n')
            aggregatePom.write(indent_3 + '<location>')
            aggregatePom.write("${project.basedir}/" + location)
            aggregatePom.write('</location>\n')
            aggregatePom.write(indent_2 + '</offlineLink>\n')
        aggregatePom.write(indent_1 + '</offlineLinks>\n')

# Function that creates the Projects within the repository.
# Projects contain a Maven identifier, module path, parent POM
def create_projects(project_list_identifiers: list, artifact_identifier_to_version: Dict[str, ArtifactVersion]) -> Dict[str, Project]:
    projects: Dict[str, Project] = {}

    for root, _, files in os.walk(root_path):
        # Ignore sdk/resourcemanagerhybrid, sdk/e2e, sdk/template and azure-security-test-keyvault-jca 
        if 'resourcemanagerhybrid' in root \
            or 'e2e' in root \
            or 'azure-security-test-keyvault-jca' in root \
            or 'template' in root:
            continue

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
        or project_identifier in exclude_projects \
        or parent_pom not in valid_parents \
        or group_id.text not in include_groups \
        or project_identifier in parent_pom_identifiers :
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
    parser = argparse.ArgumentParser(description='Generates a POM for aggregate reports.')
    parser.add_argument('--project-list', '--pl', type=str)
    parser.add_argument('--groups', '--g', type=str)
    parser.add_argument('--exclude-project-list', '--epl', type=str)
    parser.add_argument('--type', '--t', required=True, type=str, choices=['coverage', 'javadoc'], help='Specify the type of aggregate pom to generate.')
    args = parser.parse_args()
    start_time = time.time()
    create_aggregate_pom(args.project_list, args.groups, args.exclude_project_list, args.type)
    elapsed_time = time.time() - start_time

    print('Effective POM File')
    with open(file=client_aggregate_pom_path, mode='r') as aggregatePom:
        print(aggregatePom.read())

    print('elapsed_time={}'.format(elapsed_time))
    print('Total time for replacement: {} seconds'.format(str(timedelta(seconds=elapsed_time))))

if __name__ == '__main__':
    main()
