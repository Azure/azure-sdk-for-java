# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.4 or higher is required to run this script.

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
#    python utilities/set_versions.py --ut [library|external_dependency|all] --bt [client|data|management] --bq <BuildQualifierString> --ar <artifactId>
#
# The script must be run at the root of azure-sdk-for-java.

import argparse
from datetime import timedelta
import os
import re
import time
from utils import BuildType
from utils import CodeModule
from utils import UpdateType
from utils import version_regex_str_with_anchor

# The regex string we want should be the anchored one since the entire string is what's being matched
version_regex = re.compile(version_regex_str_with_anchor)

def update_versions_file(update_type, build_type, build_qualifier, artifact_id):

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
                if not artifact_id or module.artifact_id == artifact_id:
                    if update_type == UpdateType.library or update_type == UpdateType.all:
                        if '-' in module.current:
                            module.current += "." + build_qualifier
                        else:
                            module.current += '-' + build_qualifier
                        match = version_regex.match(module.current)
                        if not match:
                            raise ValueError('{}\'s current version + build qualifier {} is not a valid semver version'.format(module.name, module.current + build_qualifier))
                    if update_type == UpdateType.external_dependency or update_type == UpdateType.all:
                        if '-' in module.dependency:
                            module.dependency += "." + build_qualifier
                        else:
                            module.dependency += '-' + build_qualifier
                        match = version_regex.match(module.dependency)
                        if not match:
                            raise ValueError('{}\'s dependency version + build qualifier {} is not a valid semver version'.format(module.name, module.dependency + build_qualifier))
                newlines.append(module.string_for_version_file())

    with open(version_file, 'w') as f:
        for line in newlines:
            f.write(line)
    
def main():
    parser = argparse.ArgumentParser(description='set version numbers in the appropriate version text file')
    parser.add_argument('--update-type', '--ut', type=UpdateType, choices=list(UpdateType))
    parser.add_argument('--build-type', '--bt', type=BuildType, choices=list(BuildType))
    parser.add_argument('--build-qualifier', '--bq', help='build qualifier to append onto the version string.')
    parser.add_argument('--artifact-id', '--ar', help='artifactId to target.')
    args = parser.parse_args()
    if (args.build_type == BuildType.management):
        raise ValueError('{} is not currently supported.'.format(BuildType.management.name))
    start_time = time.time()
    update_versions_file(args.update_type, args.build_type, args.build_qualifier, args.artifact_id)
    elapsed_time = time.time() - start_time
    print('elapsed_time={}'.format(elapsed_time))
    print('Total time for replacement: {}'.format(str(timedelta(seconds=elapsed_time))))

if __name__ == '__main__':
    main()