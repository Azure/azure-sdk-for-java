# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
#
# How to use this script to update spring-boot's version?
# Here are the steps:
# 1. Update ROOT_POMS' version manually.
# 2. Run command "python .\sdk\spring\scripts\update_spring_managed_external_dependencies.py".
# 3. Run command "python .\eng\versioning\update_versions.py --ut external_dependency --sr".
# 4. Run command ".\eng\versioning\pom_file_version_scanner.ps1 -Debug". If there is error, fix it.
# 5. Update changelog about compatible Spring Boot versions and Spring Cloud versions.

import argparse
import os
import queue
import time
import unittest
import urllib.request as request
import xml.etree.ElementTree as elementTree
from itertools import takewhile
from urllib.error import HTTPError
import in_place

from log import log, Log
from pom import Pom

EXTERNAL_DEPENDENCIES_FILE = 'eng/versioning/external_dependencies.txt'
ROOT_POMS = [
    'org.springframework.boot:spring-boot-starter-parent;2.5.4',
    'org.springframework.boot:spring-boot-dependencies;2.5.4',
    'org.springframework.cloud:spring-cloud-dependencies;2020.0.3'
]
SKIP_IDS = [
    'org.eclipse.jgit:org.eclipse.jgit'  # Refs: https://github.com/Azure/azure-sdk-for-java/pull/13956/files#r468368271
]
MAVEN_NAME_SPACE = {'maven': 'http://maven.apache.org/POM/4.0.0'}


def main():
    start_time = time.time()
    change_to_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    dependency_dict = {}
    for root_pom in ROOT_POMS:
        update_dependency_dict(dependency_dict, root_pom)
    output_version_dict_to_file(dependency_dict)
    update_version_for_external_dependencies(dependency_dict)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def update_dependency_dict(dependency_dict, root_pom_id):
    root_pom_info = root_pom_id.split(';')
    root_pom_group_artifact = root_pom_info[0]
    root_pom_group_info = root_pom_group_artifact.split(':')
    root_pom_group_id = root_pom_group_info[0]
    root_pom_artifact_id = root_pom_group_info[1]
    root_pom_version = root_pom_info[1]
    dependency_dict[root_pom_group_id + ':' + root_pom_artifact_id] = root_pom_version
    root_pom = Pom(
        root_pom_group_id,
        root_pom_artifact_id,
        root_pom_version,
        1
    )
    q = queue.Queue()
    q.put(root_pom)
    pom_count = 1
    log.info('Added root pom.depth = {}, url = {}.'.format(root_pom.depth, root_pom.to_url()))
    while not q.empty():
        pom = q.get()
        pom_url = pom.to_url()
        log.info('Get dependencies from pom. depth = {}, url = {}.'.format(pom.depth, pom_url))
        try:
            tree = elementTree.ElementTree(file = request.urlopen(pom_url))
        except HTTPError:
            log.warn('Error in open {}'.format(pom_url))
            continue
        project_element = tree.getroot()
        property_dict = {}
        parent_element = project_element.find('./maven:parent', MAVEN_NAME_SPACE)
        if parent_element is not None:
            # get properties from parent
            parent_group_id = parent_element.find('./maven:groupId', MAVEN_NAME_SPACE).text.strip(' ${}')
            parent_artifact_id = parent_element.find('./maven:artifactId', MAVEN_NAME_SPACE).text.strip(' ${}')
            parent_version = parent_element.find('./maven:version', MAVEN_NAME_SPACE).text.strip(' ${}')
            parent_pom = Pom(parent_group_id, parent_artifact_id, parent_version, pom.depth + 1)
            parent_pom_url = parent_pom.to_url()
            parent_tree = elementTree.ElementTree(file = request.urlopen(parent_pom_url))
            parent_project_element = parent_tree.getroot()
            log.debug('Get properties from parent pom. parent_pom_url = {}.'.format(parent_pom_url))
            update_property_dict(parent_project_element, property_dict)
        update_property_dict(project_element, property_dict)
        # get dependencies
        dependency_elements = project_element.findall('./maven:dependencyManagement/maven:dependencies/maven:dependency', MAVEN_NAME_SPACE)
        for dependency_element in dependency_elements:
            group_id = dependency_element.find('./maven:groupId', MAVEN_NAME_SPACE).text.strip(' ${}')
            # some group_id contain 'project.groupId', so put project_version first.
            if group_id in property_dict:
                group_id = property_dict[group_id]
            artifact_id = dependency_element.find('./maven:artifactId', MAVEN_NAME_SPACE).text.strip(' ')
            version = dependency_element.find('./maven:version', MAVEN_NAME_SPACE).text.strip(' ${}')
            key = group_id + ':' + artifact_id
            if version in property_dict:
                version = property_dict[version]
            if key not in dependency_dict:
                dependency_dict[key] = version
                log.debug('Dependency version added. key = {}, value = {}'.format(key, version))
            elif version != dependency_dict[key]:
                log.info('Dependency version skipped. key = {}, version = {}, dependency_dict[key] = {}.'.format(key, version, dependency_dict[key]))
            artifact_type = dependency_element.find('./maven:type', MAVEN_NAME_SPACE)
            artifact_scope = dependency_element.find('./maven:scope', MAVEN_NAME_SPACE)
            if artifact_type is not None and \
                artifact_scope is not None and \
                artifact_type.text.strip() == 'pom' and \
                artifact_scope.text.strip() == 'import':
                new_pom = Pom(group_id, artifact_id, version, pom.depth + 1)
                q.put(new_pom)
                pom_count = pom_count + 1
    log.info('Root pom summary. pom_count = {}, root_pom_url = {}'.format(pom_count, root_pom.to_url()))


def update_property_dict(project_element, property_dict):
    # get properties
    properties = project_element.find('maven:properties', MAVEN_NAME_SPACE)
    # some property contain 'project.version', so put 'project.version' into property_dict.
    version_element = project_element.find('./maven:version', MAVEN_NAME_SPACE)
    if version_element is None:
        version_element = project_element.find('./maven:parent/maven:version', MAVEN_NAME_SPACE)
    project_version = version_element.text.strip()
    property_dict['project.version'] = project_version
    # some property contain 'project.groupId', so put 'project.groupId' into property_dict.
    group_id_element = project_element.find('./maven:groupId', MAVEN_NAME_SPACE)
    if group_id_element is None:
        group_id_element = project_element.find('./maven:parent/maven:groupId', MAVEN_NAME_SPACE)
    group_id = group_id_element.text.strip()
    property_dict['project.groupId'] = group_id
    if properties is not None:
        for p in properties:
            key = p.tag.split('}', 1)[1]
            value_text = p.text
            if value_text is None:
                # sometimes we have tag with no text, like: <release.arguments/>
                continue
            value = value_text.strip(' ${}')
            if value in property_dict:
                value = property_dict[value]
            property_dict[key] = value
    # sometimes project_version contains '${foo}', so update project_version.
    if project_version.startswith('${'):
        property_dict['project.version'] = property_dict[project_version.strip(' ${}')]
    return property_dict


def output_version_dict_to_file(dependency_dict):
    output_file = open('sdk/spring/scripts/spring_managed_external_dependencies.txt', 'w''')
    for key, value in sorted(dependency_dict.items()):
        output_file.write('{};{}\n'.format(key, value))
    output_file.close()


def update_version_for_external_dependencies(dependency_dict):
    with in_place.InPlace(EXTERNAL_DEPENDENCIES_FILE) as file:
        for line in file:
            line = line.strip()
            if line.startswith('#') or not line:
                file.write(line)
            else:
                key_value = line.split(';', 1)
                key = key_value[0]
                value = key_value[1]
                if key not in SKIP_IDS and key in dependency_dict:
                    value_in_dict = dependency_dict[key]
                    if version_bigger_than(value, value_in_dict):
                        log.warn('Version update skipped. key = {}, value = {}, new_value = {}'.format(key, value, value_in_dict))
                        file.write(line)
                    elif version_bigger_than(value_in_dict, value):
                        log.info('Version updated. key = {}, value = {}, new_value = {}'.format(key, value, value_in_dict))
                        file.write('{};{}'.format(key, value_in_dict))
                    else:
                        file.write(line)
                else:
                    file.write(line)
            file.write('\n')


def version_bigger_than(version1, version2):
    v1 = version1.split('.')
    v2 = version2.split('.')
    len_1 = len(v1)
    len_2 = len(v2)
    max_len = max(len_1, len_1)
    for i in range(max_len):
        if i < len_1 and i < len_2:
            int_1 = int('0' + ''.join(takewhile(str.isdigit, v1[i])))
            int_2 = int('0' + ''.join(takewhile(str.isdigit, v2[i])))
            if int_1 != int_2:
                return int_1 > int_2
        elif i < len_1:
            return True
        else:
            return False
    return False


def print_dict(d):
    for key, value in d.items():
        print('key = {}, value = {}.'.format(key, value))


class Tests(unittest.TestCase):
    def test_version_bigger_than(self):
        self.assertEqual(version_bigger_than('1', '2'), False)
        self.assertEqual(version_bigger_than('2', '1'), True)
        self.assertEqual(version_bigger_than('1.0', '2'), False)
        self.assertEqual(version_bigger_than('2.0', '1'), True)
        self.assertEqual(version_bigger_than('1.1', '1'), True)
        self.assertEqual(version_bigger_than('1', '1.1'), False)
        self.assertEqual(version_bigger_than('1.0-RELEASE', '1.1'), False)
        self.assertEqual(version_bigger_than('1.1-RELEASE', '1'), True)
        self.assertEqual(version_bigger_than('1.1-RELEASE', '1.0'), True)
        self.assertEqual(version_bigger_than('1.1-RELEASE', '1.0.1'), True)
        self.assertEqual(version_bigger_than('1.1-RELEASE', '1.0.1-RELEASE'), True)
        self.assertEqual(version_bigger_than('1.1-RELEASE', '1.1.1-RELEASE'), False)


def init():
    parser = argparse.ArgumentParser(
        description = 'Update versions in /eng/versioning/external_dependencies.txt.'
    )
    parser.add_argument(
        '--log',
        type = str,
        choices = ['debug', 'info', 'warn', 'error', 'none'],
        required = False,
        default = 'info',
        help = 'Set log level.'
    )
    args = parser.parse_args()
    log_dict = {
        'debug': Log.DEBUG,
        'info': Log.INFO,
        'warn': Log.WARN,
        'error': Log.ERROR,
        'none': Log.NONE
    }
    log.set_log_level(log_dict[args.log])
    # log.log_level_test()


if __name__ == '__main__':
    # unittest.main()
    init()
    main()