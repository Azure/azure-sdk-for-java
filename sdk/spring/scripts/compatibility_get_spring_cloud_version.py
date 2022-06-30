# Python version 3.4 or higher is required to run this script.
#
# This script is only used to return spring-cloud version during pipeline running.
#
# The script must be run at the root of azure-sdk-for-java.


import os
import json
import argparse


def get_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-b', '--spring_boot_dependencies_version', type = str, required = True)
    return parser.parse_args()


def change_to_repo_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def get_spring_cloud_version(filepath):
    spring_boot_version = get_args().spring_boot_dependencies_version
    spring_cloud_version = ""
    with open(filepath, 'r') as file:
        data = json.load(file)
    for entry in data:
        for key in entry:
            if spring_boot_version == entry[key]:
                spring_cloud_version = entry["spring-cloud-version"]
                break
    print(spring_cloud_version)


def main():
    change_to_repo_root_dir()
    get_spring_cloud_version("./sdk/spring/spring-cloud-azure-supported-spring.json")


if __name__ == '__main__':
    main()
