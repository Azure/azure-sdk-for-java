# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.


import argparse
import os
import queue
import time
import unittest
import urllib.request as request
import xml.etree.ElementTree as elementTree
from itertools import takewhile

import in_place

from log import log, Log
from pom import Pom

EXTERNAL_DEPENDENCIES_FILE = 'eng/versioning/external_dependencies.txt'
ROOT_POM_IDS = [
    'org.springframework.boot:spring-boot-dependencies',
    'org.springframework.cloud:spring-cloud-dependencies'
]


def main():
    start_time = time.time()
    change_to_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    dependency_dict = {}
    for root_pom_id in ROOT_POM_IDS:
        update_dependency_dict(dependency_dict, root_pom_id)
    update_version_for_external_dependencies(dependency_dict)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def get_version_from_external_dependencies(key):
    file1 = open(EXTERNAL_DEPENDENCIES_FILE, 'r')
    lines = file1.readlines()
    for line in lines:
        if line.startswith('{};'.format(key)):
            return line.split(';', 1)[1].strip()
    raise Exception('Can not get version from external_dependencies, key = {}.'.format(key))


def update_dependency_dict(dependency_dict, root_pom_id):
    root_pom_info = root_pom_id.split(':')
    root_pom_group_id = root_pom_info[0]
    root_pom_artifact_id = root_pom_info[1]
    root_pom_version = get_version_from_external_dependencies(root_pom_id)
    root_pom = Pom(
        root_pom_group_id,
        root_pom_artifact_id,
        root_pom_version,
        1
    )
    q = queue.Queue()
    q.put(root_pom)
    pom_count = 1
    log.info('Added root pom: {}, depth = {}.'.format(root_pom.to_url(), root_pom.depth))
    while not q.empty():
        pom = q.get()
        pom_url = pom.to_url()
        log.info('Get dependencies from pom: {}, depth = {}.'.format(pom_url, pom.depth))
        tree = elementTree.ElementTree(file = request.urlopen(pom_url))
        project_element = tree.getroot()
        name_space = {'maven': 'http://maven.apache.org/POM/4.0.0'}
        # get properties
        properties = project_element.find('maven:properties', name_space)
        property_dict = {}
        # some property contain 'project.version', so put 'project.version' into property_dict.
        version_element = project_element.find('./maven:version', name_space)
        if version_element is None:
            version_element = project_element.find('./maven:parent/maven:version', name_space)
        project_version = version_element.text.strip()
        property_dict['project.version'] = project_version
        # some property contain 'project.groupId', so put 'project.groupId' into property_dict.
        group_id_element = project_element.find('./maven:groupId', name_space)
        if group_id_element is None:
            group_id_element = project_element.find('./maven:parent/maven:groupId', name_space)
        group_id = group_id_element.text.strip()
        property_dict['project.groupId'] = group_id
        if properties is not None:
            for p in properties:
                key = p.tag.split('}', 1)[1]
                value = p.text.strip(' ${}')
                if value in property_dict:
                    value = property_dict[value]
                property_dict[key] = value
        # sometimes project_version contains '${foo}', so update project_version.
        if project_version.startswith('${'):
            property_dict['project.version'] = property_dict[project_version.strip(' ${}')]
        # get dependencies
        dependency_elements = project_element.findall('./maven:dependencyManagement/maven:dependencies/maven:dependency', name_space)
        for dependency_element in dependency_elements:
            group_id = dependency_element.find('./maven:groupId', name_space).text.strip(' ${}')
            # some group_id contain 'project.groupId', so put project_version first.
            if group_id in property_dict:
                group_id = property_dict[group_id]
            artifact_id = dependency_element.find('./maven:artifactId', name_space).text.strip(' ')
            version = dependency_element.find('./maven:version', name_space).text.strip(' ${}')
            key = group_id + ':' + artifact_id
            if version in property_dict:
                version = property_dict[version]
            if key not in dependency_dict:
                dependency_dict[key] = version
                log.debug('Dependency version added. key = {}, value = {}'.format(key, version))
            elif version != dependency_dict[key]:
                log.debug('Dependency version skipped. key = {}, version = {}, dependency_dict[key] = {}.'.format(key, version, dependency_dict[key]))
            artifact_type = dependency_element.find('./maven:type', name_space)
            if artifact_type is not None and artifact_type.text.strip() == 'pom':
                new_pom = Pom(group_id, artifact_id, version, pom.depth + 1)
                q.put(new_pom)
                pom_count = pom_count + 1
                log.debug('Added new pom: {}, depth = {}.'.format(new_pom.to_url(), new_pom.depth))
    log.info('Root pom summary: root_pom = {}, pom_count = {}'.format(root_pom.to_url(), pom_count))
    return dependency_dict


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
                if key in dependency_dict:
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
