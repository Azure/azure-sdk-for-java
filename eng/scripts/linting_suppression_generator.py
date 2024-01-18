# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Use case: Generates, or updates, Checkstyle (checkstyle-suppressions.xml) and Spotbugs (spotbugs-exclude.xml)
# suppression files for the Java SDK.
#
# To use this tool, first run build with the following command:
#
# mvn checkstyle:check spotbugs:check -f <project> "-Dcheckstyle.failOnViolation=false" "-Dcheckstyle.failsOnError=false" "-Dspotbugs.failOnError=false"
#
# This will run the build, but will not fail on any Checkstyle or Spotbugs violations. This is necessary because
# the build will fail if there are any violations, and we need to have the Checkstyle and Spotbugs plugins complete
# as they generate files in /target which indicate the violations.
#
# Or, if you're looking to update the existing suppression files, run the following command:
#
# mvn checkstyle:check spotbugs:check -f <project> "-Dcheckstyle.failOnViolation=false" "-Dcheckstyle.failsOnError=false" "-Dspotbugs.failOnError=false" "-Dcheckstyle.suppressionsLocation" "-Dspotbugs.suppressionsLocation"
#
# This will run the build without any existing suppression files being used. And again, this will not fail on any
# Checkstyle or Spotbugs violations.
#
# Next, run this script with the following command:
#
# python linting_suppression_generator.py --project-folder <project folder>
#
# This will generate, or update, the suppression files in the project root directory. You can then run the build
# again without the flags to fail on errors and violations.
#
# Note: This script will not remove any existing suppression files. If you want to remove them, you will need to
# do so manually.
#
# At this time, this script only supports running 

import argparse
import os
from typing import Dict, List, Set, Tuple
import xml.etree.ElementTree as ET

# From this file get to the root path of the repo.
root_path = os.path.normpath(os.path.abspath(__file__) + '/../../../')

def generate_suppression_files(project_folder: str):
    project_folder = os.path.join(root_path, project_folder)
    if not os.path.exists(project_folder):
        print('The project folder does not exist: ' + project_folder)
        return
    
    generate_checkstyle_suppression_file(project_folder)
    generate_spotbugs_suppression_file(project_folder)

def generate_checkstyle_suppression_file(project_folder: str):
    # Get the path to the checkstyle violations file.
    checkstyle_violations_file = os.path.join(project_folder, 'target/checkstyle-result.xml')
    if not os.path.exists(checkstyle_violations_file):
        print('No Checkstyle violations file was found at: ' + checkstyle_violations_file)
        return
    
    # Checkstyle violations are stored in the following format:
    # <checkstyle>
    #   <file name="...">
    #     <error line="..." column="..." severity="..." message="..." source="..."/>
    #     ...
    #   </file>
    # </checkstyle>
    #
    # Where the file name is the full path to the file on disk.
    #
    # Parsing the violations will be stored in a set of tuples of:
    #
    # Set((file_name, message, source))
    #
    # Where the file_name will be turned into the Java file path (ex: /src/main/java/com/azure/.../MyClass.java -> com.azure...MyClass.java),
    # the message will be left as is, and the source will clean the built-in checks to just their name 
    # (ex: com.puppycrawl.tools.checkstyle.checks.javadoc.MissingJavadocMethodCheck -> MissingJavadocMethod).
    #
    # Normally, handwritten suppressions will omit the message, as writing them requires a manual review of the
    # violation. However, since we're generating these files, we'll include the message so that it's easier to
    # understand what the violation is for, and how to remedy it.
    violations: Set[Tuple[str, str, str]] = set()

    tree: ET.ElementTree = ET.parse(checkstyle_violations_file)
    # root will be the <checkstyle> element.
    file_elements: List[ET.Element] = tree.getroot().findall('file')

    for file_element in file_elements:
        file_name = os.path.normpath(file_element.attrib['name']).replace('\\', '/')

        # Sanitize the file name by looking for the first instance of src/main/java, src/samples/java, or src/test/java.
        # If none of those are found, then look for the first instance of azure-sdk-for-java.
        if file_name.find('src/main/java') != -1:
            file_name = file_name[file_name.find('src/main/java') + len('src/main/java/'):]
            file_name = file_name.replace('/', '.')
        elif file_name.find('src/samples/java') != -1:
            file_name = file_name[file_name.find('src/samples/java') + len('src/samples/java/'):]
            file_name = file_name.replace('/', '.')
        elif file_name.find('src/test/java') != -1:
            file_name = file_name[file_name.find('src/test/java') + len('src/test/java/'):]
            file_name = file_name.replace('/', '.')
        else:
            file_name = file_name[file_name.find('azure-sdk-for-java') + len('azure-sdk-for-java/'):]

        errors: List[ET.Element] = file_element.findall('error')
        for error in errors:
            message = error.attrib['message'].replace('"', '\'').replace('<', '&lt;').replace('>', '&gt;')
            source = error.attrib['source']
            if source.startswith('com.puppycrawl'):
                source = source[source.rfind('.') + 1:]

            violations.add((file_name, message, source))

    if len(violations) == 0:
        # Don't update the suppression file if there are no violations.
        # This could indicate that the build failed or was ran incorrectly.
        print('No Checkstyle violations were found.')
        return
    
    # Now that we have the violations, we can generate the suppression file.
    # The format of the suppression file is as follows:
    #
    # <?xml version="1.0" encoding="UTF-8"?>
    # <!DOCTYPE suppressions PUBLIC "-//Checkstyle//DTD SuppressionFilter Configuration 1.2//EN" "https://checkstyle.org/dtds/suppressions_1_2.dtd">
    # <suppressions>
    #   <suppress files="..." checks="..." message="..."/>
    # </suppressions>
    with open(file=os.path.join(project_folder, 'checkstyle-suppressions.xml'), mode='w') as checkstyle_suppressions:
        checkstyle_suppressions.write('<?xml version="1.0" encoding="UTF-8"?>\n')
        checkstyle_suppressions.write('<!DOCTYPE suppressions PUBLIC "-//Checkstyle//DTD SuppressionFilter Configuration 1.2//EN" "https://checkstyle.org/dtds/suppressions_1_2.dtd">\n')
        checkstyle_suppressions.write('<!-- This file is generated by the /eng/scripts/linting_suppression_generator.py script. -->\n\n')

        checkstyle_suppressions.write('<suppressions>\n')
        for violation in violations:
            checkstyle_suppressions.write(f'  <suppress files="{violation[0]}" checks="{violation[2]}" message="{violation[1]}"/>\n')
        checkstyle_suppressions.write('</suppressions>\n')

def generate_spotbugs_suppression_file(project_folder: str):
    # Get the path to the spotbugs violations file.
    spotbugs_violations_file = os.path.join(project_folder, 'target/spotbugs.xml')
    if not os.path.exists(spotbugs_violations_file):
        print('No Spotbugs violations file was found at: ' + spotbugs_violations_file)
        return
    
    # Spotbugs violations are stored in the following format:
    # <BugCollection>
    #   <file classname="...">
    #     <BugInstance type='...' priority='...' category='...' message='...' lineNumber='...' />
    #     ...
    #   </file>
    # </BugCollection>
    #
    # Parsing the violations will be stored in a dictionary of list of tuples of:
    # 
    # Dict(type, List[(classname, message, lineNumber)]
    #
    # Normally, handwritten suppressions will omit the message, as writing them requires a manual review of the
    # violation. However, since we're generating these files, we'll include the message so that it's easier to
    # understand what the violation is for, and how to remedy it.
    violations: Dict[str, Set[Tuple[str, str, str]]] = dict()

    tree: ET.ElementTree = ET.parse(spotbugs_violations_file)
    # root will be the <checkstyle> element.
    file_elements: List[ET.Element] = tree.getroot().findall('file')

    for file_element in file_elements:
        file_name = file_element.attrib['classname']

        errors: List[ET.Element] = file_element.findall('BugInstance')
        for error in errors:
            type = error.attrib['type']
            message = error.attrib['message'].replace('"', '\'').replace('<', '&lt;').replace('>', '&gt;')
            line_number = error.attrib['lineNumber']

            if type not in violations:
                violations[type] = set()
            
            violations[type].add((file_name, message, line_number))

    if len(violations) == 0:
        # Don't update the suppression file if there are no violations.
        # This could indicate that the build failed or was ran incorrectly.
        print('No Spotbugs violations were found.')
        return
    
    # Now that we have the violations, we can generate the suppression file.
    # The format of the suppression file is as follows:
    #
    # <?xml version="1.0" encoding="UTF-8"?>
    # <FindBugsFilter xmlns="https://github.com/spotbugs/filter/3.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    #     xsi:schemaLocation="https://github.com/spotbugs/filter/3.0.0 https://raw.githubusercontent.com/spotbugs/spotbugs/3.1.0/spotbugs/etc/findbugsfilter.xsd">
    #   <Match>
    #     <Bug pattern="..." />
    #     <Class name="..." />
    #   </Match>
    # </FindBugsFilter>
    #
    # If the same bug applies to multiple classes, then the <Class> element can be repeated:
    #
    # <Match>
    #   <Bug pattern="..." />
    #   <Or>
    #       <Class name="..." />
    #       <Class name="..." />
    #       ...
    #   </Or>
    # </Match>
    #
    # Before each class an XML comment will be added with the message and line number of the violation.
    # This is to make it easier to understand what the violation is for, and how to remedy it.
    with open(file=os.path.join(project_folder, 'spotbugs-exclude.xml'), mode='w') as spotbugs_suppressions:
        spotbugs_suppressions.write('<?xml version="1.0" encoding="UTF-8"?>\n\n')
        spotbugs_suppressions.write('<FindBugsFilter xmlns="https://github.com/spotbugs/filter/3.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"\n')
        spotbugs_suppressions.write('                xsi:schemaLocation="https://github.com/spotbugs/filter/3.0.0 https://raw.githubusercontent.com/spotbugs/spotbugs/3.1.0/spotbugs/etc/findbugsfilter.xsd">\n')
    
        for type in violations.keys():
            spotbugs_suppressions.write('  <Match>\n')
            bugs: Set(str, str, str) = violations[type]

            # If there is only one bug, then we can just write it out.
            if len(bugs) == 1:
                bug = bugs.pop()
                spotbugs_suppressions.write(f'    <!-- Line: {bug[2]}, Message: {bug[1]} -->\n')
                spotbugs_suppressions.write(f'    <Bug pattern="{type}" />\n')
                spotbugs_suppressions.write(f'    <Class name="{bug[0]}" />\n')
            else:
                # If there are multiple bugs, then we need to use the <Or> element.
                spotbugs_suppressions.write(f'    <Or>\n')
                for bug in bugs:
                    spotbugs_suppressions.write(f'      <!-- Line: {bug[2]}, Message: {bug[1]} -->\n')
                    spotbugs_suppressions.write(f'      <Class name="{bug[0]}" />\n')
                spotbugs_suppressions.write(f'    </Or>\n')

            spotbugs_suppressions.write('  </Match>\n')

        spotbugs_suppressions.write('</FindBugsFilter>\n')

def main():
    parser = argparse.ArgumentParser(description='Generate Checkstyle and Spotbugs suppression files for a project.')
    parser.add_argument('--project-folder', '-pf', help='The project to generate suppression files for.')
    args = parser.parse_args()
    #args.project_folder = '.\\sdk\\search\\azure-search-documents'
    if args.project_folder:
        generate_suppression_files(args.project_folder)
    else:
        print('Please provide a project name.')

if __name__ == '__main__':
    main()
