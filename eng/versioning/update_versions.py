# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.4 or higher is required to run this script.

# Use case: Update all the versions in README.md and pom.xml files based on
# the versions in versions_[client|data|management].txt, external_dependencies.txt
#
# It's worth noting that there are 3 update types, library, external_dependencies and all. 'All' means update both the libraries
# for the track selected as well as the external_dependencies.
#
#    python eng/versioning/update_versions.py --update-type [library|external_dependency|all] --build-type [client|data|management]
# For example: To update the library versions for the client track without touching the README files
#    python eng/versioning/update_versions.py --ut library --bt client --sr
#
# Use case: Update the versions in a particular file
#
#    python utilities/update_versions.py --update-type [library|external_dependency|all] -build-type [client|data|management] --target-file pom-file-to-update
# For example: To update all versions for the client track for a given pom file
#    python eng/versioning/update_versions.py --ut all --bt client --tf <pathToPomFile>\pom.xml
#
# Use case: Update the external_dependencies
#
#    python utilities/update_versions.py --update-type [library|external_dependency|all] -build-type [client|data|management] --target-file pom-file-to-update
# For example: To update all versions for the client track for a given pom file. While the skip readme flag isn't entirely
# necessary here, since our README.md files don't contain externaly dependency versions, there's no point in scanning files
# that shouldn't require changes.
#    python eng/versioning/update_versions.py --ut external_dependency --sr
#
# The script must be run at the root of azure-sdk-for-java.

import argparse
from datetime import timedelta
import errno
import os
import re
import sys
import time
import traceback
from utils import BuildType
from utils import CodeModule
from utils import external_dependency_version_regex
from utils import external_dependency_include_regex
from utils import run_check_call
from utils import UpdateType
from utils import include_update_marker
from utils import version_regex_str_no_anchor
from utils import version_update_start_marker
from utils import version_update_end_marker
from utils import version_update_marker
import xml.etree.ElementTree as ET

def update_versions(update_type, version_map, ext_dep_map, target_file, skip_readme, auto_version_increment):

    newlines = []
    repl_open, repl_thisline, file_changed, is_include = False, False, False, False
    print('processing: ' + target_file)
    try:
        with open(target_file, encoding='utf-8') as f:
            for line in f:
                is_include = False
                repl_thisline = repl_open
                match = version_update_marker.search(line)
                if match and not target_file.endswith('.md'):
                    module_name, version_type = match.group(1), match.group(2)
                    repl_thisline = True
                elif include_update_marker.search(line):
                    match = include_update_marker.search(line)
                    module_name, version_type = match.group(1), match.group(2)
                    repl_thisline = True
                    is_include = True
                else:
                    match = version_update_start_marker.search(line)
                    if match:
                        module_name, version_type = match.group(1), match.group(2)
                        repl_open, repl_thisline = True, True
                    else:
                        match = version_update_end_marker.search(line)
                        if match:
                            repl_open, repl_thisline = False, False

                if repl_thisline:
                    # If the module isn't found then just continue. This can happen if we're going through and updating
                    # library versions for one track and tag entry is for another track or if we're only updating
                    # external_dependency versions.
                    if module_name not in version_map and (version_type == 'current' or version_type == 'dependency'):
                        newlines.append(line)
                        continue
                    new_version = ''
                    if version_type == 'current':
                        try:
                            module = version_map[module_name]
                            new_version = module.current
                            newline = re.sub(version_regex_str_no_anchor, new_version, line)
                        except AttributeError:
                            # This can happen when a dependency is an unreleased_ or beta_ dependency and the tag is current instead of dependency
                            raise ValueError('Module: {0} does not have a current version.\nFile={1}\nLine={2}'.format(module_name, target_file, line))
                    elif version_type == 'dependency':
                        try:
                            module = version_map[module_name]
                            new_version = module.dependency
                            newline = re.sub(version_regex_str_no_anchor, new_version, line)
                        except AttributeError:
                            # This should never happen unless the version file is malformed
                            raise ValueError('Module: {0} does not have a dependency version.\nFile={1}\nLine={2}'.format(module_name, target_file, line))
                    elif version_type == 'external_dependency':
                        # The external dependency map will be empty if the update type is library
                        if update_type == UpdateType.library:
                            newlines.append(line)
                            continue
                        if is_include:
                            try:
                                module = ext_dep_map[module_name]
                                new_include_version = module.string_for_allowlist_include()
                                newline = re.sub(external_dependency_include_regex, new_include_version, line)
                            except AttributeError:
                                raise ValueError('Module: {0} does not have an external dependency version.\nFile={1}\nLine={2}'.format(module_name, target_file, line))
                        else:
                            try:
                                module = ext_dep_map[module_name]
                                new_version = module.external_dependency
                                newline = re.sub(external_dependency_version_regex, new_version, line)
                            except AttributeError:
                                raise ValueError('Module: {0} does not have an external dependency version.\nFile={1}\nLine={2}'.format(module_name, target_file, line))
                    else:
                        raise ValueError('Invalid version type: {} for module: {}.\nFile={}\nLine={}'.format(version_type, module_name, target_file, line))

                    newlines.append(newline)
                    if line != newline:
                        file_changed = True
                else:
                    newlines.append(line)

                if not repl_open:
                    module_name, version_type = '', ''

        if file_changed:
            with open(target_file, 'w', encoding='utf-8') as f:
                for line in newlines:
                    f.write(line)

            # If the pom file changed check and see if we need to add a version line to the Changelog
            file_name = os.path.basename(target_file)
            if ((auto_version_increment or not skip_readme) and (file_name.startswith('pom.') and file_name.endswith('.xml'))):
                update_changelog(target_file, auto_version_increment)

    except Exception as e:
        print("Unexpected exception: " + str(e))
        traceback.print_exc(file=sys.stderr)

# Updating the changelog is special. Grab the version from the respective pom file
def update_changelog(pom_file, is_increment):

    # Before doing anything, ensure that there is a changelog.md file sitting next to the pom file
    dirname = os.path.dirname(pom_file)
    changelog = os.path.join(dirname, "CHANGELOG.md")

    if os.path.isfile(changelog):
        tree = ET.parse(pom_file)
        xml_root = tree.getroot()
        xml_version = xml_root.find('{http://maven.apache.org/POM/4.0.0}version')
        version = xml_version.text

        script = os.path.join(".", "eng", "common", "scripts", "Update-ChangeLog.ps1")
        commands = [
            "pwsh",
            script,
            "--Version",
            version,
            "--ChangeLogPath",
            changelog,
            "--Unreleased:$true", # This update versions should never stamp in a release date so it will always be unreleased.
            "--ReplaceLatestEntryTitle:$" + str(not is_increment) # If this call is not a result of auto version increment then replace the latest entry with the current version
        ]
        # Run script to update change log
        run_check_call(commands, '.')
    else:
        print('There is no CHANGELOG.md file in {}, skipping update'.format(dirname))

def load_version_map_from_file(the_file, version_map):
    with open(the_file) as f:
        for raw_line in f:
            stripped_line = raw_line.strip()
            if not stripped_line or stripped_line.startswith('#'):
                continue
            module = CodeModule(stripped_line)
            # verify no duplicate entries
            if (module.name in version_map):
                raise ValueError('Version file: {0} contains a duplicate entry: {1}'.format(the_file, module.name))
            # verify that if the module is beta_ or unreleased_ that there's a matching non-beta_ or non-unreleased_ entry
            if (module.name.startswith('beta_') or module.name.startswith('unreleased_')):
                tempName = module.name
                if tempName.startswith('beta_'):
                    tempName = module.name[len('beta_'):]
                else:
                    tempName = module.name[len('unreleased_'):]
                # if there isn't a non beta or unreleased entry then raise an issue
                if tempName not in version_map:
                    raise ValueError('Version file: {0} does not contain a non-beta or non-unreleased entry for beta_/unreleased_ library: {1}'.format(the_file, module.name))

            version_map[module.name] = module

def display_version_info(version_map):
    for value in version_map.values():
        print(value)

def update_versions_all(update_type, build_type, target_file, skip_readme, auto_version_increment):
    version_map = {}
    ext_dep_map = {}
    # Load the version and/or external dependency file for the given UpdateType
    # into the verion_map. If UpdateType.all is selected then versions for both
    # the libraries and external dependencies are being updated.
    if update_type == UpdateType.library or update_type == UpdateType.all:
        version_file = os.path.normpath('eng/versioning/version_' + build_type.name + '.txt')
        print('version_file=' + version_file)
        load_version_map_from_file(version_file, version_map)

    if update_type == UpdateType.external_dependency or update_type == UpdateType.all:
        dependency_file = os.path.normpath('eng/versioning/external_dependencies.txt')
        print('external_dependency_file=' + dependency_file)
        load_version_map_from_file(dependency_file, ext_dep_map)

    display_version_info(version_map)
    display_version_info(ext_dep_map)

    if target_file:
        update_versions(update_type, version_map, ext_dep_map, target_file, skip_readme, auto_version_increment)
    else:
        for root, _, files in os.walk("."):
            for file_name in files:
                file_path = root + os.sep + file_name
                if (file_name.endswith('.md') and not skip_readme) or (file_name.startswith('pom') and file_name.endswith('.xml')):
                    update_versions(update_type, version_map, ext_dep_map, file_path, skip_readme, auto_version_increment)

    # This is a temporary stop gap to deal with versions hard coded in java files.
    # Everything within the begin/end tags below can be deleted once
    # https://github.com/Azure/azure-sdk-for-java/issues/7106 has been fixed.
    # version_*_java_files.txt
    # BEGIN:Versions_in_java_files
    if not target_file and BuildType.none != build_type:
        # the good thing here is that the java files only contain library versions, not
        # external versions
        version_java_file = os.path.normpath('eng/versioning/version_' + build_type.name + '_java_files.txt')

        if os.path.exists(version_java_file):
            with open(version_java_file) as f:
                for raw_line in f:
                    java_file_to_update = raw_line.strip()
                    if not java_file_to_update or java_file_to_update.startswith('#'):
                        continue
                    if os.path.isfile(java_file_to_update):
                        update_versions(update_type, version_map, ext_dep_map, java_file_to_update, skip_readme, auto_version_increment)
                    else:
                        # In pipeline contexts, files not local to the current SDK directory may not be checked out from git.
                        print(java_file_to_update + ' does not exist. Skipping')
        else:
            print(version_java_file + ' does not exist. Skipping.')
    # END:Versions_in_java_files

def main():
    parser = argparse.ArgumentParser(description='Replace version numbers in poms and READMEs.')
    parser.add_argument('--update-type', '--ut', type=UpdateType, choices=list(UpdateType))
    parser.add_argument('--build-type', '--bt', nargs='?', type=BuildType, choices=list(BuildType), default=BuildType.none)
    parser.add_argument('--skip-readme', '--sr', action='store_true', help='Skip updating of readme files if argument is present')
    parser.add_argument('--target-file', '--tf', nargs='?', help='File to update (optional) - all files in the current directory and subdirectories are scanned if omitted')
    parser.add_argument('--auto-version-increment', '--avi', action='store_true', help='If this script is being run after an auto version increment, add changelog entry for new version')
    args = parser.parse_args()
    if args.build_type == BuildType.management:
        raise ValueError('{} is not currently supported.'.format(BuildType.management.name))
    start_time = time.time()
    update_versions_all(args.update_type, args.build_type, args.target_file, args.skip_readme, args.auto_version_increment)
    elapsed_time = time.time() - start_time
    print('elapsed_time={}'.format(elapsed_time))
    print('Total time for replacement: {}'.format(str(timedelta(seconds=elapsed_time))))

if __name__ == '__main__':
    main()
