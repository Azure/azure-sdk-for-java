# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.4 or higher is required to run this script.

# Utility script to generate reflection configuration needed for declaring all classes that can be reflectively accessed by GraalVM native image.
#
# Generates reflect-config.json containing all public Java classes to allow GraalVM native images to reflectively access all classes in Azure SDK. The generated configs are written to files
# in <sdk_root>/native_image/package_name/reflect-config.json. 
# 
# These generated files should be moved to appropriate resource directories of each package (META-INF/native-image/com.azure/<package-name>/reflect-config.json).
# 
# GraalVM does not support adding a package and all classes have to be fully enumerated that are needed for reflection. 
# https://github.com/oracle/graal/issues/1236
#
# Note: This script must be run from the root of the azure-sdk-for-java repository
# Flags
#   --project-list/--pl: (required) List of projects for which reflect-config.json should be generated.
#
# For example: To generate reflection configuration for AppConfiguration and Text Analytics run the following:
#    python eng/scripts/generate_reflect_config.py --pl com.azure:azure-data-appconfiguration,com.azure:azure-ai-textanalytics

import argparse
from datetime import timedelta
import os
import time
import json
from pom_helper import *

root_path = os.path.normpath(os.path.abspath(__file__) + '/../../../')


# Used for serializing reflection config as JSON
class ReflectionClass:
    def __init__(self, class_name):
        self.name = class_name
        self.allDeclaredConstructors = True
        self.allDeclaredFields = True
        self.allDeclaredMethods = True

    def toJSON(self):
        return json.dumps(self, default=lambda o: o.__dict__, indent=4)


def obj_dict(obj):
    return obj.__dict__


# Generates the reflect-config.json files for all packages in the input list
def generate_reflect_config(project_list: str):
    project_list_identifiers = project_list.split(',')

    for project_identifier in project_list_identifiers:
        public_classes = find_all_public_classes(project_identifier)
        print(json.dumps(public_classes, default=obj_dict, indent=4))
        reflect_config_file = os.path.join(root_path, "native_image/" + project_identifier.split(":")[1] + "/reflect-config.json")
        os.makedirs(os.path.dirname(reflect_config_file), exist_ok=True)
        with open(file=reflect_config_file, mode='w') as reflect_config:
            reflect_config.write(json.dumps(public_classes, default=obj_dict, indent=4))
        

def find_all_public_classes(project_list_identifiers):
    ignored_classes = ["module-info.java", "package-info.java"]
    public_classes = []

    for root, _, files in os.walk(root_path):
        for file_name in files:
            file_path = root + os.sep + file_name

            # Only parse files that are pom.xml files.
            if (file_name.startswith('pom') and file_name.endswith('.xml')):
                project = create_project_for_pom(file_path, project_list_identifiers)
                if project is not None:
                    for package, _, source_files in os.walk(root + "/src/main/java"):
                        package_name = package.split("src/main/java")[1].replace("\\", ".").removeprefix(".") + "."
                        for source_file_name in source_files:
                            if source_file_name not in ignored_classes and source_file_name.endswith('.java'):
                                class_name = package_name + source_file_name.replace(".java", "")
                                reflection_class = ReflectionClass(class_name)
                                public_classes.append(reflection_class)
    return public_classes



def create_project_for_pom(pom_path: str, project_list_identifiers: list):
    if 'eng' in pom_path.split(os.sep):
        return

    tree = ET.parse(pom_path)
    tree_root = tree.getroot()

    project_identifier = create_artifact_identifier(tree_root)
    module_path = pom_path.replace(root_path, '').replace('\\', '/')
    directory_path = module_path[:module_path.rindex('/')]
    parent_pom = get_parent_pom(tree_root)

    # If the project isn't a track 2 POM skip it and not one of the project list identifiers.
    if not project_identifier in project_list_identifiers:
        return

    project = Project(project_identifier, directory_path, module_path, parent_pom)
    return project


def main():
    parser = argparse.ArgumentParser(description='Generates reflect-config.json file for all classes in a library that will be needed for building GraalVM native images.')
    parser.add_argument('--project-list', '--pl', type=str)
    args = parser.parse_args()
    if args.project_list == None:
        raise ValueError('Missing project list.')
    start_time = time.time()
    generate_reflect_config(args.project_list)
    elapsed_time = time.time() - start_time

    print('elapsed_time={}'.format(elapsed_time))
    print('Total time for generating reflection configuration: {} seconds'.format(str(timedelta(seconds=elapsed_time))))


if __name__ == '__main__':
    main()
