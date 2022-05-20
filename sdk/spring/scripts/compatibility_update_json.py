
import time
from log import log
import os
import json
import pandas as pd


def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def update_json_file(filepath, suppoerted_spring_boot_version):
    pass


def get_supported_spring_boot_version(filepath):
    supported_version_list = []
    with open(filepath, 'r') as file:
        data = json.load(file)
    for entry in data:
        for key in entry:
            if entry[key] == "supported":
                supported_version_list.append(entry["spring-boot-version"])
    return supported_version_list


def main():
    start_time = time.time()
    change_to_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    suppoerted_spring_boot_version = get_supported_spring_boot_version("./sdk/spring/spring-cloud-azure-supported-spring.json")
    update_json_file("./sdk/spring/supported-version-matrix.json", suppoerted_spring_boot_version)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


if __name__ == '__main__':
    main()