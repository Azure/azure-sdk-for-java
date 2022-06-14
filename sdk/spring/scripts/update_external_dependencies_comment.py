############################################################################################################################################
# This script is used to update comments at the beginning of `eng/versioning/external_dependencies.txt`.
#
# How to use this script.
#  1. Get `SPRING_BOOT_VERSION` from (.\sdk\spring\spring_boot_${SPRING_BOOT_VERSION}_managed_external_dependencies.txt) filename.
#  2. Run command: `python .\sdk\spring\scripts\update_external_dependencies_comment.py -b 2.7.0`.
#     Or `python .\sdk\spring\scripts\sync_external_dependencies.py --spring_boot_dependencies_version 2.7.0`.
#  3. Then `eng/versioning/external_dependencies.txt` will be updated.
#
# Please refer to ./README.md to get more information about this script.
############################################################################################################################################

import time
import os
import argparse

from log import log

EXTERNAL_DEPENDENCIES_FILE = 'eng/versioning/external_dependencies.txt'


def get_spring_boot_managed_external_dependencies_file_name(spring_boot_version):
    return 'sdk/spring/scripts/spring_boot_{}_managed_external_dependencies.txt'.format(spring_boot_version)


def get_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-b', '--spring_boot_dependencies_version', type = str, required = True)
    args = parser.parse_args()
    return args


def update_external_dependencies_comment(source_name, target_file):
    with open(target_file, 'r', encoding = 'utf-8') as file:
        lines = file.readlines()
        lines[1] = '# make sure the version is same to {}\n'.format(source_name)
        lines[2] = '# If your version is different from {},\n'.format(source_name)
    with open(target_file, 'w', encoding = 'utf-8') as file:
        file.writelines(lines)


def main():
    start_time = time.time()
    change_to_repo_root_dir()
    args = get_args()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    file_name = get_spring_boot_managed_external_dependencies_file_name(args.spring_boot_dependencies_version)
    update_external_dependencies_comment(file_name, EXTERNAL_DEPENDENCIES_FILE)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def change_to_repo_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


if __name__ == '__main__':
    main()
