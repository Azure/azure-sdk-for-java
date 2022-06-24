# Python version 3.4 or higher is required to run this script.
#
# This script is used to delete dependency version in ./sdk/spring/**/pom*.xml for compatibility check.
# Sample:
# 1. python .\sdk\spring\scripts\compatibility_delete_version.py
#
# The script must be run at the root of azure-sdk-for-java.import time


import os
from os.path import join
import time

from log import log


def main():
    start_time = time.time()
    change_to_repo_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    find_all_poms_do_version_control("./sdk/spring")
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def change_to_repo_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def find_all_poms_do_version_control(directory):
    for root, dirs, files in os.walk(directory):
        for file_name in files:
            if file_name.startswith('pom') and file_name.endswith('.xml'):
                file_path = join(root, file_name)
                delete_dependency_version(file_path)


# Delete explicit versions, and use versions from Spring Boot or Spring Cloud BOMs
def delete_dependency_version(file_path):
    log.info("delete dependency version in " + file_path)
    with open(file_path, 'r', encoding = 'utf-8') as pom_file:
        lines = pom_file.readlines()
    with open(file_path, 'w', encoding = 'utf-8') as new_pom_file:
        for line in lines:
            if ';external_dependency} -->' not in line:
                new_pom_file.write(line)
            elif '<!-- {x-version-update;com.github.tomakehurst:wiremock-jre8;external_dependency} -->' in line:
                new_pom_file.write(line)
            elif external_dependencies_managed(line):
                # listed in external-dependencies.txt but not managed by spring
                new_pom_file.write(line)


def external_dependencies_managed(line):
    dependency_name = line.split(";")[1]
    flag = False
    with open(get_managed_file_name(), 'r', encoding = 'utf-8') as managed_file:
        lines = managed_file.readlines()
        for dependency in lines:
            if dependency_name not in dependency:
                flag = True
            else:
                flag = False
                break
    return flag


def get_managed_file_name():
    with open("./eng/versioning/external_dependencies.txt", "r", encoding = 'utf-8') as external_file:
        lines = external_file.readlines()
        for line in lines:
            if "org.springframework.boot:spring-boot-dependencies;" in line:
                return "sdk/spring/scripts/spring_boot_{}_managed_external_dependencies.txt".format(line.split(";")[1].replace("\n", ""))


if __name__ == '__main__':
    main()
