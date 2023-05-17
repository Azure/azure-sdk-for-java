############################################################################################################################################
# This script is used to sync 3rd party dependencies from `.\sdk\spring\spring_boot_SPRING_BOOT_VERSION_managed_external_dependencies.txt`
# to `eng/versioning/external_dependencies.txt`.
#
# How to use this script.
#  1. Get `SPRING_BOOT_VERSION` from https://github.com/spring-projects/spring-boot/tags.
#  2. Make sure file(`.\sdk\spring\spring_boot_${SPRING_BOOT_VERSION}_managed_external_dependencies.txt`) exist. If it doesn't exist, please run
#    `.\sdk\spring\scripts\get_spring_boot_managed_external_dependencies.py` to create that file.
#  3. Run command: `python .\sdk\spring\scripts\resolve_version_conflict.py`.
#  4. Then `eng/versioning/external_dependencies.txt` will be updated.
#
# Please refer to ./README.md to get more information about this script.
############################################################################################################################################

import in_place
import time
import os

from log import log

EXTERNAL_DEPENDENCIES_FILE = 'eng/versioning/external_dependencies.txt'
CONFLICT_VERSIONS_FILE = 'sdk/spring/scripts/conflict_versions.txt'
SKIP_IDS = [
    'org.eclipse.jgit:org.eclipse.jgit'  # Refs: https://github.com/Azure/azure-sdk-for-java/pull/13956/files#r468368271
]


def main():
    start_time = time.time()
    change_to_repo_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    sync_external_dependencies(CONFLICT_VERSIONS_FILE, EXTERNAL_DEPENDENCIES_FILE)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def change_to_repo_root_dir():
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
                key, val = line.split(';', 1)
                # key_value = line.split(';', 1)
                # key = key_value[0]
                # value = key_value[1]
                dependency_dict[key] = val
    # Write artifact versions into target file.
    with in_place.InPlace(target_file) as file:
        for line in file:
            line = line.strip()
            if line.startswith('#') or not line:
                file.write(line)
            else:
                key, val = line.split(';', 1)
                if key not in SKIP_IDS and key in dependency_dict:
                    file.write('{};{}'.format(key, dependency_dict[key]))
                else:
                    file.write(line)
            file.write('\n')


def print_dict(d):
    for key, value in d.items():
        print('key = {}, value = {}.'.format(key, value))


if __name__ == '__main__':
    main()
