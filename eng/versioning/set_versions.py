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
# Use case: increment the version of a given artifact in the approprate version_[client|data|management].txt file
#
#    python eng/versioning/set_versions.py --bt [client|data|management] --increment-version --artifact-id <artifactId>
# For example: To update increment the version of azure-core
#    python eng/versioning/update_versions.py --bt client --iv -ar azure-core
#
# Use case: verify the version of a given artifact in the approprate version_[client|data|management].txt file
#
#    python eng/versioning/set_versions.py --bt [client|data|management] --verify-version --artifact-id <artifactId>
# For example: To update increment the version of azure-core
#    python eng/versioning/update_versions.py --bt client --vv -ar azure-core
#
# The script must be run at the root of azure-sdk-for-java.

import argparse
from datetime import timedelta
import os
import re
import sys
import time
from utils import BuildType
from utils import CodeModule
from utils import UpdateType
from utils import version_regex_str_with_names_anchored
from utils import prerelease_version_regex_with_name

# The regex string we want should be the anchored one since the entire string is what's being matched
version_regex_named = re.compile(version_regex_str_with_names_anchored)
prerelease_regex_named = re.compile(prerelease_version_regex_with_name)

def update_versions_file_for_nightly_devops(update_type, build_type, build_qualifier, artifact_id):

    version_file = os.path.normpath('eng/versioning/version_' + build_type.name + '.txt')
    print('version_file=' + version_file)

    newlines = []
    with open(version_file, encoding='utf-8') as f:
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
                        match = version_regex_named.match(module.current)
                        if not match:
                            raise ValueError('{}\'s current version + build qualifier {} is not a valid semver version'.format(module.name, module.current + build_qualifier))
                    if update_type == UpdateType.external_dependency or update_type == UpdateType.all:
                        if '-' in module.dependency:
                            module.dependency += "." + build_qualifier
                        else:
                            module.dependency += '-' + build_qualifier
                        match = version_regex_named.match(module.dependency)
                        if not match:
                            raise ValueError('{}\'s dependency version + build qualifier {} is not a valid semver version'.format(module.name, module.dependency + build_qualifier))
                newlines.append(module.string_for_version_file())

    with open(version_file, 'w', encoding='utf-8') as f:
        for line in newlines:
            f.write(line)

# Prep the appropriate version file for source 
def prep_version_file_for_source_testing(build_type):

    version_file = os.path.normpath('eng/versioning/version_' + build_type.name + '.txt')
    print('version_file=' + version_file)
    file_changed = False

    newlines = []
    with open(version_file, encoding='utf-8') as f:
        for raw_line in f:
            stripped_line = raw_line.strip()
            if not stripped_line or stripped_line.startswith('#'):
                newlines.append(raw_line)
            else:
                module = CodeModule(stripped_line)
                if not module.current == module.dependency:
                    module.dependency = module.current
                    file_changed = True
                newlines.append(module.string_for_version_file())

    with open(version_file, 'w', encoding='utf-8') as f:
        for line in newlines:
            f.write(line)
    
    return file_changed

# given a build type and artifact id
def increment_version_for_artifact(build_type, artifact_id):

    if not build_type:
        raise ValueError('build_type cannot be empty.')

    if not artifact_id:
        raise ValueError('artifact_id cannot be empty.')

    version_file = os.path.normpath('eng/versioning/version_' + build_type.name + '.txt')
    print('version_file=' + version_file)

    artifact_found = False
    newlines = []
    with open(version_file, encoding='utf-8') as f:
        for raw_line in f:
            stripped_line = raw_line.strip()
            if not stripped_line or stripped_line.startswith('#'):
                newlines.append(raw_line)
            else:
                module = CodeModule(stripped_line)
                # Tick up the version here. If the version is already a pre-release
                # version then just increment the revision. Otherwise increment the
                # minor version, zero the patch and add "-beta.1" to the end
                # https://github.com/Azure/azure-sdk/blob/master/docs/policies/releases.md#java
                if module.artifact_id == artifact_id:
                    artifact_found = True
                    vmatch = version_regex_named.match(module.current)
                    if (vmatch.group('prerelease') is not None):
                        prever = prerelease_regex_named.match(vmatch.group('prerelease'))
                        rev = int(prever.group('revision'))
                        rev += 1
                        new_version = '{}.{}.{}-beta.{}'.format(vmatch.group('major'), vmatch.group('minor'), vmatch.group('patch'), str(rev))
                    else:
                        minor = int(vmatch.group('minor'))
                        minor += 1
                        new_version = '{}.{}.{}-beta.1'.format(vmatch.group('major'), minor, 0)
                    print('artifact_id {}, previous version={}, new current version={}'.format(artifact_id, module.current, new_version))
                    module.current = new_version
                newlines.append(module.string_for_version_file())

    if not artifact_found:
       raise ValueError('artifact_id ({}) was not found in version file {}'.format(artifact_id, version_file)) 

    with open(version_file, 'w', encoding='utf-8') as f:
        for line in newlines:
            f.write(line)

# Verify that the current version of an artifact matches our versioning scheme. This is meant to be called
# as part of the release pipeline for a given artifact to verify that we don't accidentally release a version
# that doesn't match our versioning scheme
def verify_current_version_of_artifact(build_type, artifact_id):
    if not build_type:
        raise ValueError('build_type cannot be empty.')

    if not artifact_id:
        raise ValueError('artifact_id cannot be empty.')

    version_file = os.path.normpath('eng/versioning/version_' + build_type.name + '.txt')
    print('version_file=' + version_file)

    artifact_found = False
    with open(version_file, encoding='utf-8') as f:
        for raw_line in f:
            stripped_line = raw_line.strip()
            if not stripped_line or stripped_line.startswith('#'):
                continue
            else:
                module = CodeModule(stripped_line)
                # verify the current version of the artifact matches our version schema which is one
                # of the following:
                # <major>.<minor>.<patch/hotfix>
                # <major>.<minor>.<patch/hotfix>-beta.<prerelease>
                if module.artifact_id == artifact_id:
                    artifact_found = True
                    vmatch = version_regex_named.match(module.current)
                    temp_ver = '{}.{}.{}'.format(vmatch.group('major'), vmatch.group('minor'), vmatch.group('patch'))
                    # we should never have buildmetadata in our versioning scheme
                    if vmatch.group('buildmetadata') is not None:
                        raise ValueError('artifact ({}) version ({}) in version file ({}) is not a correct version to release. buildmetadata is set and should never be {}'.format(artifact_id, module.current, version_file, vmatch.group('buildmetadata'))) 

                    # reconstruct the version from the semver pieces and it should match exactly the current
                    # version in the module
                    # If there's a pre-release version it should be beta.X
                    if vmatch.group('prerelease') is not None:
                        prerel = vmatch.group('prerelease')

                        if prerelease_regex_named.match(prerel) is None:
                            raise ValueError('artifact ({}) version ({}) in version file ({}) is not a correct version to release. The accepted prerelease tag is (beta.X) and the current prerelease tag is ({})'.format(artifact_id, module.current, version_file, prerel))

                        prever = prerelease_regex_named.match(prerel)
                        rev = int(prever.group('revision'))
                        temp_ver = '{}-beta.{}'.format(temp_ver, str(rev))

                    # last but not least, for sanity verify that the version constructed from the
                    # semver pieces matches module's current version
                    if module.current != temp_ver:
                        raise ValueError('artifact ({}) version ({}) in version file ({}) does not match the version constructed from the semver pieces ({})'.format(artifact_id, module.current, version_file, temp_ver)) 

                    print('The version {} for artifact_id {} looks good!'.format(module.name, module.current))
                    

    if not artifact_found:
       raise ValueError('artifact_id ({}) was not found in version file {}'.format(artifact_id, version_file))

def main():
    parser = argparse.ArgumentParser(description='set version numbers in the appropriate version text file')
    parser.add_argument('--update-type', '--ut', type=UpdateType, choices=list(UpdateType))
    parser.add_argument('--build-type', '--bt', type=BuildType, choices=list(BuildType))
    parser.add_argument('--build-qualifier', '--bq', help='build qualifier to append onto the version string.')
    parser.add_argument('--artifact-id', '--ar', help='artifactId to target.')
    parser.add_argument('--prep-source-testing', '--pst', action='store_true', help='prep the version file for source testing')
    parser.add_argument('--increment-version', '--iv', action='store_true', help='increment the version for a given artifact')
    parser.add_argument('--verify-version', '--vv', action='store_true', help='verify the version for a given artifact')
    args = parser.parse_args()
    if (args.build_type == BuildType.management):
        raise ValueError('{} is not currently supported.'.format(BuildType.management.name))
    start_time = time.time()
    file_changed = False
    if (args.prep_source_testing):
        file_changed = prep_version_file_for_source_testing(args.build_type)
    elif (args.increment_version):
        increment_version_for_artifact(args.build_type, args.artifact_id)
    elif (args.verify_version):
        verify_current_version_of_artifact(args.build_type, args.artifact_id)
    else:
        update_versions_file_for_nightly_devops(args.update_type, args.build_type, args.build_qualifier, args.artifact_id)
    elapsed_time = time.time() - start_time
    print('elapsed_time={}'.format(elapsed_time))
    print('Total time for replacement: {}'.format(str(timedelta(seconds=elapsed_time))))
    # if the file changed flag is set, which only happens through a call to prep_version_file_for_source_testing,
    # then exit with a unique code that allows us to know that something changed.
    if (file_changed):
        sys.exit(5678)

if __name__ == '__main__':
    main()