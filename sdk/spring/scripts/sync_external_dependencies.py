############################################################################################################################################
# This script is used to sync 3rd party dependencies from `.\sdk\spring\spring_boot_SPRING_BOOT_VERSION_managed_external_dependencies.txt`
# to `eng/versioning/external_dependencies.txt`.
#
# How to use this script.
#  1. Make sure file(`.\sdk\spring\spring_boot_SPRING_BOOT_VERSION_managed_external_dependencies.txt`) exist. If it doesn't exist, please run
#    `.\sdk\spring\scripts\get_spring_boot_managed_external_dependencies.py` to create that file.
#  2. Update `SPRING_BOOT_VERSION` in this script manually.
#  3. Run command, sample: `python .\sdk\spring\scripts\sync_external_dependencies.py -b 2.7.0`.
#     Or `python .\sdk\spring\scripts\sync_external_dependencies.py --spring_boot_dependencies_version 2.7.0`.
#  4. Then `eng/versioning/external_dependencies.txt` will be updated.
#
# Please refer to ./README.md to get more information about this script.
############################################################################################################################################

import in_place
import time
import os
import unittest
import argparse
from itertools import takewhile

from log import log

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
    args = parser.parse_args()
    log.set_log_level(args.log)
    return args


def main():
    start_time = time.time()
    change_to_root_dir()
    args = get_args()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    sync_external_dependencies(get_spring_boot_managed_external_dependencies_file_name(args.spring_boot_dependencies_version), EXTERNAL_DEPENDENCIES_FILE)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def sync_external_dependencies(source_file, target_file):
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
                dependency_dict[key] = value
    # Write artifact versions into target file.
    with in_place.InPlace(target_file) as file:
        for line in file:
            line = line.strip()
            if line.startswith('#') or not line:
                file.write(line)
            else:
                key_value = line.split(';', 1)
                key = key_value[0]
                value = key_value[1]
                if key not in SKIP_IDS and key in dependency_dict:
                    value_in_dict = dependency_dict[key]
                    if version_bigger_than(value, value_in_dict):
                        log.warn('Version update skipped. key = {}, value = {}, new_value = {}'.format(key, value, value_in_dict))
                        file.write(line)
                    elif version_bigger_than(value_in_dict, value):
                        log.info('Version updated. key = {}, value = {}, new_value = {}'.format(key, value, value_in_dict))
                        file.write('{};{}'.format(key, value_in_dict))
                    else:
                        file.write(line)
                else:
                    file.write(line)
            file.write('\n')


def version_bigger_than(version1, version2):
    v1 = version1.split('.')
    v2 = version2.split('.')
    len_1 = len(v1)
    len_2 = len(v2)
    max_len = max(len_1, len_1)
    for i in range(max_len):
        if i < len_1 and i < len_2:
            int_1 = int('0' + ''.join(takewhile(str.isdigit, v1[i])))
            int_2 = int('0' + ''.join(takewhile(str.isdigit, v2[i])))
            if int_1 != int_2:
                return int_1 > int_2
        elif i < len_1:
            return True
        else:
            return False
    return False


class Tests(unittest.TestCase):
    def test_version_bigger_than(self):
        self.assertEqual(version_bigger_than('1', '2'), False)
        self.assertEqual(version_bigger_than('2', '1'), True)
        self.assertEqual(version_bigger_than('1.0', '2'), False)
        self.assertEqual(version_bigger_than('2.0', '1'), True)
        self.assertEqual(version_bigger_than('1.1', '1'), True)
        self.assertEqual(version_bigger_than('1', '1.1'), False)
        self.assertEqual(version_bigger_than('1.0-RELEASE', '1.1'), False)
        self.assertEqual(version_bigger_than('1.1-RELEASE', '1'), True)
        self.assertEqual(version_bigger_than('1.1-RELEASE', '1.0'), True)
        self.assertEqual(version_bigger_than('1.1-RELEASE', '1.0.1'), True)
        self.assertEqual(version_bigger_than('1.1-RELEASE', '1.0.1-RELEASE'), True)
        self.assertEqual(version_bigger_than('1.1-RELEASE', '1.1.1-RELEASE'), False)


if __name__ == '__main__':
    main()
