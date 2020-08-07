# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.


import os
import queue
import time
import unittest
import urllib.request as request
import xml.etree.ElementTree as elementTree
from itertools import takewhile

import in_place

EXTERNAL_DEPENDENCIES_FILE = 'eng/versioning/external_dependencies.txt'
POM = 'https://repo.maven.apache.org/maven2/{group}/{artifact}/{version}/{artifact}-{version}.pom'

LOG_LEVEL_DEBUG = 5
LOG_LEVEL_INFO = 4
LOG_LEVEL_WARN = 3
LOG_LEVEL_ERROR = 2
LOG_LEVEL_NONE = 1
logLevel = LOG_LEVEL_INFO


class PomModule:
    def __init__(self, group_id, artifact_id, version):
        self.group_id = group_id
        self.artifact_id = artifact_id
        self.version = version

    def to_url(self):
        return POM.format(
            group = self.group_id.replace('.', '/'),
            artifact = self.artifact_id,
            version = self.version)

    def __str__(self):
        return '{}:{}:{}'.format(self.group_id, self.artifact_id, self.version)


def main():
    start_time = time.time()
    change_to_root_dir()
    debug('Current working directory = {}.'.format(os.getcwd()))
    dependency_dict = get_dependency_dict()
    update_version_for_external_dependencies(dependency_dict)
    elapsed_time = time.time() - start_time
    info('elapsed_time = {}'.format(elapsed_time))


def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def get_spring_boot_version():
    file1 = open(EXTERNAL_DEPENDENCIES_FILE, 'r')
    lines = file1.readlines()
    for line in lines:
        if line.startswith('org.springframework.boot:spring-boot-dependencies;'):
            return line.split(';', 1)[1].strip()
    raise Exception('Can not get spring boot version.')


def get_dependency_dict():
    spring_boot_version = get_spring_boot_version()
    pom_module = PomModule(
        'org.springframework.boot',
        'spring-boot-dependencies',
        spring_boot_version)
    q = queue.Queue()
    q.put(pom_module)
    dependency_dict = {}
    while not q.empty():
        pom_module = q.get()
        pom_url = pom_module.to_url()
        info('Get dependencies from pom: {}.'.format(pom_url))
        tree = elementTree.ElementTree(file = request.urlopen(pom_url))
        project_element = tree.getroot()
        name_space = {'maven': 'http://maven.apache.org/POM/4.0.0'}
        # get properties
        properties = project_element.find('maven:properties', name_space)
        property_dict = {}
        # some property contain 'project.version', so put project_version first.
        version_element = project_element.find('./maven:version', name_space)
        if version_element is None:
            version_element = project_element.find('./maven:parent/maven:version', name_space)
        project_version = version_element.text.strip()
        property_dict['project.version'] = project_version
        # some property contain 'project.groupId', so put project_version first.
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
        dependency_elements = project_element.findall(
            './maven:dependencyManagement/maven:dependencies/maven:dependency',
            name_space)
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
                debug('    Dependency version added. key = {}, value = {}'.format(key, version))
            else:
                debug('    Dependency version skipped. key = {}, value = {}'.format(key, version))
            artifact_type = dependency_element.find('./maven:type', name_space)
            if artifact_type is not None and artifact_type.text.strip() == 'pom':
                new_pom_module = PomModule(group_id, artifact_id, version)
                q.put(new_pom_module)
                debug('Added new pom pom: {}.'.format(new_pom_module.to_url()))
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
                        warn('Version update skipped. key = {}, value = {}, value_in_dict = {}'
                             .format(key, value, value_in_dict))
                        file.write(line)
                    elif version_bigger_than(value_in_dict, value):
                        info('Version updated. key = {}, value = {}, new_value = {}'.format(
                            key,
                            value_in_dict,
                            value))
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


def debug(string):
    if (logLevel >= LOG_LEVEL_DEBUG):
        print('[DEBUG] {}'.format(string))


def info(string):
    if (logLevel >= LOG_LEVEL_INFO):
        print('[INFO ] {}'.format(string))


def warn(string):
    if (logLevel >= LOG_LEVEL_WARN):
        print('[WARN ] {}'.format(string))


def error(string):
    if (logLevel >= LOG_LEVEL_ERROR):
        print('[WARN ] {}'.format(string))


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


if __name__ == '__main__':
    # unittest.main()
    logLevel = LOG_LEVEL_INFO
    main()
