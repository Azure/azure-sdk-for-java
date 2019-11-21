# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.


from datetime import timedelta
from enum import Enum
import os
import re
import time
from utils import CodeModule
from utils import version_update_marker
import xml.etree.ElementTree as ET

valid_parents = ['azure-client-sdk-parent','azure-data-sdk-parent','azure-cosmos-parent']
version_replacement_tag = '<!-- {{x-version-update;{};external_dependency}} -->'

class PomDependencyFormatError(Exception): 

    # Constructor or Initializer
    def __init__(self, message, dependency_lines):
        self.message = message
        self.dependency_lines = dependency_lines
        
    # __str__ is to print() the value
    def __str__(self):
        returnString = self.message + os.linesep
        for line in self.dependency_lines:
            returnString += line
        return(returnString) 

# <!-- {x-version-update;<groupId>:<artifactId>;dependency} -->

# Nothing like processing in the most ghetto way possible...
# Because I need to preserve things like comments and whatnot, using an XML
# dom isn't ideal. Further, I need to add comments after the <version> element
# which, as far as I can tell nothing supports. Finally, external dependencies
# utilizing the dependency rollup may or may not actually have <version> elements
# in which case they have to be added...yeah, complete suckfest
def add_dependency_version_tags(dependency_version_map, target_file):
    newlines = []
    dependency_lines = []
    file_changed = False
    print('processing: ' + target_file)
    try:
        with open(target_file, encoding='utf-8') as f:
            # read the file until we find a dependency tag
            line = f.readline()
            while (line):
                if '<dependency>' in line or '<plugin>' in line:
                    dependency_lines.append(line)
                    line = f.readline()
                    # read the rest of the dependency block
                    while(line):
                        dependency_lines.append(line)
                        line = f.readline()
                        if '</dependency>' in line or '</plugin>' in line:
                            dependency_lines.append(line)
                            break
                    # process the dependency block
                    dependency_changed, dependency_lines = process_dependency_block(dependency_version_map, dependency_lines)
                    if dependency_changed:
                        file_changed = True
                    for line in dependency_lines:
                        newlines.append(line)
                    dependency_lines.clear()
                else:
                    newlines.append(line)
                # read the next line
                line = f.readline()

        if file_changed:
            with open(target_file, 'w', encoding='utf-8') as f:
                for line in newlines:
                    f.write(line)

    except PomDependencyFormatError as pdfe:
        print('Dependency format error in {}{}{}'.format(target_file, os.linesep, str(pdfe)))
    except Exception as e:
        print('Unexpected exception: ' + str(e))

def process_dependency_block(dependency_version_map, dependency_lines):
    modified_dependency_lines = False

    # get the group/artifactId
    groupId = ''
    artifactId = ''
    artifact_pos = -1
    version_pos = -1
    i = 0
    # it's worth noting that the additional checks below, the ones after the and clause
    # are there to prevent erroneous matching on dependency exclude clauses
    for line in dependency_lines:
        if '<groupId>' in line and groupId == '':
            groupId = line[line.find('>') + 1:line.rfind('<')]
        elif '<artifactId>' in line and artifact_pos == -1:
            artifactId = line[line.find('>') + 1:line.rfind('<')]
            artifact_pos = i
        elif '<version>' in line and version_pos == -1:
            # if there's already a version update or exempt tag then there
            # is nothing to process in this particular dependency block
            if 'x-version-update' in line or 'x-version-exempt' in line:
                return modified_dependency_lines, dependency_lines
            version_pos = i
        i+=1

    if groupId == '' or artifactId == '':
        raise PomDependencyFormatError('Malformed dependency is missing groupId or artifactId', dependency_lines)

    module_name = groupId + ':' + artifactId
    if module_name not in dependency_version_map:
        raise PomDependencyFormatError('module_name {} not found in dependency_version_map'.format(module_name), dependency_lines)

    modified_dependency_lines = True
    # if there's no version element insert one after the artifactId 
    if version_pos == -1:
        version_string = ''
        # pad spaces on the left for the new element so everything is formatted correctly
        version_string = version_string.rjust(dependency_lines[artifact_pos].find('<'), ' ')
        version_string += '<version>{0}</version> {1}\n'.format(dependency_version_map[module_name].external_dependency, version_replacement_tag.format(module_name))
        dependency_lines.insert(artifact_pos+1, version_string)
    else:
        version_string = dependency_lines[version_pos].rstrip()
        version_string = version_string[:version_string.find('>') + 1] + dependency_version_map[module_name].external_dependency + version_string[version_string.rfind('<'):]
        version_string += ' {}\n'.format(version_replacement_tag.format(module_name))
        dependency_lines[version_pos] = version_string

    return modified_dependency_lines, dependency_lines

def load_version_map_from_file(the_file, version_map):
    with open(the_file) as f:
        for raw_line in f:
            stripped_line = raw_line.strip()
            if not stripped_line or stripped_line.startswith('#'):
                continue
            module = CodeModule(stripped_line)
            version_map[module.name] = module

def should_process_file(the_file):
    if '.\pom.data.xml' in the_file or '.\pom.client.xml' in the_file or '.\pom.xml' in the_file or '.\parent\pom.xml' in the_file:
        return the_file
    try:
        tree = ET.parse(the_file)
        xmlRoot = tree.getroot()
        xmlParent = xmlRoot.find('{http://maven.apache.org/POM/4.0.0}parent')
        if xmlParent is not None:
            xmlArtifactId = xmlParent.find('{http://maven.apache.org/POM/4.0.0}artifactId')
            if xmlArtifactId is not None:
                artifactIdText = xmlArtifactId.text
                if artifactIdText in valid_parents:
                    return True
    except Exception as e:
        print('Unexpected exception while processing{}:{}'.format(the_file, str(e)))

    return False

def display_version_info(version_map):
    for value in version_map.values():
        print(value)

def update_versions_all():
    dependency_version_map = {}

    dependency_file = os.path.normpath('eng/versioning/external_dependencies.txt')
    print('external_dependency_file=' + dependency_file)
    load_version_map_from_file(dependency_file, dependency_version_map)

    display_version_info(dependency_version_map)

    for root, _, files in os.walk('.'):
        for file_name in files:
            file_path = root + os.sep + file_name
            if file_name.startswith('pom.') and file_name.endswith('.xml'):
                # before calling to update versions, ensure that the parent's artifactId is
                # one that we want to process (we're skipping management right now)
                if should_process_file(file_path):
                    #print('Would process {}'.format(file_path))
                    add_dependency_version_tags(dependency_version_map, file_path)

def main():
    start_time = time.time()
    update_versions_all()
    elapsed_time = time.time() - start_time
    print('elapsed_time={}'.format(elapsed_time))
    print('Total time for replacement: {}'.format(str(timedelta(seconds=elapsed_time))))

if __name__ == '__main__':
    main()