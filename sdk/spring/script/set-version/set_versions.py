# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.4 or higher is required to run this script.

# Use case: set all the versions in pom.xml files.
#   Configuration:
#     1. config/target_folder.txt: list all the folders need to set version.
#     2. config/version_list.txt: the version list.


import os
import re
import time

class Artifact:
    def __init__(self, name):
        # line format: 
        # groupId:artifactId:version
        items = name.split(':')
        self.group_id = items[0]
        self.artifact_id = items[1]
        self.version = items[2]
        self.name = self.group_id + ":" + self.artifact_id
    def __str__(self):
        return self.group_id + ":" + self.artifact_id + ":" + self.version

def load_version_map_from_file(the_file, version_map):
    with open(the_file) as f:
        for raw_line in f:
            stripped_line = raw_line.strip()
            if not stripped_line or stripped_line.startswith('#'):
                continue
            artifact = Artifact(stripped_line)
            if (artifact.name in version_map):
                raise ValueError('Version file: {0} contains a duplicate entry: {1}'.format(the_file, artifact.name))
            version_map[artifact.name] = artifact

def set_versions_in_file(file, version_map):
    version_update_marker = re.compile(r'\{(x-version-update|x-include-update);([^;]+);([^}]+)\}')
    newlines = []
    file_changed = False
    print('Processing: ' + file)
    try:
        with open(file, encoding='utf-8') as f:
            for line in f:
                match = version_update_marker.search(line)
                if match:
                    name = match.group(2)
                    if name not in version_map:
                        newlines.append(line)
                        continue
                    new_artifact = version_map[name]
                    new_version = new_artifact.version
                    new_include_string = new_artifact.group_id + ":" + new_artifact.artifact_id + "[" + new_version + "]"
                    line1 = re.sub(r'(?<=<version>).+?(?=</version>)', new_version, line)
                    line2 = re.sub(r'(?<=<include>).+?(?=</include>)', new_include_string, line1)
                    newlines.append(line2)
                    print("    Updated item:")
                    print("        Original value: " + line.strip())
                    print("        New value:      " + line2.strip())
                    file_changed = True
                else:
                    newlines.append(line)
        if file_changed:
            with open(file, 'w', encoding='utf-8') as f:
                for line in newlines:
                    f.write(line)
    except Exception as e:
        print("Unexpected exception: " + str(e))

def set_versions_in_folder(folder, version_map):
    for root, dirs, files in os.walk(os.path.normpath(folder), topdown=False):
        for file in files:
            if file.startswith('pom.') and file.endswith('.xml'):
                set_versions_in_file(os.path.join(root, file), version_map)

def load_folders_from_file(target_folder_list_file, folders):
    with open(target_folder_list_file) as f:
        for raw_line in f:
            stripped_line = raw_line.strip()
            if not stripped_line or stripped_line.startswith('#'):
                continue
            folders.append(stripped_line)

def set_versions_by_config():
    version_list_file = os.path.normpath('sdk/spring/script/set-version/config/version_list.txt')
    version_map = {}
    load_version_map_from_file(version_list_file, version_map)
    target_folder_list_file = os.path.normpath('sdk/spring/script/set-version/config/target_folder_list.txt')
    folders = []
    load_folders_from_file(target_folder_list_file, folders)
    for folder in folders:
        set_versions_in_folder(folder, version_map)

def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir("../../../..")
    print("Working directory: " + os.getcwd())

def main():
    start_time = time.time()
    change_to_root_dir()
    set_versions_by_config()
    elapsed_time = time.time() - start_time
    print('elapsed_time={}'.format(elapsed_time))

if __name__ == '__main__':
    main()
