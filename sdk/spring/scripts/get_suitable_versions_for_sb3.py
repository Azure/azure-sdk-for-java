############################################################################################################################################
# This script is used to create a version file for `sdk/spring/scripts/resolve_version_conflict_for_sb3.py` to resolve conflict versions when sync 3rd party dependencies of branch feature/spring-boot-3.
#
# Some external dependencies could exist in branch feature/spring-boot-3's but not in local `eng/versioning/external_dependencies.txt`, this
# script will add these dependencies in. And will try to use the version defined in
# `.\sdk\spring\spring_boot_SPRING_BOOT_VERSION_managed_external_dependencies.txt`
#
#
# How to use this script.
#  1. Get `SPRING_BOOT_VERSION` from file(`.\sdk\spring\spring_boot_${SPRING_BOOT_VERSION}_managed_external_dependencies.txt`).
#  2. Run command: `python .\sdk\spring\scripts\get_suitable_versions_for_sb3.py -b 3.0.5`.
#     Or `python .\sdk\spring\scripts\get_suitable_versions_for_sb3.py --spring_boot_dependencies_version 3.0.5`.
#  3. Then `sdk\spring\conflict_versions.txt` will be created and may have some logs.
#  4. If there are logs, Judge whether to change or delete dependencies.
#
############################################################################################################################################

import time
import os
import argparse
import requests
from log import log
from version_util import version_greater_than
from replace_util import change_to_repo_root_dir

EXTERNAL_DEPENDENCIES_FILE = 'eng/versioning/external_dependencies.txt'
SB3_EXTERNAL_DEPENDENCIES_FILE_URL = 'https://raw.githubusercontent.com/Azure/azure-sdk-for-java/feature/spring-boot-3/eng/versioning/external_dependencies.txt'
OUTPUT_FILE = 'sdk/spring/scripts/conflict_versions.txt'
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
    resolve_dict = {}
    remove_list = []
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
            v_local = ver_local[k]
            v_remote = ver_remote[k]
            v_sb3 = ver_sb3_managed.get(k, 'N/A')
            if v_sb3 != 'N/A' and v_remote == v_sb3 and version_greater_than(v_remote, v_local):
                resolve_dict[k] = v_remote
            else:
                log.warn("Conflicting version found for [{:<{}}], local is [{:<{}}], but remote is [{:<{}}], the sb3 managed version is [{:<{}}], it will be set as remote version,"
                         " change it in conflict_versions.txt if needed.".format(k, len_id, v_local, len_ver, v_remote, len_ver, v_sb3, len_ver))
                log.warn("")
                resolve_dict[k] = v_remote
    if local_missing_set:
        local_missing_set.sort()
        len_ver, len_id = cal_len_for_logging_format(local_missing_set, ver_sb3_managed, ver_remote)
        for k in local_missing_set:
            v_remote = ver_remote[k]
            v_sb3 = ver_sb3_managed.get(k, 'N/A')
            if v_sb3 != 'N/A' and v_remote == v_sb3:
                resolve_dict[k] = v_remote
            else:
                log.warn("Entry existing in remote but not local found for [{:<{}}], remote version is [{:<{}}], the sb3 managed version is [{:<{}}], it will be set as remote version,"
                         " change it in conflict_versions.txt if needed.".format(k, len_id, v_remote, len_ver, v_sb3, len_ver))
                log.warn("")
                resolve_dict[k] = v_remote
    if remote_missing_set:
        remote_missing_set.sort()
        len_ver, len_id = cal_len_for_logging_format(remote_missing_set, ver_local, ver_remote)
        for k in remote_missing_set:
            log.warn("Entry existing in local but not remote found for [{:<{}}], it will be removed, if want to keep, delete the line in conflict_versions.txt".format(k, len_id))
            remove_list.append(k)
        log.warn("")
    return resolve_dict, remove_list


def output_versions_file(resolve_dict, remove_list):
    if resolve_dict or remove_list:
        output_file = open(OUTPUT_FILE, 'w')
        for key, value in sorted(resolve_dict.items()):
            output_file.write('{};{}\n'.format(key, value))
        for k in sorted(remove_list):
            output_file.write("#remove;{}\n".format(k))
        output_file.close()
    else:
        log.warn("Nothing conflict!")
        return


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
    resolve_dict, remove_list = three_way_merge(dump_versions_from_local_external_deps(), dump_versions_from_sb3_branch(),
                                                dump_versions_from_sb3_managed_external_deps(args.spring_boot_dependencies_version))
    output_versions_file(resolve_dict, remove_list)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


if __name__ == '__main__':
    main()
