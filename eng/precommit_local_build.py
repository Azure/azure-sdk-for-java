# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.4 or higher is required to run this script.

# Use case: Runs compilation, testing, and linting for the passed artifacts and POM artifacts.
#
# Parameters:
#
# '--artifacts'/'--a': A comma separated list of artifact identifiers (groupId:artifactId)
# '--poms'/'--p': A comma separated list of pom.xml paths
# '--skip-tests'/'--st': Skips running tests
# '--skip-javadocs'/'--sj': Skips javadoc generation
# '--skip-checkstyle'/'--sc': Skips checkstyle linting
# '--skip-spotbugs'/'--ss': Skips spotbugs linting
# '--skip-revapi'/'--sr': Skips revapi linting
# '--command-only'/'--co': Indicates that only the command should be generated and not ran
# '--debug'/'--d': Generates command with verbose logging
#
# Example: Run precommit for artifact com.azure:azure-core
#
# python eng/precommit_local_build.py --a "com.azure:azure-core"
#
# Example: Run precommit for aggregate JAR sdk/storage/pom.xml
#
# python eng/precommit_local_build.py --p "sdk/storage/pom.xml"
#
# Example: Run precommit for artifact com.azure and aggregate JAR sdk/storage/pom.xml and skip testing
#
# python eng/precommit_local_build.py --a "com.azure:azure-core" --p "sdk/storage/pom.xml" --st
#
# The script must be run at the root of azure-sdk-for-java.

import argparse
import os
import xml.etree.ElementTree as ET

base_command = 'mvn clean install -f pom.xml -pl "{}" -am "-Dgpg.skip=true" {}'
xml_namespace = '{http://maven.apache.org/POM/4.0.0}'

def get_artifacts_from_pom(pom_path: str, build_artifacts: list, debug: bool):
    # Skip files that don't exist as there still may be artifacts to build.
    if not os.path.exists(pom_path):
        print("POM {} doesn't exist, skipping".format(pom_path))
        return

    # Turn the POM into an XML tree so we can walk it.
    tree = ET.parse(pom_path)
    modules_element = tree.getroot().find(xml_namespace + 'modules')

    # If the POM has a <modules> tag assume that it is an aggregate POM.
    if modules_element != None:
        pom_basedir = os.path.dirname(pom_path)
        for module_element in modules_element.iterfind(xml_namespace + 'module'):
            module_name = module_element.text
            module_pom_path = os.path.normpath(os.path.join(pom_basedir, module_name, 'pom.xml'))

            if debug:
                print('Getting module artifact for {} from aggregator POM {}'.format(module_name.split('/')[-1], pom_path))

            get_artifacts_from_pom(module_pom_path, build_artifacts, debug)

    # Otherwise grab its groupId and artifactId to determine the artifact identifier.
    else:
        group_id = tree.getroot().findtext(xml_namespace + "groupId")
        artifact_id = tree.getroot().findtext(xml_namespace + "artifactId")
        artifact_identifier = '{}:{}'.format(group_id, artifact_id)

        if debug:
            print('Adding artifact {} for POM file {}'.format(artifact_identifier, pom_path))

        build_artifacts.append(artifact_identifier)

def main():
    parser = argparse.ArgumentParser(description='Runs compilation, testing, and linting for the passed artifacts.')
    parser.add_argument('--artifacts', '-a', type=str, default=None, help='Comma separated list of groupId:artifactId identifiers')
    parser.add_argument('--poms', '-p', type=str, default=None, help='Comma separated list of POM paths')
    parser.add_argument('--skip-tests', '-st', action='store_true', help='Skips running tests')
    parser.add_argument('--skip-javadocs', '-sj', action='store_true', help='Skips javadoc generation')
    parser.add_argument('--skip-checkstyle', '-sc', action='store_true', help='Skips checkstyle linting')
    parser.add_argument('--skip-spotbugs', '-ss', action='store_true', help='Skips spotbugs linting')
    parser.add_argument('--skip-revapi', '-sr', action='store_true', help='Skips revapi linting')
    parser.add_argument('--skip-readme', '-smd', action='store_true', help='Skips README validation')
    parser.add_argument('--skip-changelog', '-scl', action='store_true', help='Skips CHANGELOG validation')
    parser.add_argument('--command-only', '-co', action='store_true', help='Indicates that only the command should be generated and not ran')
    parser.add_argument('--debug', '-d', action='store_true', help='Generates command with verbose logging')
    args = parser.parse_args()

    if args.artifacts == None and args.poms == None:
        raise ValueError('--artifacts/--a or --poms/--p must be passed.')

    debug = args.debug

    build_artifacts = []
    if args.poms != None:
        for pom in args.poms.split(','):
            get_artifacts_from_pom(os.path.abspath(pom), build_artifacts, debug)

    if args.artifacts != None:
        build_artifacts.extend(args.artifacts.split(','))

    # If all passed POMs are invalid fail.
    if build_artifacts.count == 0:
        raise ValueError('No build artifacts found.')

    arguments = []
    if args.skip_tests:
        arguments.append('"-DskipTests=true"')

    if args.skip_javadocs:
        arguments.append('"-Dmaven.javadocs.skip=true"')

    if args.skip_checkstyle:
        arguments.append('"-Dcheckstyle.skip=true"')
    
    if args.skip_spotbugs:
        arguments.append('"-Dspotbugs.skip=true"')
    
    if args.skip_revapi:
        arguments.append('"-Drevapi.skip=true"')

    if not args.skip_readme:
        arguments.append('"-Dverify-readme"')

    if not args.skip_changelog:
        arguments.append('"-Dverify-changelog"')

    # If Checkstyle, Spotbugs, or RevApi is being ran install sdk-build-tools to ensure the linting configuration is up-to-date.
    if not args.skip_checkstyle or not args.skip_spotbugs or not args.skip_revapi:
        if debug:
            print('Installing sdk-build-tools as Checkstyle, Spotbugs, or RevApi linting is being performed.')
        
        os.system('mvn install -f ' + os.path.join('eng', 'code-quality-reports', 'pom.xml'))

    maven_command = base_command.format(','.join(list(set(build_artifacts))), ' '.join(arguments))

    print('Running Maven command: {}'.format(maven_command))

    if not args.command_only:
        os.system(maven_command)

if __name__ == '__main__':
    main()
