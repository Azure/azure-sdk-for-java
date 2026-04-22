# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# This script is used to find unused dependencies in the version_client.txt and external_dependencies.txt files.
# It is used in the CI pipeline to ensure that all dependencies are used in the codebase.

import os

from utils import load_version_map_from_file
from utils import version_update_marker

IGNORED_DEPENDENCIES = {'springboot4_org.springframework.boot:spring-boot-dependencies',
                        'springboot4_org.springframework.cloud:spring-cloud-dependencies'}

def fixup_version_map(version_file, version_map):
    # uses the util function to load the version map from the file, then adds a bool to each entry to track if it is visisted
    load_version_map_from_file(version_file, version_map)
    for key in version_map:
        val = version_map[key]
        if key in IGNORED_DEPENDENCIES:
            version_map[key] = (True, val)
        else:
            version_map[key] = (False, val)

def find_unused_dependencies(dep_map, message):
    unused_deps = [key for key in dep_map if not dep_map[key][0]]
    if unused_deps:
        print(message)
        for dep in unused_deps:
            print("  " + dep)
    return bool(unused_deps)

def main():
    version_map = {}
    ext_dep_map = {}

    version_file = os.path.normpath("eng/versioning/version_client.txt")
    dependency_file = os.path.normpath("eng/versioning/external_dependencies.txt")

    fixup_version_map(version_file, version_map)
    fixup_version_map(dependency_file, ext_dep_map)

    for root, _, files in os.walk("."):
        try:
            for file in files:
                if file.startswith("pom") and file.endswith(".xml"):
                    with open(os.path.join(root, file), encoding="utf-8") as f:
                        for line in f:
                            match = version_update_marker.search(line)
                            if match:
                                module_name, version_type = match.group(1), match.group(2)
                                if module_name in ext_dep_map or module_name in version_map:
                                    if version_type == "external_dependency":
                                        ext_dep_map[module_name] = (True, ext_dep_map[module_name][1])
                                    else:
                                        version_map[module_name] = (True, version_map[module_name][1])
        except KeyError as e:
            print(str(e) + " was not found in the right place. Please investigate.")

    unused_dependencies = find_unused_dependencies(version_map, "Unused version_client.txt entries:")
    unused_ext_dep = find_unused_dependencies(ext_dep_map, "Unused external_dependencies.txt entries:")

    if unused_dependencies or unused_ext_dep:
        exit(1)

if __name__ == "__main__":
    main()
