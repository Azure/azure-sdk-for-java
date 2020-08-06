# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.


import fileinput
import os
import requests
import time
import xml.etree.ElementTree as ET


EXTERNAL_DEPENDENCIES_FILE = 'eng/versioning/external_dependencies.txt'
SPRING_BOOT_DEPENDENCIES_FILE = 'https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/{}/spring-boot-dependencies-{}.pom'


def main():
    start_time = time.time()
    change_to_root_dir()
    print('Current working directory = {}.'.format(os.getcwd()))
    spring_boot_version = get_spring_boot_version()
    print('spring_boot_version = {}.'.format(spring_boot_version))
    dependencyDict = get_spring_boot_dependencies(spring_boot_version)
    update_version_for_external_dependencies(dependencyDict)

    elapsed_time = time.time() - start_time
    print('elapsed_time = {}'.format(elapsed_time))


def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def get_spring_boot_version():
    file1 = open(EXTERNAL_DEPENDENCIES_FILE, 'r')
    Lines = file1.readlines()
    count = 0
    for line in Lines:
        if line.startswith('org.springframework.boot:spring-boot-dependencies;'):
            return line.split(';', 1)[1].strip()
    raise Exception("Can not get spring boot version.")


def get_spring_boot_dependencies(spring_boot_version):
    r = requests.get(SPRING_BOOT_DEPENDENCIES_FILE.format(spring_boot_version, spring_boot_version))
    projectElement = ET.fromstring(r.text)
    nameSpace = {'maven': 'http://maven.apache.org/POM/4.0.0'}
    # get properties
    properties = projectElement.find('maven:properties', nameSpace)
    propertyDict = {}
    for property in properties:
        key = property.tag.split('}', 1)[1]
        value = property.text
        propertyDict[key] = value
    # get dependencies
    dependencyDict = {}
    dependencyElements = projectElement.findall('./maven:dependencyManagement/maven:dependencies/maven:dependency', nameSpace)
    for dependencyElement in dependencyElements:
        groupId = dependencyElement.find("./maven:groupId", nameSpace).text.strip(' ')
        artifactId = dependencyElement.find("./maven:artifactId", nameSpace).text.strip(' ')
        version = dependencyElement.find("./maven:version", nameSpace).text.strip(' ${}')
        key = groupId + ':' + artifactId
        value = propertyDict[version]
        dependencyDict[key] = value
    return dependencyDict


def update_version_for_external_dependencies(dependencyDict):
    file_line_count = sum(1 for line in open(EXTERNAL_DEPENDENCIES_FILE))
    for line in fileinput.input(EXTERNAL_DEPENDENCIES_FILE, inplace=True):
        line = line.strip()
        endValue = '' if fileinput.filelineno() == file_line_count else '\n'
        if line.startswith('#') or not line:
            print(line, end = endValue)
        else:
            keyValue = line.split(';', 1)
            key = keyValue[0]
            value = keyValue[1]
            if key in dependencyDict:
                value = dependencyDict[key]
            print('{};{}'.format(key, value), end = endValue)


def print_dict(dict):
    for key, value in dict.items():
        print('key = {}, value = {}.'.format(key, value))


if __name__ == '__main__':
    main()