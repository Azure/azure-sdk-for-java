# Python version 3.4 or higher is required to run this script.
#
# This script is used to update sdk\spring\supported-version-matrix.json before compatibility check.
# Sample:
# 1. python .\sdk\spring\scripts\compatibility_update_supported_version_matrix_json.py
# 2. python .\sdk\spring\scripts\compatibility_update_supported_version_matrix_json.py -m 3
#
# The script must be run at the root of azure-sdk-for-java.import time


import time
from log import log
import os
import json
import argparse
import requests


def get_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-m', '--spring-boot-major-version', type = str, default='2')
    return parser.parse_args()


def change_to_repo_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def update_supported_version_matrix_json_file(filepath, suppoerted_spring_boot_version):
    names = {}
    for version in suppoerted_spring_boot_version:
        names[version] = "springboot" + version.replace(".", "_")
    with open(filepath, 'r') as file:
        data = json.load(file)
        data['displayNames'] = names
        data['matrix']['SPRING_CLOUD_AZURE_TEST_SUPPORTED_SPRING_BOOT_VERSION'] = suppoerted_spring_boot_version
    with open(filepath, 'w') as file:
        json.dump(data, file, indent = 2)


def get_supported_spring_boot_version(filepath):
    supported_version_list = []
    data = requests.get(filepath).json()
    for entry in data:
        for key in entry:
            if entry[key] == "SUPPORTED":
                if entry["spring-boot-version"].startswith(get_args().spring_boot_major_version + "."):
                    supported_version_list.append(entry["spring-boot-version"])
    return supported_version_list


def main():
    start_time = time.time()
    change_to_repo_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    suppoerted_spring_boot_version = get_supported_spring_boot_version("https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/spring/spring-cloud-azure-supported-spring.json")
    update_supported_version_matrix_json_file("./sdk/spring/supported-version-matrix.json", suppoerted_spring_boot_version)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


if __name__ == '__main__':
    main()
