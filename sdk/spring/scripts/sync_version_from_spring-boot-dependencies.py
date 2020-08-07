# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.


import os
import time
import unittest
import urllib.request as request
import xml.etree.ElementTree as elementTree
from itertools import takewhile

import in_place

EXTERNAL_DEPENDENCIES_FILE = 'eng/versioning/external_dependencies.txt'
SPRING_BOOT_DEPENDENCIES_FILE = \
    'https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/{}/spring-boot-dependencies-{}.pom '


def main():
    start_time = time.time()
    change_to_root_dir()
    print('Current working directory = {}.'.format(os.getcwd()))
    spring_boot_version = get_spring_boot_version()
    print('spring_boot_version = {}.'.format(spring_boot_version))
    dependency_dict = get_spring_boot_dependencies(spring_boot_version)
    update_version_for_external_dependencies(dependency_dict)
    elapsed_time = time.time() - start_time
    print('elapsed_time = {}'.format(elapsed_time))


def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def get_spring_boot_version():
    file1 = open(EXTERNAL_DEPENDENCIES_FILE, 'r')
    lines = file1.readlines()
    for line in lines:
        if line.startswith('org.springframework.boot:spring-boot-dependencies;'):
            return line.split(';', 1)[1].strip()
    raise Exception("Can not get spring boot version.")


def get_spring_boot_dependencies(spring_boot_version):
    tree = elementTree.ElementTree(
        file = request.urlopen(
            SPRING_BOOT_DEPENDENCIES_FILE.format(spring_boot_version, spring_boot_version)))
    project_element = tree.getroot()
    name_space = {'maven': 'http://maven.apache.org/POM/4.0.0'}
    # get properties
    properties = project_element.find('maven:properties', name_space)
    property_dict = {}
    for p in properties:
        key = p.tag.split('}', 1)[1]
        value = p.text
        property_dict[key] = value
    # get dependencies
    dependency_dict = {}
    dependency_elements = project_element.findall(
        './maven:dependencyManagement/maven:dependencies/maven:dependency',
        name_space)
    for dependency_element in dependency_elements:
        group_id = dependency_element.find("./maven:groupId", name_space).text.strip(' ')
        artifact_id = dependency_element.find("./maven:artifactId", name_space).text.strip(' ')
        version = dependency_element.find("./maven:version", name_space).text.strip(' ${}')
        key = group_id + ':' + artifact_id
        value = property_dict[version]
        dependency_dict[key] = value
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
                        print('Not update version. key = {}, value = {}, value_in_dict = {}'
                              .format(key, value, value_in_dict))
                        file.write(line)
                    elif version_bigger_than(value, value_in_dict):
                        file.write('{};{}'.format(key, value))
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


def print_dict(d):
    for key, value in d.items():
        print('key = {}, value = {}.'.format(key, value))


if __name__ == '__main__':
    # unittest.main()
    main()
