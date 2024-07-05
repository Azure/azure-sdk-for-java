# Python version 3.4 or higher is required to run this script.
#
# This script is only used to return spring-cloud version during pipeline running.
#
# The script must be run at the root of azure-sdk-for-java.


import os
import argparse
import requests


def get_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-b', '--spring_boot_dependencies_version', type = str, required = True)
    return parser.parse_args()


def change_to_repo_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def get_spring_cloud_version(filepath):
    spring_boot_version = get_args().spring_boot_dependencies_version
    spring_cloud_version = None
    data = requests.get(filepath).json()

    # Only find the Spring Cloud Version that matches the special Spring Boot version first
    for entry in data:
        for key in entry:
            if spring_boot_version == entry[key]:
                spring_cloud_version = entry["spring-cloud-version"]
                break

        if spring_cloud_version is not None:
            break
    print(spring_cloud_version)


def main():
    change_to_repo_root_dir()
    get_spring_cloud_version("https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/spring/pipeline/spring-cloud-azure-supported-spring.json")


if __name__ == '__main__':
    main()
