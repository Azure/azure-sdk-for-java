# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.9 or higher is required to run this script.

# Use case: Update all the versions in README.md and pom.xml files based on
# the versions in versions_[client|data|management].txt, external_dependencies.txt
#
# This script will update all versions to what is specified in the version files. In a previous iteration
# of this script, there were flags to only update specific versions. That was a historic cases when libraries
# weren't as compliant with following the versioning scheme as they are now.
#
#    python eng/versioning/update_versions.py
# For example: To update versions without touching the README files
#    python eng/versioning/update_versions.py --skip-readme (--sr)
#
# Use case: Update the versions in a particular file
#
#    python eng/versioning/update_versions.py --target-file pom-file-to-update
# For example: To update all versions for the given pom file
#    python eng/versioning/update_versions.py --tf <pathToPomFile>\pom.xml

import argparse
from datetime import timedelta
import json
import os
import re
import sys
import time
from typing import Dict
from utils import BuildType
from utils import CodeModule
from utils import load_version_map_from_file
from utils import external_dependency_version_regex
from utils import external_dependency_include_regex
from utils import run_check_call
from utils import include_update_marker
from utils import version_regex_str_no_anchor
from utils import version_update_start_marker
from utils import version_update_end_marker
from utils import version_update_marker
import xml.etree.ElementTree as ET

exception_list = []

def update_versions(version_map: Dict[str, CodeModule], ext_dep_map, target_file, skip_readme, auto_version_increment, library_array):

    newlines = []
    repl_open, repl_thisline, file_changed, is_include = False, False, False, False
    print('processing: ' + os.path.normpath(target_file))
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
                        # only update the version in the MD file if the module is in the list or the list is empty
                        if len(library_array) == 0 or module_name in library_array:
                            repl_open, repl_thisline = True, True
                        else:
                            repl_open, repl_thisline = False, False
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
                        except (KeyError, AttributeError):
                            # This can happen when a dependency is an unreleased_ or beta_ dependency and the tag is current instead of dependency
                            raise ValueError('Module: {0} does not have a current version.\nFile={1}\nLine={2}'.format(module_name, target_file, line))
                    elif version_type == 'dependency':
                        try:
                            module = version_map[module_name]
                            if module.is_unreleased and module.replace_unreleased_dependency:
                                to_replace_module = version_map[module.name[len('unreleased_'):]]
                                newline = re.sub(version_regex_str_no_anchor, to_replace_module.dependency, line)
                                newline = newline.replace(module.name, to_replace_module.name)
                            else:
                                newline = re.sub(version_regex_str_no_anchor, module.dependency, line)
                        except (KeyError, AttributeError):
                            # This should never happen unless the version file is malformed
                            raise ValueError('Module: {0} does not have a dependency version.\nFile={1}\nLine={2}'.format(module_name, target_file, line))
                    elif version_type == 'external_dependency':
                        if is_include:
                            try:
                                module = ext_dep_map[module_name]
                                new_include_version = module.string_for_allowlist_include()
                                newline = re.sub(external_dependency_include_regex, new_include_version, line)
                            except (KeyError, AttributeError):
                                raise ValueError('Module: {0} does not have an external dependency version.\nFile={1}\nLine={2}'.format(module_name, target_file, line))
                        else:
                            try:
                                module = ext_dep_map[module_name]
                                new_version = module.external_dependency
                                newline = re.sub(external_dependency_version_regex, new_version, line)
                            except (KeyError, AttributeError):
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
                update_changelog(target_file, auto_version_increment, library_array)

    except Exception as e:
        exception_list.append(e)

# Updating the changelog is special. Grab the version from the respective pom file
def update_changelog(pom_file, is_increment, library_array):

    # Before doing anything, ensure that there is a changelog.md file sitting next to the pom file
    dirname = os.path.dirname(pom_file)
    changelog = os.path.join(dirname, "CHANGELOG.md")

    if os.path.isfile(changelog):
        tree = ET.parse(pom_file)
        xml_root = tree.getroot()
        xml_version = xml_root.find('{http://maven.apache.org/POM/4.0.0}version')
        version = xml_version.text
        xml_artifactId = xml_root.find('{http://maven.apache.org/POM/4.0.0}artifactId')
        xml_groupId = xml_root.find('{http://maven.apache.org/POM/4.0.0}groupId')
        library = xml_groupId.text + ":" + xml_artifactId.text
        if len(library_array) == 0 or library in library_array:
            script = os.path.join(os.path.dirname(__file__), '..', "common", "scripts", "Update-ChangeLog.ps1")
            commands = [
                "pwsh",
                script,
                "--Version",
                version,
                "--ChangeLogPath",
                changelog,
                "--Unreleased:$true", # If is_increment is false then a release is being prepped
                "--ReplaceLatestEntryTitle:$" + str(not is_increment) # If this call is not a result of auto version increment then replace the latest entry with the current version
            ]
            # Run script to update change log
            run_check_call(commands, '.')
    else:
        print('There is no CHANGELOG.md file in {}, skipping update'.format(dirname))

def load_version_overrides(the_file, version_map, overrides_name):
    with open(the_file) as f:
        data = json.load(f)
        if overrides_name not in data:
            raise ValueError('Version override name: {0} is not found in {1}'.format(overrides_name, the_file))

        overrides = data[overrides_name]
        for override in overrides:
            if len(override) != 1:
                raise ValueError('Expected exactly one module, but got: {0}'.format(override))

            for module_name in override:
                module_str = module_name + ";" + override[module_name]
                module = CodeModule(module_str)
                version_map[module.name] = module
                break

def display_version_info(version_map):
    for value in version_map.values():
        print(value)

def update_versions_all(target_file, skip_readme, auto_version_increment: bool, library_array, version_overrides, include_perf_tests, setting_dev_version: bool):
    version_map: Dict[str, CodeModule] = {}
    ext_dep_map: Dict[str, CodeModule] = {}
    # Load the version file into version_map and external dependency file into ext_dep_map.
    version_file = os.path.join(os.path.dirname(__file__), 'version_client.txt')
    load_version_map_from_file(version_file, version_map, auto_version_increment, setting_dev_version)

    dependency_file = os.path.join(os.path.dirname(__file__), 'external_dependencies.txt')
    load_version_map_from_file(dependency_file, ext_dep_map)

    if version_overrides and not version_overrides.startswith('$'):
        # Azure DevOps passes '$(VersionOverrides)' when the variable value is not set
        load_version_overrides(os.path.join(os.path.dirname(__file__), 'supported_external_dependency_versions.json'), ext_dep_map, version_overrides)

    # The dependency files are always loaded, report their information.
    print('version_file=' + version_file)
    display_version_info(version_map)

    print('external_dependency_file=' + dependency_file)
    display_version_info(ext_dep_map)

    if target_file:
        update_versions(version_map, ext_dep_map, target_file, skip_readme, auto_version_increment, library_array)
    else:
        for root, _, files in os.walk(os.path.join(os.path.dirname(__file__), '..', '..')):
            for file_name in files:
                file_path = root + os.sep + file_name
                if (file_name.endswith('.md') and not skip_readme) or (file_name.startswith('pom') and file_name.endswith('.xml')):
                    update_versions(version_map, ext_dep_map, file_path, skip_readme, auto_version_increment, library_array)
                elif (file_name.startswith('perf-tests') and (file_name.endswith('.yaml') or file_name.endswith('.yml')) and include_perf_tests):
                    update_versions(version_map, ext_dep_map, file_path, True, False, library_array)


    # This is a temporary stop gap to deal with versions hard coded in java files.
    # Everything within the begin/end tags below can be deleted once
    # https://github.com/Azure/azure-sdk-for-java/issues/7106 has been fixed.
    # version_java_files.txt
    # BEGIN:Versions_in_java_files
    if not target_file:
        # the good thing here is that the java files only contain library versions, not external versions
        version_java_file = os.path.join(os.path.dirname(__file__), 'version_java_files.txt')

        if os.path.exists(version_java_file):
            with open(version_java_file) as f:
                for raw_line in f:
                    java_file_to_update = raw_line.strip()
                    if not java_file_to_update or java_file_to_update.startswith('#'):
                        continue
                    if os.path.isfile(java_file_to_update):
                        update_versions(version_map, ext_dep_map, java_file_to_update, skip_readme, auto_version_increment, library_array)
                    else:
                        # In pipeline contexts, files not local to the current SDK directory may not be checked out from git.
                        print(java_file_to_update + ' does not exist. Skipping')
        else:
            print(version_java_file + ' does not exist. Skipping.')
    # END:Versions_in_java_files

def main():
    parser = argparse.ArgumentParser(description='Replace version numbers in poms and READMEs.')
    parser.add_argument('--skip-readme', '--sr', action='store_true', help='Skip updating of readme files if argument is present')
    parser.add_argument('--target-file', '--tf', nargs='?', help='File to update (optional) - all files in the current directory and subdirectories are scanned if omitted')
    parser.add_argument('--auto-version-increment', '--avi', action='store_true', help='If this script is being run after an auto version increment, add changelog entry for new version')
    # Comma separated list artifacts, has to be split into an array. If we're not skipping README updates, only update MD files for entries for the list of libraries passed in
    parser.add_argument('--library-list', '--ll', nargs='?', help='(Optional) Comma seperated list of groupId:artifactId. If updating MD files, only update entries in this list.')
    parser.add_argument('--version-override', '--vo', nargs='?', help='(Optional) identifier of version update configuratation matching (exactly) first-level identifier in supported_external_dependency_versions.json')
    parser.add_argument('--include-perf-tests', '--ipt', action='store_true', help='Whether perf-tests.yml/perf-tests.yaml files are included in the update')
    parser.add_argument('--setting-dev-version', '--sdv', action='store_true', help='Whether the dev version is being set in the pom.xml files. This is used in CI only.')
    args = parser.parse_args()
    start_time = time.time()
    library_array = []
    if args.library_list:
        library_array = args.library_list.split(',')
    print('library_array length: {0}'.format(len(library_array)))
    print(library_array)
    update_versions_all(args.target_file, args.skip_readme, args.auto_version_increment, library_array, args.version_override, args.include_perf_tests, args.setting_dev_version)
    elapsed_time = time.time() - start_time
    print('elapsed_time={}'.format(elapsed_time))
    print('Total time for replacement: {}'.format(str(timedelta(seconds=elapsed_time))))

    if len(exception_list) > 0:
        for ex in exception_list:
            print("ERROR: " + str(ex))
        sys.exit('There were replacement errors. All errors are immediately above this message.')

if __name__ == '__main__':
    main()
