# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.4 or higher is required to run this script.

# Use case: Creates a cgmanifest.json file for dependencies and plugins used by the passed project.
#
# Flags
#   --p, --project: The path to the project file. This can be left empty to use the root project.
#   --go, --generate-only: Only generate the cgmanifest.json file. Do not run dependencies and plugins resolution.
#                          If used the existing dependency resolution files must be called 'buildAndPluginDeps.txt'.
#
# Output:
#   cgmanifest.json file in the same directory as the project file.
#
# Maven must be installed and on the path for this to run. This script calls mvn dependency:tree and 
# dependency:resvole-plugins to get the list of plugins and their dependencies used by the project. It then processes
# the output and creates a cgmanifest.json file with the dependencies as the registered components with the 
# user as the dependency roots so that detection can be sourced back to the user.
#
# For example: Create a cgmanifest file for azure-core
#  python generate_plugin_cgmanifest.py --p sdk/core/azure-core
#
# For example: Create a cgmanifest file for the entire repository
#  python generate_plugin_cgmanifest.py
#
# For example: Create a cgmanifest file with existing dependencies resolved
#  python generate_plugin_cgmanifest.py --p sdk/core/azure-core --go
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

# Azure libraries match ' *(.*):(.*):.*:(.*)'
azure_lib_regex = re.compile(r'^ *(.*):(.*):.*:(.*)$')

# Azure dependencies match '[+-|\ ]*(.*):(.*):.*:(.*):.*'
azure_dep_regex = re.compile(r'^[\+\-\|\\ ]*(.*):(.*):.*:(.*):.*$')

# plugin lines match ' *(.*):(.*):maven-plugin:(.*):.*'
plugin_line_regex = re.compile(r'^ *(.*):(.*):maven-plugin:(.*):.*$')

# plugin dependencies match ' *(.*):(.*):.*:(.*):.*'
plugin_dep_regex = re.compile(r'^ *(.*):(.*):.*:(.*)$')

# plugin identifiers match \s*(.*):(.*):maven-plugin:(.*):.*
# plugin dependencies match \s*(.*):(.*):.*:(.*):.*
def create_cgmanifest_from_project(project: str, generate_only: bool):
    project = 'sdk/core/azure-core'
    generate_only = True
    abs_project = None
    if project is not None:
        abs_project = os.path.join(root_path, project)
    else:
        abs_project = root_path
    
    if not generate_only:
        subprocess.run(['mvn', 'dependency:tree', 'dependency:resolve-plugins', '-DexcludeReactor=false', '-DoutputFile=target/buildAndPluginDeps.txt', '-DappendOutput', '-T', '2C'], check=True, shell=True, cwd=abs_project)

    dep_to_usage: Dict[str, Set[str]] = {}
    for root, _, files in os.walk(abs_project):
        for file_name in files:
            if file_name != 'buildAndPluginDeps.txt':
                continue

            with open(file=root + os.sep + file_name, mode='r') as plugin_deps_file:
                # Build dependencies will be listed first as dependency:tree runs first.
                # So it will be build dependencies followed by plugin dependencies.
                # Plugin dependencies are noted by a line containing 'The following plugins have been resolved:'.
                is_plugin = False
                last_user = None
                for line in plugin_deps_file.readlines():
                    line = line.strip()
                    if line == '':
                        continue

                    if 'The following plugins have been resolved:' in line:
                        is_plugin = True
                        continue
                    
                    if not is_plugin:
                        match = azure_lib_regex.match(line)
                        if last_user is None and match:
                            last_user = match.group(1) + ':' + match.group(2) + ':' + match.group(3)
                        else:
                            match = azure_dep_regex.match(line)
                            if match:
                                azure_dep = match.group(1) + ':' + match.group(2) + ':' + match.group(3)
                                if azure_dep not in dep_to_usage:
                                    dep_to_usage[azure_dep] = set()

                                dep_to_usage[azure_dep].add(last_user)   
                    else:
                        match = plugin_line_regex.match(line)
                        if match:
                            last_user = match.group(1) + ':' + match.group(2) + ':' + match.group(3)
                        else:
                            match = plugin_dep_regex.match(line)
                            if match:
                                plugin_dep = match.group(1) + ':' + match.group(2) + ':' + match.group(3)
                                if plugin_dep not in dep_to_usage:
                                    dep_to_usage[plugin_dep] = set()

                                dep_to_usage[plugin_dep].add(last_user)
    
    cgmanifest = {}
    cgmanifest['$schema'] = 'https://json.schemastore.org/component-detection-manifest.json'
    cgmanifest['version'] = 1
    cgmanifest['registrations'] = []
    for dep in dep_to_usage:
        split_dep = dep.split(':')
        component = {}
        component['component'] = {}
        component['component']['type'] = 'maven'
        component['component']['maven'] = {}
        component['component']['maven']['groupId'] = split_dep[0]
        component['component']['maven']['artifactId'] = split_dep[1]
        component['component']['maven']['version'] = split_dep[2]
        component['component']['dependencyRoots'] = []
        
        for user in dep_to_usage[dep]:
            split_user = user.split(':')
            plugin_component = {}
            plugin_component['component'] = {}
            plugin_component['component']['type'] = 'maven'
            plugin_component['component']['maven'] = {}
            plugin_component['component']['maven']['groupId'] = split_user[0]
            plugin_component['component']['maven']['artifactId'] = split_user[1]
            plugin_component['component']['maven']['version'] = split_user[2]
            component['component']['dependencyRoots'].append(plugin_component)
        
        cgmanifest['registrations'].append(component)
    
    cgmanifest_path = os.path.abspath(os.path.join(abs_project, 'cgmanifest.json'))

    cgmanifest_json = json.dumps(cgmanifest, indent=2)
    with (open(file=cgmanifest_path, mode='w')) as cgmanifest_file:
        cgmanifest_file.write(cgmanifest_json)

def main():
    parser = argparse.ArgumentParser(description='Generated a cgmanifest.json for a project.')
    parser.add_argument('--project', '--p', type=str)
    parser.add_argument('--generate-only', '--go', action='store_true')
    args = parser.parse_args()
    start_time = time.time()
    create_cgmanifest_from_project(args.project, args.generate_only)
    elapsed_time = time.time() - start_time

    print('elapsed_time={}'.format(elapsed_time))
    print('Total time for replacement: {} seconds'.format(str(timedelta(seconds=elapsed_time))))

if __name__ == '__main__':
    main()
