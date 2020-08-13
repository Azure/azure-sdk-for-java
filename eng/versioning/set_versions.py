# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.4 or higher is required to run this script.

# Nobody outside of the EngSys team should actually be using this script which is primarily
# used for version verification and manipulation in the pipeline automation.
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
#    python eng/versioning/set_versions.py --ut [library|external_dependency|all] --bt [client|data|management] --bq <BuildQualifierString> --ar <artifactId>
#
# Use case: increment the version of a given artifact in the approprate version_[client|data|management].txt file
#
#    python eng/versioning/set_versions.py --bt [client|data|management] --increment-version --artifact-id <artifactId>
# For example: To increment the version of azure-core
#    python eng/versioning/set_versions.py --bt client --iv --ar azure-core
#
# Use case: verify the version of a given artifact in the approprate version_[client|data|management].txt file
#
#    python eng/versioning/set_versions.py --bt [client|data|management] --verify-version --artifact-id <artifactId>
# For example: To verify the version of azure-core
#    python eng/versioning/set_versions.py --bt client --vv --ar azure-core
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
from utils import prerelease_data_version_regex
from utils import prerelease_version_regex_with_name

# some things that should not be updated for devops builds, in the case where everything is being updated in one call
items_we_should_not_update = ['com.azure:azure-sdk-all', 'com.azure:azure-sdk-parent', 'com.azure:azure-client-sdk-parent', 'azure-data-sdk-parent']

# The regex string we want should be the anchored one since the entire string is what's being matched
version_regex_named = re.compile(version_regex_str_with_names_anchored)
prerelease_regex_named = re.compile(prerelease_version_regex_with_name)
prerelease_data_regex = re.compile(prerelease_data_version_regex)

# Update packages (excluding unreleased dependencies and packages which already
# have a dev version set) to use a "zero dev version" (e.g. dev.20201225.0).
# This ensures that packages built in pipelines who have unreleased dependencies
# that are built in other pipelines can successfully fall back to a source build
# of the unreleased dependency package in the monorepo if the unreleased
# dependency has not been published to the dev feed yet. This makes it so
# nightly pipelines do not have to coordinate to publish packages to the dev
# feed in dependency order.
# This function assumes that the version file has already been updated with dev
# versions for the appropriate target packages.
def set_dev_zero_version(build_type, build_qualifier):
    version_file = os.path.normpath('eng/versioning/version_' + build_type.name + '.txt')
    print('version_file=' + version_file)

    # Assuming a build qualifier of the form: "dev.20200204.123"
    # Converts "dev.20200204.123" -> "dev.20200204.0"
    zero_qualifier = build_qualifier[:build_qualifier.rfind('.') + 1] + '0'

    newlines = []
    with open(version_file, encoding='utf8') as f:
        for raw_line in f:
            stripped_line = raw_line.strip()

            if not stripped_line or stripped_line.startswith('#'):
                newlines.append(raw_line)
                continue

            module = CodeModule(stripped_line)

            if module.name in items_we_should_not_update:
                newlines.append(module.string_for_version_file())
                continue

            if module.name.startswith('unreleased_') or module.name.startswith('beta_'):
                newlines.append(module.string_for_version_file())
                continue

            if hasattr(module, 'current'):

                if 'dev' in module.current:
                    newlines.append(module.string_for_version_file())
                    continue

                set_both = module.current == module.dependency

                if '-' in module.current:
                    # if the module is 1.2.3-beta.x, strip off everything after the '-' and add the qualifier
                    module.current = module.current[:module.current.rfind('-') + 1] + zero_qualifier
                else:
                    # if the module is a GA version 1.2.3, add '-' and the qualifier
                    module.current += '-' + zero_qualifier
                # The resulting version must be a valid SemVer
                match = version_regex_named.match(module.current)
                if not match:
                    raise ValueError('{}\'s current version + build qualifier {} is not a valid semver version'.format(module.name, module.current + build_qualifier))

                if set_both:
                    module.dependency = module.current

                print(f'updating {module.name} to use dependency={module.dependency}, current={module.current}')
                newlines.append(module.string_for_version_file())

    with open(version_file, 'w', encoding='utf-8') as f:
        for line in newlines:
            f.write(line)


def update_versions_file_for_nightly_devops(build_type, build_qualifier, artifact_id, group_id):

    version_file = os.path.normpath('eng/versioning/version_' + build_type.name + '.txt')
    print('version_file=' + version_file)
    library_to_update = group_id + ':' + artifact_id
    print('adding build_qualifier({}) to {}'.format(build_qualifier, library_to_update))
    version_map = {}
    newlines = []
    artifact_found = False
    with open(version_file, encoding='utf-8') as f:
        for raw_line in f:
            stripped_line = raw_line.strip()
            if not stripped_line or stripped_line.startswith('#'):
                newlines.append(raw_line)
            else:
                module = CodeModule(stripped_line)
                # basically we don't want to change any of the parent versions here, only
                # library versions
                if module.name in items_we_should_not_update:
                    newlines.append(module.string_for_version_file())
                    continue
                if library_to_update == module.name:
                    artifact_found = True
                    if hasattr(module, 'current'):
                        set_both = False
                        # In the case where the current and dependency are both equal then both
                        # need to be updated. In theory, this should only really happen when a
                        # new library has been added and has not yet been released but can also
                        # happen if a library has just been released and the devops build kicks
                        # of before the human submits the PR to increase the current version. Being
                        # that it's a devops build and both versions are being updated this should
                        # be OK.
                        if module.current == module.dependency:
                            set_both = True
                        if '-' in module.current:
                            # if the module is 1.2.3-beta.x, strip off everything after the '-' and add the qualifier
                            module.current = module.current[:module.current.rfind('-') + 1] + build_qualifier
                        else:
                            module.current += '-' + build_qualifier
                        match = version_regex_named.match(module.current)
                        if not match:
                            raise ValueError('{}\'s current version + build qualifier {} is not a valid semver version'.format(module.name, module.current + build_qualifier))
                        if set_both:
                            module.dependency = module.current

                # If the library is not the update target and is unreleased, use
                # a version range based on the build qualifier as the dependency
                # version so that packages from this build that are released to
                # the dev feed will depend on a version that should be found in
                # the dev feed.
                # This script runs once for each artifact so it makes no
                # changes in the case where a dependency version has already
                # been modified.
                elif (module.name.startswith('unreleased_') or module.name.startswith('beta_'))  and not module.dependency.startswith('['):
                    # Assuming a build qualifier of the form: "dev.20200204.1"
                    # Converts "dev.20200204.1" -> "dev.20200204."
                    unreleased_build_qualifier = build_qualifier[:build_qualifier.rfind('.') + 1]

                    if '-' in module.dependency:
                        # if the module is 1.2.3-beta.x, strip off everything after the '-' and add the qualifier
                        module.dependency = module.dependency[:module.dependency.rfind('-') + 1] + unreleased_build_qualifier
                    else:
                        module.dependency += '-' + unreleased_build_qualifier

                    # The final unreleased dependency version needs to be of the form
                    # [1.0.0-dev.YYYYMMDD.,] <-- note the ., this is the version range for Maven
                    module.dependency = '[{},]'.format(module.dependency)

                    print(f'updating unreleased/beta dependency {module.name} to use dependency version range: "{module.dependency}"')

                version_map[module.name] = module
                newlines.append(module.string_for_version_file())

    if not artifact_found:
       raise ValueError('library_to_update ({}) was not found in version file {}'.format(library_to_update, version_file))

    with open(version_file, 'w', encoding='utf-8') as f:
        for line in newlines:
            f.write(line)

# Prep the appropriate version file for source testing. What this really means is set the
# all of the dependency versions to the current versions. This will effectively cause maven
# to use the built version of the libraries for build and testing. The purpose of this is to
# ensure current version compatibility amongst the various libraries for a given built type
def prep_version_file_for_source_testing(build_type):

    version_file = os.path.normpath('eng/versioning/version_' + build_type.name + '.txt')
    print('version_file=' + version_file)
    file_changed = False

    # The version map is needed to get the 'current' version of any beta dependencies
    # in order to update the beta_ version in the From Source runs
    version_map = {}
    newlines = []
    with open(version_file, encoding='utf-8') as f:
        for raw_line in f:
            stripped_line = raw_line.strip()
            if not stripped_line or stripped_line.startswith('#'):
                newlines.append(raw_line)
            else:
                module = CodeModule(stripped_line)
                if hasattr(module, 'current') and not module.current == module.dependency:
                    module.dependency = module.current
                    file_changed = True
                # In order to ensure that the From Source runs are effectively testing everything
                # together using the latest source built libraries, ensure that the beta_ dependency's
                # version is set
                elif module.name.startswith('beta_'):
                    tempName = module.name[len('beta_'):]
                    if tempName in version_map:
                        # beta_ tags only have a dependency version, set that to
                        # the current version of the non-beta dependency
                        module.dependency = version_map[tempName].current
                        file_changed = True
                    else:
                        # if the beta_ dependency doesn't have a non-beta entry in the version file then this is an error
                        raise ValueError('prep_version_file_for_source_testing: beta library ({}) does not have a non-beta entry {} in version file {}'.format(module.name, tempName, version_file))

                version_map[module.name] = module
                newlines.append(module.string_for_version_file())

    with open(version_file, 'w', encoding='utf-8') as f:
        for line in newlines:
            f.write(line)

    return file_changed

# given a build type, artifact id and group id, set the dependency version to the
# current version and increment the current version
def increment_library_version(build_type, artifact_id, group_id):

    version_file = os.path.normpath('eng/versioning/version_' + build_type.name + '.txt')
    print('version_file=' + version_file)
    library_to_update = group_id + ':' + artifact_id

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
                if module.name == library_to_update and hasattr(module, 'current'):
                    artifact_found = True
                    vmatch = version_regex_named.match(module.current)
                    if (vmatch.group('prerelease') is not None):
                        prever = prerelease_regex_named.match(vmatch.group('prerelease'))
                        # This is the case where, somehow, the versioning verification has failed and
                        # the prerelease verification doesn't match "beta.X"
                        if prever is None:
                            # if the build_type isn't data then error
                            if build_type.name.lower() != 'data':
                                raise ValueError('library_to_update ({}:{}) has an invalid prerelease version ({}) which should be of the format beta.X'.format(library_to_update, module.current, vmatch.group('prerelease')))
                            else:
                                # verify that prerelease is "beta"
                                if prerelease_data_regex.match(vmatch.group('prerelease')) is None:
                                    raise ValueError('library_to_update ({}:{}) has an invalid prerelease version ({}) which should be of the format (beta) or (beta.X)'.format(library_to_update, module.current, vmatch.group('prerelease')))
                                # in the case there the prerelease version is just "beta", increment the minor and set the patch to 0
                                minor = int(vmatch.group('minor'))
                                minor += 1
                                new_version = '{}.{}.{}-beta'.format(vmatch.group('major'), minor, 0)
                        else:
                            rev = int(prever.group('revision'))
                            rev += 1
                            new_version = '{}.{}.{}-beta.{}'.format(vmatch.group('major'), vmatch.group('minor'), vmatch.group('patch'), str(rev))
                    else:
                        minor = int(vmatch.group('minor'))
                        minor += 1
                        new_version = '{}.{}.{}-beta.1'.format(vmatch.group('major'), minor, 0)
                    # The dependency version only needs to be updated it if is different from the current version.
                    # This would be the case where a library hasn't been released yet and has been released (either GA or preview)
                    if (module.dependency != module.current):
                        vDepMatch = version_regex_named.match(module.dependency)
                        # If the dependency version is a beta then just set it to whatever the current
                        # version is
                        if (vDepMatch.group('prerelease') is not None):
                            print('library_to_update {}, previous dependency version={}, new dependency version={}'.format(library_to_update, module.dependency, module.current))
                            module.dependency = module.current
                        # else, the dependency version isn't a pre-release version
                        else:
                            # if the dependency version isn't a beta and the current version is, don't
                            # update the dependency version
                            if (vmatch.group('prerelease') is not None):
                                print('library_to_update {}, has a GA dependency version {} and a beta current version {}. The dependency version will be kept at the GA version. '.format(library_to_update, module.dependency, module.current))
                            else:
                                print('library_to_update {}, has both GA dependency {} and current {} versions. The dependency will be updated to {}. '.format(library_to_update, module.dependency, module.current, module.current))
                                module.dependency = module.current
                    print('library_to_update {}, previous current version={}, new current version={}'.format(library_to_update, module.current, new_version))
                    module.current = new_version
                newlines.append(module.string_for_version_file())

    if not artifact_found:
       raise ValueError('library_to_update ({}) was not found in version file {}'.format(library_to_update, version_file))

    with open(version_file, 'w', encoding='utf-8') as f:
        for line in newlines:
            f.write(line)

# Verify that the current version of an artifact matches our versioning scheme. This is meant to be called
# as part of the release pipeline for a given artifact to verify that we don't accidentally release a version
# that doesn't match our versioning scheme
def verify_current_version_of_artifact(build_type, artifact_id, group_id):

    version_file = os.path.normpath('eng/versioning/version_' + build_type.name + '.txt')
    print('version_file=' + version_file)
    library_to_update = group_id + ':' + artifact_id

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
                if module.name == library_to_update and hasattr(module, 'current'):
                    artifact_found = True
                    vmatch = version_regex_named.match(module.current)
                    temp_ver = '{}.{}.{}'.format(vmatch.group('major'), vmatch.group('minor'), vmatch.group('patch'))
                    # we should never have buildmetadata in our versioning scheme
                    if vmatch.group('buildmetadata') is not None:
                        raise ValueError('library ({}) version ({}) in version file ({}) is not a correct version to release. buildmetadata is set and should never be {}'.format(library_to_update, module.current, version_file, vmatch.group('buildmetadata')))

                    # reconstruct the version from the semver pieces and it should match exactly the current
                    # version in the module
                    # If there's a pre-release version it should be beta.X
                    if vmatch.group('prerelease') is not None:
                        prerel = vmatch.group('prerelease')

                        # this regex is looking for beta.X
                        if prerelease_regex_named.match(prerel) is None:
                            # if the build_type isn't data then error
                            if build_type.name.lower() != 'data':
                                raise ValueError('library ({}) version ({}) in version file ({}) is not a correct version to release. The accepted prerelease tag is (beta.X) and the current prerelease tag is ({})'.format(library_to_update, module.current, version_file, prerel))
                            else:
                                # verify that the prerelease tag is "beta" which is the only allowable thing for data track aside from beta.X
                                if prerelease_data_regex.match(prerel) is None:
                                    raise ValueError('library ({}) version ({}) in version file ({}) is not a correct version to release. The accepted prerelease tags for data track are (beta) or (beta.X) and the current prerelease tag is ({})'.format(library_to_update, module.current, version_file, prerel))
                                # at this point the version is <major>.<minor>.<patch>-beta
                                temp_ver = '{}-{}'.format(temp_ver, str(prerel))
                        else:
                            prever = prerelease_regex_named.match(prerel)
                            rev = int(prever.group('revision'))
                            temp_ver = '{}-beta.{}'.format(temp_ver, str(rev))

                    # last but not least, for sanity verify that the version constructed from the
                    # semver pieces matches module's current version
                    if module.current != temp_ver:
                        raise ValueError('library ({}) version ({}) in version file ({}) does not match the version constructed from the semver pieces ({})'.format(library_to_update, module.current, version_file, temp_ver))

                    print('The version {} for {} looks good!'.format(module.current, module.name))


    if not artifact_found:
       raise ValueError('library ({}) was not found in version file {}'.format(library_to_update, version_file))

def main():
    parser = argparse.ArgumentParser(description='set version numbers in the appropriate version text file', add_help=False)
    required = parser.add_argument_group('required arguments')
    required.add_argument('--build-type', '--bt', type=BuildType, choices=list(BuildType), required=True)
    optional = parser.add_argument_group('optional arguments')
    optional.add_argument('--build-qualifier', '--bq', help='build qualifier to append onto the version string.')
    optional.add_argument('--artifact-id', '--ai', help='artifactId of the target library')
    optional.add_argument('--group-id', '--gi', help='groupId of the target library')
    optional.add_argument('--prep-source-testing', '--pst', action='store_true', help='prep the version file for source testing')
    optional.add_argument('--increment-version', '--iv', action='store_true', help='increment the version for a given group/artifact')
    optional.add_argument('--verify-version', '--vv', action='store_true', help='verify the version for a given group/artifact')
    optional.add_argument('--set-dev-zero-version', '--sdzv', action='store_true', help='Set a zero dev build version for packages that do not already have dev versions set (should be run after setting dev versions for other packages)')
    optional.add_argument('-h', '--help', action='help', default=argparse.SUPPRESS, help='show this help message and exit')

    args = parser.parse_args()
    if (args.build_type == BuildType.management):
        raise ValueError('{} is not currently supported.'.format(BuildType.management.name))
    start_time = time.time()
    file_changed = False
    if (args.prep_source_testing):
        file_changed = prep_version_file_for_source_testing(args.build_type)
    elif (args.increment_version):
        if not args.artifact_id or not args.group_id:
            raise ValueError('increment-version requires both the artifact-id and group-id arguments. artifact-id={}, group-id={}'.format(args.artifact_id, args.group_id))
        increment_library_version(args.build_type, args.artifact_id, args.group_id)
    elif (args.verify_version):
        if not args.artifact_id or not args.group_id:
            raise ValueError('verify-version requires both the artifact-id and group-id arguments. artifact-id={}, group-id={}'.format(args.artifact_id, args.group_id))
        verify_current_version_of_artifact(args.build_type, args.artifact_id, args.group_id)
    elif (args.set_dev_zero_version):
        if not args.build_qualifier:
            raise ValueError('set-dev-zero-version requires build-qualifier')
        set_dev_zero_version(args.build_type, args.build_qualifier)
    else:
        if not args.artifact_id or not args.group_id or not args.build_qualifier:
            raise ValueError('update-version requires the artifact-id, group-id and build-qualifier arguments. artifact-id={}, group-id={}, build-qualifier={}'.format(args.artifact_id, args.group_id, args.build_qualifier))
        update_versions_file_for_nightly_devops(args.build_type, args.build_qualifier, args.artifact_id, args.group_id)
    elapsed_time = time.time() - start_time
    print('elapsed_time={}'.format(elapsed_time))
    print('Total time for replacement: {}'.format(str(timedelta(seconds=elapsed_time))))
    # if the file changed flag is set, which only happens through a call to prep_version_file_for_source_testing,
    # then exit with a unique code that allows us to know that something changed.
    if (file_changed):
        print('##vso[task.setvariable variable=ShouldRunSourceTests]true')

if __name__ == '__main__':
    main()