# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# This script is used to find unused dependencies in the version_client.txt and external_dependencies.txt files.
# It is used in the CI pipeline to ensure that all dependencies are used in the codebase.

import argparse
import os

from utils import CodeModule
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
    unused_deps = get_unused_dependencies(dep_map)
    if unused_deps:
        print(message)
        for dep in unused_deps:
            print("  " + dep)
    return bool(unused_deps)

def get_unused_dependencies(dep_map):
    return [key for key in dep_map if not dep_map[key][0]]

def mark_referenced_dependencies(repository_root, version_map, ext_dep_map):
    for root, _, files in os.walk(repository_root):
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

def remove_dependency_entries(dependency_file, dependency_names):
    with open(dependency_file, encoding="utf-8", newline="") as file:
        lines = file.readlines()

    with open(dependency_file, "w", encoding="utf-8", newline="") as file:
        for line in lines:
            stripped_line = line.strip()
            if stripped_line and not stripped_line.startswith("#"):
                module = CodeModule(stripped_line)
                if module.name in dependency_names:
                    continue
            file.write(line)

def remove_unused_external_dependencies(repository_root, dependency_file):
    ext_dep_map = {}
    fixup_version_map(dependency_file, ext_dep_map)
    mark_referenced_dependencies(repository_root, {}, ext_dep_map)

    unused_ext_deps = get_unused_dependencies(ext_dep_map)
    if unused_ext_deps:
        print("Removing unused external_dependencies.txt entries:")
        for dependency in unused_ext_deps:
            print("  " + dependency)
        remove_dependency_entries(dependency_file, set(unused_ext_deps))

    return unused_ext_deps

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--remove-unused-external-dependencies",
        action="store_true",
        help="Remove external_dependencies.txt entries that have no references in pom files.",
    )
    args = parser.parse_args()

    repository_root = os.path.normpath(".")
    version_file = os.path.normpath("eng/versioning/version_client.txt")
    dependency_file = os.path.normpath("eng/versioning/external_dependencies.txt")

    if args.remove_unused_external_dependencies:
        remove_unused_external_dependencies(repository_root, dependency_file)
        return 0

    version_map = {}
    ext_dep_map = {}
    fixup_version_map(version_file, version_map)
    fixup_version_map(dependency_file, ext_dep_map)
    mark_referenced_dependencies(repository_root, version_map, ext_dep_map)

    unused_dependencies = find_unused_dependencies(version_map, "Unused version_client.txt entries:")
    unused_ext_dep = find_unused_dependencies(ext_dep_map, "Unused external_dependencies.txt entries:")

    if unused_dependencies or unused_ext_dep:
        return 1
    return 0

if __name__ == "__main__":
    exit(main())
