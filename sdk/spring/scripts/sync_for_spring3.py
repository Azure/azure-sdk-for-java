############################################################################################################################################
# This script is used to sync 3rd party dependencies of branch feature/spring-boot-3 to `eng/versioning/external_dependencies.txt`.
#
# Some external dependencies could exist in branch feature/spring-boot-3's but not in local `eng/versioning/external_dependencies.txt`, this
# script will add these dependencies in. And will try to use the version defined in
# `.\sdk\spring\spring_boot_SPRING_BOOT_VERSION_managed_external_dependencies.txt`
#
#
# How to use this script.
#  1. Get `SPRING_BOOT_VERSION` from https://github.com/spring-projects/spring-boot/tags.
#  2. Make sure file(`.\sdk\spring\spring_boot_${SPRING_BOOT_VERSION}_managed_external_dependencies.txt`) exist. If it doesn't exist, please run
#    `.\sdk\spring\scripts\get_spring_boot_managed_external_dependencies.py` to create that file.
#  3. Run command: `python .\sdk\spring\scripts\sync_for_spring3.py -b 3.0.5`.
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
import requests
from itertools import takewhile
from packaging.version import parse
from log import log

EXTERNAL_DEPENDENCIES_FILE = 'eng/versioning/external_dependencies.txt'
SB3_EXTERNAL_DEPENDENCIES_FILE_URL = 'https://raw.githubusercontent.com/Azure/azure-sdk-for-java/feature/spring-boot-3/eng/versioning/external_dependencies.txt'
SKIP_IDS = [
    'org.eclipse.jgit:org.eclipse.jgit'  # Refs: https://github.com/Azure/azure-sdk-for-java/pull/13956/files#r468368271
]


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


def change_to_repo_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def dump_versions_from_sb3_branch():
    response = requests.get(SB3_EXTERNAL_DEPENDENCIES_FILE_URL)
    response.raise_for_status()  # ensure we notice bad responses
    return dump_version(response.iter_lines(decode_unicode = True))


def dump_versions_from_sb3_managed_external_deps(sb_ver):
    managed_ver_file = 'sdk/spring/scripts/spring_boot_{}_managed_external_dependencies.txt'.format(sb_ver)
    with open(managed_ver_file, "r") as f:
        return dump_version(iter(f.readlines()))


def dump_versions_from_local_external_deps():
    with open(EXTERNAL_DEPENDENCIES_FILE, "r") as f:
        return dump_version(iter(f.readlines()))


def dump_version(lines_iter):
    dependency_dict = {}
    for line in lines_iter:
        line = line.strip()
        if line and not line.startswith('#'):
            some = line.split(';', 1)
            if len(some) < 2:
                print(line)
            key, val = line.split(';', 1)
            dependency_dict[key] = val
    return dependency_dict


def three_way_merge(ver_local, ver_remote, ver_sb3_managed):
    conflicting_set = []
    local_missing_set = []
    remote_missing_set = []
    total_keys = ver_remote.keys() | ver_local.keys()
    for k in total_keys:
        if k not in ver_local:
            local_missing_set.append(k)
        elif k not in ver_remote:
            remote_missing_set.append(k)
        elif ver_remote[k] != ver_local[k]:
            conflicting_set.append(k)
    if conflicting_set:
        conflicting_set.sort()
        len_ver, len_id = cal_len_for_logging_format(conflicting_set, ver_local, ver_remote)
        for k in conflicting_set:
            log.warn("Conflicting version found for [{:<{}}], local is [{:<{}}], but remote is [{:<{}}], the sb3 managed version is [{:<{}}]"
                     .format(k, len_id, ver_local[k], len_ver, ver_remote[k], len_ver, ver_sb3_managed.get(k, 'N/A'), len_ver))
        log.warn("")
    if local_missing_set:
        local_missing_set.sort()
        len_ver, len_id = cal_len_for_logging_format(local_missing_set, ver_sb3_managed, ver_remote)
        for k in local_missing_set:
            log.warn("Entry existing in remote but not local found for [{:<{}}], remote version is [{:<{}}], the sb3 managed version is [{:<{}}]"
                     .format(k, len_id, ver_remote[k], len_ver, ver_sb3_managed.get(k, 'N/A'), len_ver))
        log.warn("")
    for k in remote_missing_set:
        remote_missing_set.sort()
        len_ver, len_id = cal_len_for_logging_format(remote_missing_set, ver_local, ver_remote)
        for k in remote_missing_set:
            log.warn("Entry existing in local but not remote found for [{:<{}}]".format(k, len_id))
        log.warn("")
    return local_missing_set, conflicting_set, remote_missing_set


def cal_len_for_logging_format(k_set, ver_local, ver_remote):
    max_len_id = 0
    max_len_ver = 0
    for k in k_set:
        max_len_id = max(len(k), max_len_id)
        max_len_ver = max(len(ver_local.get(k, "")), len(ver_remote.get(k, "")), max_len_ver)
    return max_len_ver, max_len_id


def main():
    start_time = time.time()
    change_to_repo_root_dir()
    args = get_args()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    ver_remote_external_deps = dump_versions_from_sb3_branch()
    ver_local_external_deps = dump_versions_from_local_external_deps()
    ver_local_sb3_managed_deps = dump_versions_from_sb3_managed_external_deps(args.spring_boot_dependencies_version)
    local_missing_set, conflicting_set, remote_missing_set = three_way_merge(ver_local_external_deps, ver_remote_external_deps, ver_local_sb3_managed_deps)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


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


def version_bigger_than(source_version, target_version):
    # Format version with jre ('11.2.3.jre17')
    if source_version.find('.jre'):
        source_version = source_version.partition('.jre')[0]
    if target_version.find('.jre'):
        target_version = target_version.partition('.jre')[0]
    # Format version with Final ('4.1.89.Final')
    if source_version.find('.Final'):
        source_version = source_version.partition('.Final')[0]
    if target_version.find('.Final'):
        target_version = target_version.partition('.Final')[0]
    # Format version with RELEASE ('6.2.0.RELEASE')
    if source_version.find('.RELEASE'):
        source_version = source_version.partition('.RELEASE')[0]
    if target_version.find('.RELEASE'):
        target_version = target_version.partition('.RELEASE')[0]
    # Format version with v... ('9.4.50.v20221201')
    if source_version.find('.v'):
        source_version = source_version.partition('.v')[0]
    if target_version.find('.v'):
        target_version = target_version.partition('.v')[0]
    sv = parse(source_version)
    tv = parse(target_version)
    if sv == tv:
        return True
    elif sv < tv:
        # Spring milestone comparison ('3.0.0-M4', '3.0.0-M5')
        if is_invalid_version(source_version) and is_invalid_version(target_version):
            return False
        # ('1.0-RELEASE','1.1') ('1.1-RELEASE','1') ('1.1-RELEASE','1.0')
        if is_invalid_version(source_version) or is_invalid_version(target_version):
            return special_version_bigger_than(source_version, target_version)
    else:
        # Spring RC version should be bigger than milestone version ('3.0.0-RC1', '3.0.0-M5')
        if not is_invalid_version(source_version) and sv.is_prerelease and is_invalid_version(target_version):
            return True
        # ('1.1-RELEASE','1.0.1-RELEASE')
        if is_invalid_version(source_version) and is_invalid_version(target_version):
            return True
        # ('2.7.4', '3.0.0-M5')
        if is_invalid_version(source_version) or is_invalid_version(target_version):
            return special_version_bigger_than(source_version, target_version)
    if sv.major != tv.major:
        return sv.major > tv.major
    elif sv.major == tv.major and sv.minor != tv.minor:
        return sv.minor > tv.minor
    elif sv.major == tv.major and sv.minor == tv.minor and sv.micro != tv.micro:
        return sv.micro >= tv.micro
    return sv > tv


def is_invalid_version(verify_version):
    version_dict = vars(parse(verify_version))
    return type(version_dict['_version']) == str


def special_version_bigger_than(version1, version2):
    v1 = version1.split('.')
    v2 = version2.split('.')
    len_1 = len(v1)
    len_2 = len(v2)
    max_len = max(len_1, len_2)
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
        self.assertEqual(version_bigger_than('2.7.4', '3.0.0-M5'), False)
        self.assertEqual(version_bigger_than('3.0.0-M5', '2.7.4'), True)
        self.assertEqual(version_bigger_than('3.0.0-M4', '3.0.0-M5'), False)
        self.assertEqual(version_bigger_than('3.0.0-M5', '3.0.0-M4'), True)
        self.assertEqual(version_bigger_than('3.0.0-M5', '3.0.0-RC1'), False)
        self.assertEqual(version_bigger_than('3.0.0-RC1', '3.0.0-M5'), True)
        self.assertEqual(version_bigger_than('3.0.0-RC1', '3.0.0-RC2'), False)
        self.assertEqual(version_bigger_than('3.0.0-RC2', '3.0.0-RC1'), True)
        self.assertEqual(version_bigger_than('11.2.3.jre17', '10.2.3.jre8'), True)
        self.assertEqual(version_bigger_than('4.1.89.Final', '4.1.87.Final'), True)
        self.assertEqual(version_bigger_than('6.2.0.RELEASE', '6.2.2.RELEASE'), False)
        self.assertEqual(version_bigger_than('9.4.50.v20221201', '11.0.13'), False)


if __name__ == '__main__':
    main()
