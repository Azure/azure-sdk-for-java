# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Common classes, utilities and regular expression strings used by the
# versioning python scripts.

from enum import Enum
import re
from typing import Dict
from subprocess import check_call, CalledProcessError

include_update_marker = re.compile(r'\{x-include-update;([^;]+);([^}]+)\}')
version_update_start_marker = re.compile(r'\{x-version-update-start;([^;]+);([^}]+)\}')
version_update_end_marker = re.compile(r'\{x-version-update-end\}')
version_update_marker = re.compile(r'\{x-version-update;([^;]+);([^}]+)\}')

# regex for the version string is suggested one directly from semver.org
# it's worth noting that both regular expressions on that page have start
# of line (^) and end of line ($) anchors which need to be removed since
# what's being matched is in the middle of the string
# https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
version_regex_str_no_anchor = r'(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?'

# External dependency versions do not have to match semver format and the semver regular expressions
# will partially match and produce some hilarious results.
external_dependency_include_regex = r'(?<=<include>).+?(?=</include>)'

# External dependency versions do not have to match semver format and the semver regular expressions
# will partially match and produce some hilarious results.
external_dependency_version_regex = r'(?<=<version>).+?(?=</version>)'

# This is the original regular expression for semver. This differs from the
# previous one in that start of line and end of line anchors are left in place.
# This is the regex that would be used to ensure the entire string matches
# semver format
# https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
version_regex_str_with_names_anchored = r'^(?P<major>0|[1-9]\d*)\.(?P<minor>0|[1-9]\d*)\.(?P<patch>0|[1-9]\d*)(?:-(?P<prerelease>(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+(?P<buildmetadata>[0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$'

# This is specific to our revision which, if there is one, needs to have the format of beta.X
prerelease_version_regex_with_name = r'^beta\.(?P<revision>0|[1-9]\d*)$'
# This is special for track 1, data track, which can be <major>.<minor>.<version>-beta with no ".X"
prerelease_data_version_regex = r'^beta$'

# Allow list prefix remover
allowlist_exception_identifier_remover_regex = re.compile(r'^(?:.+_)(?=.+:)(.*)$')

class UpdateType(Enum):
    external_dependency = 'external_dependency'
    library = 'library'
    all = 'all'

    # defining string is necessary to get ArgumentParser's help output to produce
    # human readable values of UpdateType
    def __str__(self):
        return self.value

class BuildType(Enum):
    client = 'client'
    data = 'data'
    management = 'management'
    none = 'none' # in the case where only external dependencies is being updated

    # defining string is necessary to get ArgumentParser's help output to produce
    # human readable values of BuildType
    def __str__(self):
        return self.value

class CodeModule:
    name: str
    group_id: str
    artifact_id: str
    dependency: str
    current: str = None # current version is optional, only for library versions
    external_dependency: str = None # external dependency version is optional, only for external dependencies
    update_type: UpdateType
    is_beta: bool = False # beta_ is a prefix for unreleased dependencies, may not exist
    is_unreleased: bool = False # unreleased_ is a prefix for unreleased dependencies, may not exist
    replace_unreleased_dependency: bool = False # used to indicate that the unreleased dependency should be replaced with the dependency version

    def __init__(self, module_str):
        # For library versions there will be up to 3 items resulting from the split
        # which will be module name, dependency version and current version. For
        # external dependency versions there should only be 2 items resulting from
        # the split which will be module name and external dependency version.
        items = module_str.split(';')

        self.name = items[0]
        self.group_id = items[0].split(':')[0]
        self.artifact_id = items[0].split(':')[1]

        if len(items) == 2:
            self.is_beta = self.group_id.startswith('beta_')
            self.is_unreleased = self.group_id.startswith('unreleased_')
            if self.is_beta or self.is_unreleased:
                self.dependency = items[1].strip()
                self.update_type = UpdateType.library
            else:
                self.external_dependency = items[1].strip()
                self.update_type = UpdateType.external_dependency
        elif len(items) == 3:
            if self.group_id.startswith('unreleased_') or self.group_id.startswith('beta_'):
                raise ValueError('Unreleased dependency entries should not have a current version, they should only a dependency version')
            self.dependency = items[1]
            self.current = items[2].strip()
            self.update_type = UpdateType.library
        else:
            raise ValueError('unable to parse module string: ' + module_str)

    # overridden string primarily used for error reporting
    def __str__(self):
        # current may or may not exist
        if self.external_dependency != None:
            return self.name + ': External Dependency version=' + self.external_dependency
        if self.current != None:
            return self.name + ': Dependency version=' + self.dependency + ': Current version=' + self.current
        else:
            return self.name + ': Dependency version=' + self.dependency

    # return the CodeModule string formatted for a version file
    def string_for_version_file(self):
        if self.current != None:
            return self.name + ';' + self.dependency + ';' + self.current + '\n'
        else:
            return self.name + ';' + self.dependency + '\n'

    # return the CodeModule string formatted for a allowlist include entry
    # note: for allowlist includes the version needs to be braces in order for
    # the version to be an explicit version. Without the braces a version
    # would be treated as that version and above. For example:
    # <groupId>:<artifactId>:1.2 would be treated as 1.2 and above or equivalent to [1.2,)
    def string_for_allowlist_include(self):
        if self.external_dependency != None:
            temp = self.name
            # This is necessary to deal with the fact that external_dependencies can have
            # '_' in them if they're an external dependency exception. Since the allowlist
            # name needs to be the actual dependency, take everything after the _ which is
            # the actual name
            match = allowlist_exception_identifier_remover_regex.match(temp)
            if match:
                temp = match.group(1)
            return temp + ':[' + self.external_dependency + ']'
        else:
            raise ValueError('string_for_allowlist_include called on non-external_dependency: ' + self.name)

def run_check_call(
    command_array,
    working_directory,
    acceptable_return_codes=[],
    run_as_shell=False,
    always_exit=True,
):
    try:
        if run_as_shell:
            print(
                "Command Array: {0}, Target Working Directory: {1}".format(
                    " ".join(command_array), working_directory
                )
            )
            check_call(" ".join(command_array), cwd=working_directory, shell=True)
        else:
            print(
                "Command Array: {0}, Target Working Directory: {1}".format(
                    command_array, working_directory
                )
            )
            check_call(command_array, cwd=working_directory)
    except CalledProcessError as err:
        if err.returncode not in acceptable_return_codes:
            print(err)
            if always_exit:
                exit(1)
            else:
                return err


def load_version_map_from_file(the_file, version_map: Dict[str, CodeModule], auto_increment_version=False, dev_version=False):
    newlines = []
    file_changed = False
    with open(the_file) as f:
        for raw_line in f:
            stripped_line = raw_line.strip()
            if not stripped_line or stripped_line.startswith('#'):
                newlines.append(raw_line)
                continue
            module = CodeModule(stripped_line)
            # verify no duplicate entries
            if (module.name in version_map):
                raise ValueError('Version file: {0} contains a duplicate entry: {1}'.format(the_file, module.name))
            # verify that if the module is beta_ or unreleased_ that there's a matching non-beta_ or non-unreleased_ entry
            is_beta = module.name.startswith('beta_')
            is_unreleased = module.name.startswith('unreleased_')
            if (is_beta or is_unreleased):
                tempName = module.name
                if is_beta:
                    tempName = module.name[len('beta_'):]
                else:
                    tempName = module.name[len('unreleased_'):]
                # if there isn't a non beta or unreleased entry then raise an issue
                if tempName not in version_map:
                    raise ValueError('Version file: {0} does not contain a non-beta or non-unreleased entry for beta_/unreleased_ library: {1}'.format(the_file, module.name))
                
                # Unreleased dependencies have a few additional checks.
                # 1. If 'auto_increment_version' is true, check if the version matches the non-unreleased dependency
                #    version of the library and that the dependency version doesn't the current version.
                #    If that check passes, flag the unreleased dependency for replacement.
                #    This is to allow the update script to replace the 'unreleased_*' tag with just '*', ex 'unreleased_core' -> 'core'.
                #    When the non-unreleased dependency versions match this is an indication that the library has never
                #    been released and there is a library outside the /sdk/<group> directory that has a development
                #    on it.
                # 2. Check to see that the version matches the current version of the library, , only if we're not setting the nightly dev version.
                #    If it isn't raise an error as unreleased dependencies should match the current version of the library.
                if is_unreleased:
                    non_unreleased_module = version_map[tempName]
                    if auto_increment_version and module.dependency == non_unreleased_module.dependency and non_unreleased_module.current != non_unreleased_module.dependency:
                        module.replace_unreleased_dependency = True
                        file_changed = True
                    elif not dev_version and module.dependency != non_unreleased_module.current:
                        raise ValueError('Version file: {0} contains an unreleased dependency: {1} with a dependency version of {2} which does not match the current version of the library it represents: {3}'.format(the_file, module.name, module.dependency, version_map[tempName].current))
            
            if not module.replace_unreleased_dependency:
                newlines.append(raw_line)

            version_map[module.name] = module

    if file_changed:
        with open(the_file, 'w', encoding='utf-8') as f:
            for line in newlines:
                f.write(line)
                