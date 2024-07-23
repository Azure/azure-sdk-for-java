# Python version 3.4 or higher is required to run this script.
#
# This script is used to update sdk\spring\pipeline\supported-version-matrix.json before compatibility check.
# Sample:
# 1. python .\sdk\spring\scripts\compatibility_update_supported_version_matrix_json.py
# 2. python .\sdk\spring\scripts\compatibility_update_supported_version_matrix_json.py --spring-boot-major-version-number 3 --matrix-config-path sdk/spring/pipeline/supported-version-matrix.json
#
# The script must be run at the root of azure-sdk-for-java.


import time
from log import log
import os
import json
import argparse
import requests

from compatibility_get_spring_cloud_version import get_spring_cloud_version
from _constants import SPRING_BOOT_MAJOR_2_VERSION_NAME, SPRING_BOOT_MAJOR_3_VERSION_NAME


def get_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-t', '--target-version-prefix', type=str)
    parser.add_argument('-n', '--non-target-version-prefix-list', type=str, nargs='*')
    parser.add_argument(
        '-sbmvn',
        '--spring-boot-major-version-number',
        type=str,
        choices=[SPRING_BOOT_MAJOR_2_VERSION_NAME, SPRING_BOOT_MAJOR_3_VERSION_NAME],
        default=SPRING_BOOT_MAJOR_3_VERSION_NAME,
        help='Which major version of Spring Boot to use. The default is ' + SPRING_BOOT_MAJOR_3_VERSION_NAME + '.'
    )
    parser.add_argument('-mcp', '--matrix-config-path', type=str, default='sdk/spring/pipeline/supported-version-matrix.json')
    return parser.parse_args()


def change_to_repo_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def update_supported_version_matrix_json_file(filepath, suppoerted_spring_boot_version):
    names = {}
    for boot_version in suppoerted_spring_boot_version:
        cloud_version = get_spring_cloud_version(boot_version)
        names[boot_version] = "SpringBoot" + boot_version.replace(".", "_") + "_Cloud" + cloud_version.replace(".", "_")
    with open(filepath, 'r') as file:
        data = json.load(file)
        data['displayNames'].update(names)
        data['matrix']['SPRING_CLOUD_AZURE_TEST_SUPPORTED_SPRING_BOOT_VERSION'] = suppoerted_spring_boot_version
    with open(filepath, 'w') as file:
        json.dump(data, file, indent=2)


def get_supported_spring_boot_version(target_version_prefix, non_target_version_prefix_list):
    supported_version_list = []
    filepath = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/spring/pipeline/spring-cloud-azure-supported-spring.json"
    data = requests.get(filepath).json()
    for entry in data:
        for key in entry:
            if entry[key] == "SUPPORTED":
                if is_target_version(entry["spring-boot-version"], target_version_prefix,
                                     non_target_version_prefix_list):
                    supported_version_list.append(entry["spring-boot-version"])
    return supported_version_list


def is_target_version(spring_boot_version, target_version_prefix, non_target_version_prefix_list):
    if not spring_boot_version.startswith(get_args().spring_boot_major_version_number + "."):
        return False

    if not spring_boot_version.startswith(target_version_prefix):
        return False
    for entry in non_target_version_prefix_list:
        if spring_boot_version.startswith(entry):
            return False
    return True


def main():
    start_time = time.time()
    change_to_repo_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    supported_spring_boot_version = get_supported_spring_boot_version(
        get_args().target_version_prefix, get_args().non_target_version_prefix_list)
    update_supported_version_matrix_json_file(get_args().matrix_config_path, supported_spring_boot_version)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


if __name__ == '__main__':
    main()
