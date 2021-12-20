# Python version 3.4 or higher is required to run this script.
#
# Change the external_dependencies in sdk/spring for test compatibility of spring-boot version .
#
# How to use:
# 1. Make sure file(`.\sdk\spring\spring_boot_SPRING_BOOT_VERSION_managed_external_dependencies.txt`) exist. If it doesn't exist, please run
#    `.\sdk\spring\scripts\get_spring_boot_managed_external_dependencies.py` to create that file.
# 2. Change `SPRING_BOOT_VERSION` in this script manually.
# 3. Then the ci will automatically run command `python .\sdk\spring\scripts\update_versions.py --ut external_dependency`.
#
# The script must be run at the root of azure-sdk-for-java.

import argparse
from datetime import timedelta
import os
import re
import sys
import time
import traceback
from utils import CodeModule
from utils import external_dependency_version_regex
from utils import external_dependency_include_regex
from utils import UpdateType
from utils import include_update_marker
from utils import version_regex_str_no_anchor
from utils import version_update_start_marker
from utils import version_update_end_marker
from utils import version_update_marker

SPRING_BOOT_VERSION = '2.6.1'

SPRING_BOOT_MANAGED_EXTERNAL_DEPENDENCIES_FILE_NAME = 'sdk/spring/scripts/spring_boot_{}_managed_external_dependencies.txt'.format(SPRING_BOOT_VERSION)

def update_versions(update_type, version_map, ext_dep_map, target_file):
    #list for new pom
    newlines = []
    repl_open, repl_line, file_changed, is_include = False, False, False, False
    print('processing: ' + target_file)
    try:
        with open(target_file, encoding='utf-8') as f:
            for line in f:
                is_include = False
                repl_line = repl_open
                #return the first match{x-version-update;([^;]+);([^}]+)\}
                match = version_update_marker.search(line)
                if match and not target_file.endswith('.md'):
                    module_name, version_type = match.group(1), match.group(2)
                    repl_line = True
                #return the first match{x-include-update;([^;]+);([^}]+)\}
                elif include_update_marker.search(line):
                    match = include_update_marker.search(line)
                    module_name, version_type = match.group(1), match.group(2)
                    repl_line = True
                    is_include = True
                else:
                    #return the first match{x-version-update-start;([^;]+);([^}]+)\}
                    match = version_update_start_marker.search(line)
                    if match:
                        module_name, version_type = match.group(1), match.group(2)
                        repl_open, repl_line = True, True
                    else:
                        #return the first match{x-version-update-end\}
                        match = version_update_end_marker.search(line)
                        if match:
                            repl_open, repl_line = False, False

                if repl_line:
                    # If the module isn't found then just continue.
                    if module_name not in ext_dep_map:
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


    except Exception as e:
        print("Unexpected exception: " + str(e))
        traceback.print_exc(file=sys.stderr)



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

def update_versions_all(update_type, target_file):
    # dictionary
    version_map = {}
    ext_dep_map = {}
    # Load the version of external dependency file for the given UpdateType into the version_map.
    if update_type == UpdateType.external_dependency:
        dependency_file = SPRING_BOOT_MANAGED_EXTERNAL_DEPENDENCIES_FILE_NAME
        print('external_dependency_file=' + dependency_file)
        load_version_map_from_file(dependency_file, ext_dep_map)

    display_version_info(version_map)
    display_version_info(ext_dep_map)

    if target_file:
        update_versions(update_type, version_map, ext_dep_map, target_file)
    else:
        for root, _, files in os.walk("./sdk/spring"):
            for file_name in files:
                file_path = root + os.sep + file_name
                if  file_name.startswith('pom') and file_name.endswith('.xml'):
                    update_versions(update_type, version_map, ext_dep_map, file_path)


def main():
    parser = argparse.ArgumentParser(description='Replace version numbers in poms.')
    parser.add_argument('--update-type', '--ut', type=UpdateType, choices=list(UpdateType))
    parser.add_argument('--target-file', '--tf', nargs='?', help='File to update (optional) - all files in the current directory and subdirectories are scanned if omitted')
    args = parser.parse_args()
    start_time = time.time()
    update_versions_all(args.update_type, args.target_file)
    elapsed_time = time.time() - start_time
    print('elapsed_time={}'.format(elapsed_time))
    print('Total time for replacement: {}'.format(str(timedelta(seconds=elapsed_time))))

if __name__ == '__main__':
    main()
