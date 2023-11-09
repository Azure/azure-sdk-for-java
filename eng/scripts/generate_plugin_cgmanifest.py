# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.4 or higher is required to run this script.

# Use case: Creates a cgmanifest.json file for plugins used by the passed project.
#
# Flags
#   --p, --project: The path to the project file. This can be left empty to use the root project.
#
# Output:
#   cgmanifest.json file in the same directory as the project file.
#
# Maven must be installed and on the path for this to run. This script calls mvn dependency:resvole-plugins
# to get the list of plugins and their dependencies used by the project. It then processes the output and
# creates a cgmanifest.json file with the plugin dependencies as the registered components with the plugin
# as the dependency roots so that detection can be sourced back to the plugin.
#
# For example: To create a cgmanifest file for azure-core
#  python generate_plugin_cgmanifest.py --p sdk/core/azure-core
#
# This script must be run from the root of the repo.

import argparse
from datetime import timedelta
import os
import time
import json
import subprocess
import re
from typing import Dict, Set

# From this file get to the root path of the repo.
root_path = os.path.normpath(os.path.abspath(__file__) + '/../../../')

# plugin lines match ' *(.*):(.*):maven-plugin:(.*):.*'
plugin_line_regex = re.compile(r' *(.*):(.*):maven-plugin:(.*):.*')

# plugin dependencies match ' *(.*):(.*):.*:(.*):.*'
plugin_dep_regex = re.compile(r' *(.*):(.*):.*:(.*)')

# plugin identifiers match \s*(.*):(.*):maven-plugin:(.*):.*
# plugin dependencies match \s*(.*):(.*):.*:(.*):.*
def create_cgmanifest_from_project(project: str, maven_local_repo: str):
    abs_project = None
    if project is not None:
        abs_project = os.path.abspath(project)
    else:
        abs_project = root_path
    
    args = None
    if maven_local_repo is not None:
        args = ['mvn', 'dependency:resolve-plugins', '-DexcludeReactor=false', '-DoutputFile=target/pluginDeps.txt', '-T', '2C', '-Dmaven.repo.local=' + maven_local_repo]
    else:
        args = ['mvn', 'dependency:resolve-plugins', '-DexcludeReactor=false', '-DoutputFile=target/pluginDeps.txt', '-T', '2C']

    subprocess.run(args, check=True, shell=True, cwd=abs_project)

    dep_to_plugin: Dict[str, Set[str]] = {}
    for root, _, files in os.walk(abs_project):
        for file_name in files:
            if file_name != 'pluginDeps.txt':
                continue

            with open(file=root + os.sep + file_name, mode='r') as plugin_deps_file:
                lines = plugin_deps_file.readlines()
                lines = [x.strip() for x in lines]
                last_plugin = None
                for line in lines:
                    match = plugin_line_regex.match(line)
                    if match:
                        last_plugin = match.group(1) + ':' + match.group(2) + ':' + match.group(3)
                    else:
                        match = plugin_dep_regex.match(line)
                        if match:
                            plugin_dep = match.group(1) + ':' + match.group(2) + ':' + match.group(3)
                            if plugin_dep not in dep_to_plugin:
                                dep_to_plugin[plugin_dep] = set()

                            dep_to_plugin[plugin_dep].add(last_plugin)
    
    cgmanifest = {}
    cgmanifest['$schema'] = 'https://json.schemastore.org/component-detection-manifest.json'
    cgmanifest['version'] = 1
    cgmanifest['registrations'] = []
    for dep in dep_to_plugin:
        split_dep = dep.split(':')
        component = {}
        component['component'] = {}
        component['component']['type'] = 'maven'
        component['component']['maven'] = {}
        component['component']['maven']['groupId'] = split_dep[0]
        component['component']['maven']['artifactId'] = split_dep[1]
        component['component']['maven']['version'] = split_dep[2]
        component['component']['dependencyRoots'] = []
        
        for plugin in dep_to_plugin[dep]:
            split_plugin = plugin.split(':')
            plugin_component = {}
            plugin_component['component'] = {}
            plugin_component['component']['type'] = 'maven'
            plugin_component['component']['maven'] = {}
            plugin_component['component']['maven']['groupId'] = split_plugin[0]
            plugin_component['component']['maven']['artifactId'] = split_plugin[1]
            plugin_component['component']['maven']['version'] = split_plugin[2]
            component['component']['dependencyRoots'].append(plugin_component)
        
        cgmanifest['registrations'].append(component)
    
    cgmanifest_path = os.path.abspath(os.path.join(abs_project, 'cgmanifest.json'))

    cgmanifest_json = json.dumps(cgmanifest, indent=2)
    with (open(file=cgmanifest_path, mode='w')) as cgmanifest_file:
        cgmanifest_file.write(cgmanifest_json)

def main():
    parser = argparse.ArgumentParser(description='Generated a cgmanifest.json for a project.')
    parser.add_argument('--project', '--p', type=str)
    parser.add_argument('--maven_local_repo', '--mlr', type=str)
    args = parser.parse_args()
    start_time = time.time()
    create_cgmanifest_from_project(args.project, args.maven_local_repo)
    elapsed_time = time.time() - start_time

    print('elapsed_time={}'.format(elapsed_time))
    print('Total time for replacement: {} seconds'.format(str(timedelta(seconds=elapsed_time))))

if __name__ == '__main__':
    main()
