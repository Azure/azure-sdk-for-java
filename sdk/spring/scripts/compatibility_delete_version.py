# Python version 3.4 or higher is required to run this script.
#
# This script is used to delete dependency version in ./sdk/spring/**/pom*.xml for compatibility check.
# Sample:
# 1. python .\sdk\spring\scripts\compatibility_delete_version.py --spring_boot_dependencies_version 2.7.0
# 2. python .\sdk\spring\scripts\compatibility_delete_version.py -b 2.7.0
#
# The script must be run at the root of azure-sdk-for-java.import time


import os
from os.path import join
import time
import argparse

from log import log

IGNORED_ARTIFACTS = {'com.github.tomakehurst:wiremock-jre8'}
IGNORED_SPRINGBOOT_ARTIFACTS = {
    "2.5.14": {"org.postgresql:postgresql"}
    }

def get_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-b', '--spring_boot_dependencies_version', type = str, required = True)
    return parser.parse_args()

def get_ignored_artifacts():
    spring_boot_dependencies_version = get_args().spring_boot_dependencies_version
    return IGNORED_ARTIFACTS.union(IGNORED_SPRINGBOOT_ARTIFACTS.get(spring_boot_dependencies_version, {}))

def main():
    start_time = time.time()
    change_to_repo_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    find_all_poms_do_version_control("./sdk/spring", get_ignored_artifacts())
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def change_to_repo_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def find_all_poms_do_version_control(directory, ignored_artifacts):
    for root, dirs, files in os.walk(directory):
        for file_name in files:
            if file_name.startswith('pom') and file_name.endswith('.xml'):
                file_path = join(root, file_name)
                delete_dependency_version(file_path, ignored_artifacts)


# Delete explicit versions, and use versions from Spring Boot or Spring Cloud BOMs
def delete_dependency_version(file_path, ignored_artifacts):
    log.info("delete dependency version in " + file_path)
    with open(file_path, 'r', encoding = 'utf-8') as pom_file:
        lines = pom_file.readlines()
    with open(file_path, 'w', encoding = 'utf-8') as new_pom_file:
        for line in lines:
            if ';external_dependency} -->' not in line:
                new_pom_file.write(line)
            elif line.split(";")[1] in ignored_artifacts:
                new_pom_file.write(line)
            elif line.split(";")[1] not in external_dependencies_managed_list():
                # listed in external-dependencies.txt but not managed by spring
                new_pom_file.write(line)


def external_dependencies_managed_list():
    dependencies = set()
    with open(get_managed_file_name(), 'r', encoding = 'utf-8') as managed_file:
        lines = managed_file.readlines()
        for dependency in lines:
            dependencies.add(dependency.split(";")[0])
    return dependencies


def get_managed_file_name():
    with open("./eng/versioning/external_dependencies.txt", "r", encoding = 'utf-8') as external_file:
        lines = external_file.readlines()
        for line in lines:
            if "org.springframework.boot:spring-boot-dependencies;" in line:
                return "sdk/spring/scripts/spring_boot_{}_managed_external_dependencies.txt".format(line.split(";")[1].replace("\n", ""))


if __name__ == '__main__':
    main()
