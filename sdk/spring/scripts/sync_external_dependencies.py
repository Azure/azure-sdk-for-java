############################################################################################################################################
# This script is used to sync 3rd party dependencies from `.\sdk\spring\spring_boot_SPRING_BOOT_VERSION_managed_external_dependencies.txt`
# to `eng/versioning/external_dependencies.txt` and update comments at the beginning of `eng/versioning/external_dependencies.txt`.
#
# How to use this script.
#  1. Get `SPRING_BOOT_VERSION` from https://github.com/spring-projects/spring-boot/tags.
#  2. Make sure file(`.\sdk\spring\spring_boot_${SPRING_BOOT_VERSION}_managed_external_dependencies.txt`) exist. If it doesn't exist, please run
#    `.\sdk\spring\scripts\get_spring_boot_managed_external_dependencies.py` to create that file.
#  3. Run command: `python .\sdk\spring\scripts\sync_external_dependencies.py -b 2.7.0`.
#     Or `python .\sdk\spring\scripts\sync_external_dependencies.py --spring_boot_dependencies_version 2.7.0`.
#  4. Then `eng/versioning/external_dependencies.txt` will be updated.
#
# Please refer to ./README.md to get more information about this script.
############################################################################################################################################

import in_place
import time
import os
import argparse
from version_util import version_greater_than

from log import log
from _constants import SPRING_BOOT_MAJOR_2_VERSION_NAME, SPRING_BOOT_MAJOR_3_VERSION_NAME, get_spring_boot_version_tag_prefix

EXTERNAL_DEPENDENCIES_FILE = 'eng/versioning/external_dependencies.txt'
SKIP_IDS = [
    'org.eclipse.jgit:org.eclipse.jgit'  # Refs: https://github.com/Azure/azure-sdk-for-java/pull/13956/files#r468368271
]


def get_spring_boot_managed_external_dependencies_file_name(spring_boot_version):
    return 'sdk/spring/scripts/spring_boot_{}_managed_external_dependencies.txt'.format(spring_boot_version)


def get_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-b', '--spring_boot_dependencies_version', type = str, required = True)
    parser.add_argument(
        '--log',
        type = str,
        choices = ['debug', 'info', 'warn', 'error', 'none'],
        required = False,
        default = 'info',
        help = 'Set log level.'
    )
    parser.add_argument(
        '-sbmvn',
        '--spring_boot_major_version_number',
        type=str,
        choices=[SPRING_BOOT_MAJOR_2_VERSION_NAME, SPRING_BOOT_MAJOR_3_VERSION_NAME],
        required=True,
        default=SPRING_BOOT_MAJOR_3_VERSION_NAME,
        help='Update the dependencies of Spring Boot major version. The default is ' + SPRING_BOOT_MAJOR_3_VERSION_NAME + '.'
    )
    args = parser.parse_args()
    log.set_log_level(args.log)
    return args


def main():
    start_time = time.time()
    change_to_repo_root_dir()
    args = get_args()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    file_name = get_spring_boot_managed_external_dependencies_file_name(args.spring_boot_dependencies_version)
    sync_external_dependencies(get_spring_boot_version_tag_prefix(args.spring_boot_major_version_number), file_name, EXTERNAL_DEPENDENCIES_FILE)
    update_external_dependencies_comment(args.spring_boot_major_version_number, file_name, EXTERNAL_DEPENDENCIES_FILE)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def change_to_repo_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def sync_external_dependencies(version_prefix, source_file, target_file):
    # Read artifact version from source_file.
    dependency_dict = {}
    with open(source_file) as file:
        for line in file:
            line = line.strip()
            if line.startswith('#') or not line:
                file.write(line)
            else:
                key_value = line.split(';', 1)
                key = key_value[0]
                value = key_value[1]
                dependency_dict[version_prefix + key] = value
    # Write artifact versions into target file.
    with in_place.InPlace(target_file) as file:
        for line in file:
            line = line.strip()
            if line.startswith('#') or not line:
                file.write(line)
            else:
                key, value = line.split(';', 1)
                if key not in SKIP_IDS and key in dependency_dict:
                    value_in_dict = dependency_dict[key]
                    if version_greater_than(value, value_in_dict):
                        log.warn('Version update skipped. key = {}, value = {}, new_value = {}'.format(key, value, value_in_dict))
                        file.write(line)
                    elif version_greater_than(value_in_dict, value):
                        log.info('Version updated. key = {}, value = {}, new_value = {}'.format(key, value, value_in_dict))
                        file.write('{};{}'.format(key, value_in_dict))
                    else:
                        file.write(line)
                else:
                    file.write(line)
            file.write('\n')


def update_external_dependencies_comment(spring_boot_major_version, source_name, target_file):
    if spring_boot_major_version == SPRING_BOOT_MAJOR_2_VERSION_NAME:
        with open(target_file, 'r', encoding='utf-8') as file:
            lines = file.readlines()
            lines[1] = '# make sure the version is same to {}\n'.format(source_name)
            lines[2] = '# If your version is different from {},\n'.format(source_name)
        with open(target_file, 'w', encoding='utf-8') as file:
            file.writelines(lines)


if __name__ == '__main__':
    main()
