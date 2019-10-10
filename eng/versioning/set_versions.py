# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Use case: Append the build qualifier onto the existing version in such a way that the
# resulting version string is still in semver format. This will be utilized by the build
# system to modify the version string to produce nightly DevOps builds.
#
# It's worth noting that this only manipulates the version in the appropriate version_*.txt file.
# which has the format <groupId>:<artifactId>;<dependencyVersion>;<currentVersion>.
# Selecting dependency for the update type will only update the dependency version. Similarly,
# selecting current will only update the current. Selecting both will update both the dependency
# and the current versions.
#
#
#    python utilities/set_versions.py --ut [current|dependency|all] --bt [client|data|management] --bq <BuildQualifierString>
#
# The script must be run at the root of azure-sdk-for-java.

import argparse
from datetime import timedelta
from enum import Enum
import os
import re
import time

version_update_marker = re.compile(r'\{x-version-update;([^;]+);([^}]+)\}')
# regex for the version string is suggested one directly from semver.org which is
# used to verify that the resulting string is in the correct format after the build_qualifier
# is appended.
# https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
version_regex = re.compile(r'^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$')

class UpdateType(Enum):
    current = 'current'
    dependency = 'dependency'
    all = 'all'

    # defining string is necessary to get ArgumentParser's help output to produce
    # human readable values of UpdateType
    def __str__(self):
        return self.value

class BuildType(Enum):
    client = 'client'
    data = 'data'
    management = 'management'
    
    # defining string is necessary to get ArgumentParser's help output to produce
    # human readable values of BuildType
    def __str__(self):
        return self.value

class CodeModule:
    def __init__(self, module_str):
        # For library versions there will be up to 3 items resulting from the split
        # which will be module name, dependency version and current version.
        items = module_str.split(';')
        if len(items) == 3: 
            self.name = items[0]
            self.dependency = items[1]
            self.current = items[2].strip()
        else: 
            raise ValueError('unable to parse module string: ' + module_str)

    def __str__(self):
        return self.name + ';' + self.dependency + ';' + self.current + '\n'

def update_versions_file(update_type, build_type, build_qualifier):

    version_file = os.path.normpath('eng/versioning/version_' + build_type.name + '.txt')
    print('version_file=' + version_file)

    newlines = []
    with open(version_file) as f:
        for raw_line in f:
            stripped_line = raw_line.strip()
            if not stripped_line or stripped_line.startswith('#'):
                newlines.append(raw_line)
            else:
                module = CodeModule(stripped_line)
                if update_type == UpdateType.current or update_type == UpdateType.all:
                    if '-' in module.current:
                        module.current += build_qualifier
                    else:
                        module.current += '-' + build_qualifier
                    match = version_regex.match(module.current)
                    if not match:
                        raise ValueError('{}\'s current version + build qualifier {} is not a valid semver version'.format(module.name, module.current + build_qualifier))
                if update_type == UpdateType.dependency or update_type == UpdateType.all:
                    if '-' in module.dependency:
                        module.dependency += build_qualifier
                    else:
                        module.dependency += '-' + build_qualifier
                    match = version_regex.match(module.dependency)
                    if not match:
                        raise ValueError('{}\'s dependency version + build qualifier {} is not a valid semver version'.format(module.name, module.dependency + build_qualifier))
                newlines.append(str(module))

    with open(version_file, 'w') as f:
        for line in newlines:
            f.write(line)
    
def main():
    parser = argparse.ArgumentParser(description='set version numbers in the appropriate version text file')
    parser.add_argument('--update-type', '--ut', type=UpdateType, choices=list(UpdateType))
    parser.add_argument('--build-type', '--bt', type=BuildType, choices=list(BuildType))
    parser.add_argument('--build-qualifier', '--bq', help='build qualifier to append onto the version string.')
    args = parser.parse_args()
    if (args.build_type == BuildType.management):
        raise ValueError('{} is not currently supported.'.format(BuildType.management.name))
    start_time = time.time()
    update_versions_file(args.update_type, args.build_type, args.build_qualifier)
    elapsed_time = time.time() - start_time
    print('elapsed_time={}'.format(elapsed_time))
    print('Total time for replacement: {}'.format(str(timedelta(seconds=elapsed_time))))

if __name__ == '__main__':
    main()