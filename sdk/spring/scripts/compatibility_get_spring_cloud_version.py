# Python version 3.4 or higher is required to run this script.
#
# This script is only used to return spring-cloud version during pipeline running.
#
# The script must be run at the root of azure-sdk-for-java.


import os
import json


def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def get_spring_cloud_version_and_set_as_env(filepath):
    spring_boot_version = os.getenv("SPRING_CLOUD_AZURE_TEST_SUPPORTED_SPRING_BOOT_VERSION")
    spring_cloud_version = "1"
    with open(filepath, 'r') as file:
        data = json.load(file)
    for entry in data:
        for key in entry:
            if spring_boot_version == entry[key]:
                spring_cloud_version = entry["spring-cloud-version"]
                break
    return spring_cloud_version


def main():
    change_to_root_dir()
    get_spring_cloud_version_and_set_as_env("./sdk/spring/spring-cloud-azure-supported-spring.json")


if __name__ == '__main__':
    main()
