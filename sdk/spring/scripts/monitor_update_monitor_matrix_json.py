# Python version 3.4 or higher is required to run this script.
#
# This script is used to update sdk\spring\pipeline\supported-version-matrix.json before compatibility check.
# Sample:
# 1. python .\sdk\spring\scripts\monitor_update_monitor_matrix_json.py
#
# The script must be run at the root of azure-sdk-for-java.


import time
from log import log
import os
import json


def change_to_repo_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def update_monitor_matrix_json_file(filepath, test_spring_boot_version):
    names = {}
    for version in test_spring_boot_version:
        names[version] = "springboot" + version.replace(".", "_")
    with open(filepath, 'r') as file:
        data = json.load(file)
        data['displayNames'] = names
        data['matrix']['TEST_SPRING_BOOT_VERSION'] = test_spring_boot_version
    with open(filepath, 'w') as file:
        json.dump(data, file, indent = 2)


def get_supported_spring_boot_version(filepath):
    version3 = []
    version2 = []
    scripts_dir = os.path.dirname(__file__)
    filepath = os.path.join(scripts_dir, '..', 'pipeline', 'spring-cloud-azure-supported-spring.json')
    with open(filepath, 'r', encoding='utf-8') as file:
        data = json.load(file)
    for entry in data:
        for key in entry:
            if entry[key] == "SUPPORTED" and entry["spring-boot-version"].startswith("3."):
                version3.append(entry["spring-boot-version"])
            if entry[key] == "SUPPORTED" and entry["spring-boot-version"].startswith("2."):
                version2.append(entry["spring-boot-version"])
    supported_version_list = [max(version3), max(version2)]
    return supported_version_list


def main():
    start_time = time.time()
    change_to_repo_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    scripts_dir = os.path.dirname(__file__)
    test_spring_boot_version = get_supported_spring_boot_version(os.path.join(scripts_dir, '..', 'pipeline', 'spring-cloud-azure-supported-spring.json'))
    update_monitor_matrix_json_file("./sdk/spring/pipeline/monitor-supported-version-matrix.json", test_spring_boot_version)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


if __name__ == '__main__':
    main()
