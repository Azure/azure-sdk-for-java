# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Common classes, utilities and regular expression strings used by the 
# versioning python scripts.

from enum import Enum
import re

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
external_dependency_version_regex = r'(?<=<version>).+?(?=</version>)'

# This is the original regular expression for semver. This differs from the
# previous one in that start of line and end of line anchors are left in place.
# This is the regex that would be used to ensure the entire string matches
# semver format
# https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
version_regex_str_with_anchor = r'^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$'

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
            self.external_dependency = items[1].strip()
            self.update_type = UpdateType.external_dependency
        elif len(items) == 3:
            self.dependency = items[1]
            self.current = items[2].strip()
            self.update_type = UpdateType.library
        else: 
            raise ValueError('unable to parse module string: ' + module_str)

    # overridden string primarily used for error reporting
    def __str__(self):
        # current may or may not exist
        try:
            return self.name + ': Dependency version=' + self.dependency + ': Current version=' + self.current
        except AttributeError:
            return self.name + ': External Dependency version=' + self.external_dependency
    
    # return the CodeModule string formatted for a version file
    def string_for_version_file(self):
        return self.name + ';' + self.dependency + ';' + self.current + '\n'
