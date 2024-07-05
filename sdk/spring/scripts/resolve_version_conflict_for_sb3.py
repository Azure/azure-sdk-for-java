############################################################################################################################################
# This script is used to resolve conflict versions when sync 3rd party dependencies from `.\sdk\spring\spring_boot_SPRING_BOOT_VERSION_managed_external_dependencies.txt`
# to `eng/versioning/external_dependencies.txt`.
#
# How to use this script.
#  1. Make sure file(`.\sdk\spring\conflict_versions.txt`) exist. If it doesn't exist, please refer to `.\sdk\spring\scripts\get_suitable_versions_for_sb3.py` for creating.
#  2. Run command: `python .\sdk\spring\scripts\resolve_version_conflict_for_sb3.py`.
#  3. Then `eng/versioning/external_dependencies.txt` will be updated.
#
############################################################################################################################################

import in_place
import time
import os
from log import log
from replace_util import change_to_repo_root_dir

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


def sync_external_dependencies(source_file, target_file):
    # Read artifact version from source_file.
    dependency_dict = {}
    remove_list = []
    if os.path.exists(source_file):
        with open(source_file) as file:
            for line in file:
                if line.startswith('#remove'):
                    remove_list = line.strip().split(';', 1)
                else:
                    line = line.strip()
                    key, val = line.split(';', 1)
                    dependency_dict[key] = val
        # Write artifact versions into target file.
        with in_place.InPlace(target_file) as file:
            for line in file:
                line = line.strip()
                if line.startswith('#') or not line:
                    file.write(line)
                    file.write('\n')
                else:
                    key, val = line.split(';', 1)
                    if key not in SKIP_IDS and key in dependency_dict:
                        file.write('{};{}\n'.format(key, dependency_dict[key]))
                        log.info("update artifact:{} to {}".format(key, dependency_dict[key]))
                        del dependency_dict[key]
                    elif key not in SKIP_IDS and key in remove_list:
                        log.warn("remove artifact:{}".format(key))
                    else:
                        file.write(line)
                        file.write('\n')
            if dependency_dict:
                file.write("\n# Spring Boot 3 dependency versions\n")
                for key in dependency_dict:
                    file.write('{};{}\n'.format(key, dependency_dict[key]))
    else:
        log.warn('No conflict versions!')


if __name__ == '__main__':
    main()
