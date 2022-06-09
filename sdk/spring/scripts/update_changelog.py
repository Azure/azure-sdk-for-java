# Python version 3.4 or higher is required to run this script.
#
# This script is used to update changelog about Spring Boot and Spring Cloud dependencies versions in `/sdk/spring/CHANGELOG.md`.
#
# Sample:
# 1. python .\sdk\spring\scripts\update_changelog.py --spring_boot_dependencies_version 2.7.0 --spring_cloud_dependencies_version 2021.0.3
# 2. python .\sdk\spring\scripts\update_changelog.py -b 2.7.0 -c 2021.0.3
#
# The script must be run at the root of azure-sdk-for-java.


import os
import time
import argparse

from log import log


CHANGE_LOG_FILE = 'sdk/spring/CHANGELOG.md'


def get_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-b', '--spring_boot_dependencies_version', type = str, required = True)
    parser.add_argument('-c', '--spring_cloud_dependencies_version', type = str, required = True)
    return parser.parse_args()


def get_change_log_content(spring_boot_dependencies_version, spring_cloud_dependencies_version):
    return "\nUpgrade Spring Boot dependencies version to {} and Spring Cloud dependencies version to {}".format(spring_boot_dependencies_version,spring_cloud_dependencies_version)


def update_changelog(spring_boot_dependencies_version, spring_cloud_dependencies_version, target_file):
    with open(target_file, 'r', encoding = 'utf-8') as file:
        file_content = file.read()
        insert_position = file_content.find('(Unreleased)') + len('(Unreleased)')
        insert_content = get_change_log_content(spring_boot_dependencies_version, spring_cloud_dependencies_version)
        final_content = file_content[:insert_position] + insert_content + file_content[insert_position:]
    with open(target_file, 'r+', encoding = 'utf-8') as file:
        file.writelines(final_content)


def main():
    start_time = time.time()
    change_to_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    args = get_args()
    update_changelog(args.spring_boot_dependencies_version, args.spring_cloud_dependencies_version, CHANGE_LOG_FILE)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


if __name__ == '__main__':
    main()
