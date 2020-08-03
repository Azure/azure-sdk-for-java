# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.


import os
import time
import requests
import xml.etree.ElementTree as ET


def main():
    start_time = time.time()
    change_to_root_dir()
    print('Working directory: ' + os.getcwd())
    spring_boot_version = get_spring_boot_version()
    print('spring_boot_version={}'.format(spring_boot_version))
    get_spring_boot_dependencies()

    elapsed_time = time.time() - start_time
    print('elapsed_time={}'.format(elapsed_time))


def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def get_spring_boot_version():
    file1 = open('eng/versioning/external_dependencies.txt', 'r')
    Lines = file1.readlines()
    count = 0
    for line in Lines:
        if line.startswith('org.springframework.boot:spring-boot;'):
            return line.split(';', 1)[1]
    raise Exception("Can not get spring boot version.")


def get_spring_boot_dependencies():
    r = requests.get('https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.3.0.RELEASE/spring-boot-dependencies-2.3.0.RELEASE.pom')
    root = ET.fromstring(r.text)
    ns = {'maven', 'http://maven.apache.org/POM/4.0.0'}
    properties = root.find('{http://maven.apache.org/POM/4.0.0}properties')
    propertyDict = {}
    for property in properties:
        key = property.tag.split('}', 1)[1]
        value = property.text
        propertyDict[key] = value
    print_dict(propertyDict)


def print_dict(dict):
    for key, value in dict.items():
        print('key = {}, value = {}.'.format(key, value))


if __name__ == '__main__':
    main()