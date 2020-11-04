# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.4 or higher is required to run this script.

# Use case: Runs compilation, testing, and linting for the passed artifacts and POM artifacts.
#
# The artifacts must exist in the module list of pom.xml in the root of azure-sdk-for-java. Artifacts may be passed in two ways:
#
# 1. Comma separated list of relative POM paths.
# 2. Comma separated list of groupId:artifactId identifiers.
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

baseCommand = 'mvn clean install -f pom.xml -pl "{}" -am {}'
xmlNamespace = '{http://maven.apache.org/POM/4.0.0}'

def getArtifactsFromPOM(pomPath: str, artifacts: list, debug: bool):
    # Skip files that don't exist as there still may be artifacts to build.
    if not os.path.exists(pomPath):
        print("POM {} doesn't exist, skipping".format(pomPath))
        return

    # Turn the POM into an XML tree so we can walk it.
    tree = ET.parse(pomPath)
    modulesElement = tree.getroot().find(xmlNamespace + 'modules')

    # If the POM has a <modules> tag assume that it is an aggregate POM.
    if modulesElement != None:
        pomBasedir = os.path.dirname(pomPath)
        for modulePomElement in modulesElement.iterfind(xmlNamespace + 'module'):
            moduleName = modulePomElement.text
            modulePomPath = os.path.normpath(os.path.join(pomBasedir, moduleName, 'pom.xml'))

            if debug:
                print('Getting module artifact for {} from aggregator POM {}'.format(moduleName.split('/')[-1], pomPath))

            getArtifactsFromPOM(modulePomPath, artifacts, debug)

    # Otherwise grab its groupId and artifactId to determine the artifact identifier.
    else:
        groupId = tree.getroot().findtext(xmlNamespace + "groupId")
        artifactId = tree.getroot().findtext(xmlNamespace + "artifactId")
        artifactIdentifier = '{}:{}'.format(groupId, artifactId)

        if debug:
            print('Adding artifact {} for POM file {}'.format(artifactIdentifier, pomPath))

        artifacts.append(artifactIdentifier)

def main():
    parser = argparse.ArgumentParser(description='Runs compilation, testing, and linting for the passed artifacts.')
    parser.add_argument('--artifacts', '--a', type=str, default=None, help='Comma separated list of groupId:artifactId identifiers')
    parser.add_argument('--poms', '--p', type=str, default=None, help='Comma separated list of POM paths')
    parser.add_argument('--skip-tests', '--st', action='store_true', help='Skips running tests')
    parser.add_argument('--skip-javadocs', '--sj', action='store_true', help='Skips javadoc generation')
    parser.add_argument('--skip-checkstyle', '--sc', action='store_true', help='Skips checkstyle linting')
    parser.add_argument('--skip-spotbugs', '--ss', action='store_true', help='Skips spotbugs linting')
    parser.add_argument('--skip-revapi', '--sr', action='store_true', help='Skips revapi linting')
    parser.add_argument('--debug', '--d', action='store_true', help='Runs the script with debug logging')
    args = parser.parse_args()

    if args.artifacts == None and args.poms == None:
        raise ValueError('--artifacts/--a or --poms/--p must be passed.')

    debug = args.debug

    buildArtifacts = []
    if args.poms != None:
        for pom in args.poms.split(','):
            getArtifactsFromPOM(os.path.abspath(pom), buildArtifacts, debug)

    if args.artifacts != None:
        buildArtifacts.extend(args.artifacts.split(','))

    # If all passed POMs are invalid fail.
    if buildArtifacts.count == 0:
        raise ValueError('No build artifacts found.')

    skipArguments = []
    if args.skip_tests:
        skipArguments.append('-DskipTests')

    if args.skip_javadocs:
        skipArguments.append('"-Dmaven.javadocs.skip=true"')

    if args.skip_checkstyle:
        skipArguments.append('"-Dcheckstyle.skip=true"')
    
    if args.skip_spotbugs:
        skipArguments.append('"-Dspotbugs.skip=true"')
    
    if args.skip_revapi:
        skipArguments.append('"-Drevapi.skip=true"')

    mavenCommand = baseCommand.format(','.join(list(set(buildArtifacts))), ' '.join(skipArguments))

    print('Running Maven command: {}'.format(mavenCommand))

    os.system(mavenCommand)

if __name__ == '__main__':
    main()
