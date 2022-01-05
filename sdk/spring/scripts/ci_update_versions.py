# Python version 3.4 or higher is required to run this script.
#
# This script is used to change external_dependency versions of springboot in sdk/spring.
# Make sure 'spring_boot_{Spring boot version}_managed_external_dependencies.txt' exists before use it.
#
# Sample:
#  `python .\sdk\spring\scripts\ci_update_versions.py --s 2.6.1`.
#
# The script must be run at the root of azure-sdk-for-java.

import in_place
import argparse
import os
import re
import time
from log import log

include_update_marker = re.compile(r'\{x-include-update;([^;]+);([^}]+)\}')
version_update_marker = re.compile(r'\{x-version-update;([^;]+);([^}]+)\}')
external_dependency_include_regex = r'(?<=<include>).+?(?=</include>)'
external_dependency_version_regex = r'(?<=<version>).+?(?=</version>)'
SPRING_BOOT_MANAGED_EXTERNAL_DEPENDENCIES_FILE = 'sdk/spring/scripts/spring_boot_{}_managed_external_dependencies.txt'

def get_version_type(match):
    return match.group(2)

def get_module_name(match):
    return match.group(1)

def get_version_update_line(line, match, external_dependency_version_map):
    module_name, version_type = get_module_name(match), get_version_type(match)
    if module_name in external_dependency_version_map and version_type == 'external_dependency':
        new_version = external_dependency_version_map[module_name]
        log.info(module_name + '; new version:' + new_version)
        return re.sub(external_dependency_version_regex, new_version, line)
    else:
        return line

def get_include_update_line(line, match, external_dependency_version_map):
    module_name, version_type = get_module_name(match), get_version_type(match)
    if module_name in external_dependency_version_map and version_type == 'external_dependency':
        new_include_version = module_name + ':[' + external_dependency_version_map[module_name] + ']'
        log.info(module_name + '; new include version:' + new_include_version)
        return re.sub(external_dependency_include_regex, new_include_version, line)
    else:
        return line

def update_versions(external_dependency_version_map, target_file):
    # replace artifact versions in target_file
    with in_place.InPlace(target_file) as file:
        for line in file:
            if line.startswith('#') or not line:
                file.write(line)
            elif version_update_marker.search(line):
                match = version_update_marker.search(line)
                newline = get_version_update_line(line, match, external_dependency_version_map)
                file.write(newline)
            elif include_update_marker.search(line):
                match = include_update_marker.search(line)
                newline = get_include_update_line(line, match, external_dependency_version_map)
                file.write(newline)
            else:
                file.write(line)

def load_version_map_from_file(the_file, version_map):
    with open(the_file) as file:
        for line in file:
            line = line.strip()
            if line.startswith('#') or not line:
                file.write(line)
            else:
                key_value = line.split(';', 1)
                key = key_value[0]
                value = key_value[1]
                version_map[key] = value

def get_spring_boot_managed_external_dependencies_file(spring_boot_version):
    return SPRING_BOOT_MANAGED_EXTERNAL_DEPENDENCIES_FILE.format(spring_boot_version)

def update_versions_all(target_folder, spring_boot_version):
    external_dependency_version_map = {}
    # Read artifact version from dependency_file.
    dependency_file = get_spring_boot_managed_external_dependencies_file(spring_boot_version)
    log.info('external_dependency_file=' + dependency_file)
    load_version_map_from_file(dependency_file, external_dependency_version_map)
    for root, _, files in os.walk(target_folder):
        for file_name in files:
            file_path = root + os.sep + file_name
            if file_name.startswith('pom') and file_name.endswith('.xml'):
                update_versions(external_dependency_version_map, file_path)

def main():
    parser = argparse.ArgumentParser(description='Replace version numbers in poms.')
    parser.add_argument('--target-folder', '-t', type=str, required=False, default="./sdk/spring", help='Set target folder.')
    parser.add_argument('--spring-boot-version', '-s', type=str, required=True)
    parser.add_argument(
        '--log',
        type=str,
        choices=['debug', 'info', 'warn', 'error', 'none'],
        required=False,
        default='info',
        help='Set log level.'
    )
    args = parser.parse_args()
    log.set_log_level(args.log)
    start_time = time.time()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    update_versions_all(args.target_folder, args.spring_boot_version)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time={}'.format(elapsed_time))

if __name__ == '__main__':
    main()
