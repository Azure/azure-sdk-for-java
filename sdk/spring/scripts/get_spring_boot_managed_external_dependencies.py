############################################################################################################################################
# This script is used to get all 3rd party dependencies managed by spring-boot-dependencies and spring-cloud-dependencies.
#
# How to use this script.
#  1. Update `SPRING_BOOT_VERSION` and `SPRING_CLOUD_VERSION` in this script manually.
#     Note that spring-cloud version should compatible with spring-boot version.
#     Refs: https://spring.io/projects/spring-cloud
#  2. Run command `python .\sdk\spring\scripts\get_spring_boot_managed_external_dependencies.py`.
#  3. Then a file named `spring_boot_SPRING_BOOT_VERSION_managed_external_dependencies.txt` will be created.
#
# Please refer to ./README.md to get more information about this script.
############################################################################################################################################

import argparse
import os
import queue
import time
import urllib.request as request
import xml.etree.ElementTree as elementTree
from urllib.error import HTTPError

from log import log
from pom import Pom

SPRING_BOOT_VERSION = '2.6.3'
SPRING_CLOUD_VERSION = '2021.0.1'

ROOT_POMS = [
    'org.springframework.boot:spring-boot-starter-parent;{}'.format(SPRING_BOOT_VERSION),
    'org.springframework.boot:spring-boot-dependencies;{}'.format(SPRING_BOOT_VERSION),
    'org.springframework.cloud:spring-cloud-dependencies;{}'.format(SPRING_CLOUD_VERSION)
]
SPRING_BOOT_MANAGED_EXTERNAL_DEPENDENCIES_FILE_NAME = 'sdk/spring/scripts/spring_boot_{}_managed_external_dependencies.txt'.format(SPRING_BOOT_VERSION)
MAVEN_NAME_SPACE = {'maven': 'http://maven.apache.org/POM/4.0.0'}


def main():
    start_time = time.time()
    change_to_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    dependency_dict = {}
    for root_pom in ROOT_POMS:
        update_dependency_dict(dependency_dict, root_pom)
    output_version_dict_to_file(dependency_dict, SPRING_BOOT_MANAGED_EXTERNAL_DEPENDENCIES_FILE_NAME)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def update_dependency_dict(dependency_dict, root_pom_id):
    root_pom_info = root_pom_id.split(';')
    root_pom_group_artifact = root_pom_info[0]
    root_pom_group_info = root_pom_group_artifact.split(':')
    root_pom_group_id = root_pom_group_info[0]
    root_pom_artifact_id = root_pom_group_info[1]
    root_pom_version = root_pom_info[1]
    dependency_dict[root_pom_group_id + ':' + root_pom_artifact_id] = root_pom_version
    root_pom = Pom(
        root_pom_group_id,
        root_pom_artifact_id,
        root_pom_version,
        1
    )
    q = queue.Queue()
    q.put(root_pom)
    pom_count = 1
    log.info('Added root pom.depth = {}, url = {}.'.format(root_pom.depth, root_pom.to_url()))
    while not q.empty():
        pom = q.get()
        pom_url = pom.to_url()
        log.info('Get dependencies from pom. depth = {}, url = {}.'.format(pom.depth, pom_url))
        try:
            tree = elementTree.ElementTree(file = request.urlopen(pom_url))
        except HTTPError:
            log.warn('Error in open {}'.format(pom_url))
            continue
        project_element = tree.getroot()
        property_dict = {}
        parent_element = project_element.find('./maven:parent', MAVEN_NAME_SPACE)
        if parent_element is not None:
            # get properties from parent
            parent_group_id = parent_element.find('./maven:groupId', MAVEN_NAME_SPACE).text.strip(' ${}')
            parent_artifact_id = parent_element.find('./maven:artifactId', MAVEN_NAME_SPACE).text.strip(' ${}')
            parent_version = parent_element.find('./maven:version', MAVEN_NAME_SPACE).text.strip(' ${}')
            parent_pom = Pom(parent_group_id, parent_artifact_id, parent_version, pom.depth + 1)
            parent_pom_url = parent_pom.to_url()
            parent_tree = elementTree.ElementTree(file = request.urlopen(parent_pom_url))
            parent_project_element = parent_tree.getroot()
            log.debug('Get properties from parent pom. parent_pom_url = {}.'.format(parent_pom_url))
            update_property_dict(parent_project_element, property_dict)
        update_property_dict(project_element, property_dict)
        # get dependencies
        dependency_elements = project_element.findall('./maven:dependencyManagement/maven:dependencies/maven:dependency', MAVEN_NAME_SPACE)
        for dependency_element in dependency_elements:
            group_id = dependency_element.find('./maven:groupId', MAVEN_NAME_SPACE).text.strip(' ${}')
            # some group_id contain 'project.groupId', so put project_version first.
            if group_id in property_dict:
                group_id = property_dict[group_id]
            artifact_id = dependency_element.find('./maven:artifactId', MAVEN_NAME_SPACE).text.strip(' ')
            version = dependency_element.find('./maven:version', MAVEN_NAME_SPACE).text.strip(' ${}')
            key = group_id + ':' + artifact_id
            if version in property_dict:
                version = property_dict[version]
            if key not in dependency_dict:
                dependency_dict[key] = version
                log.debug('Dependency version added. key = {}, value = {}'.format(key, version))
            elif version != dependency_dict[key]:
                log.info('Dependency version skipped. key = {}, version = {}, dependency_dict[key] = {}.'.format(key, version, dependency_dict[key]))
            artifact_type = dependency_element.find('./maven:type', MAVEN_NAME_SPACE)
            artifact_scope = dependency_element.find('./maven:scope', MAVEN_NAME_SPACE)
            if artifact_type is not None and \
                artifact_scope is not None and \
                artifact_type.text.strip() == 'pom' and \
                artifact_scope.text.strip() == 'import':
                new_pom = Pom(group_id, artifact_id, version, pom.depth + 1)
                q.put(new_pom)
                pom_count = pom_count + 1
    log.info('Root pom summary. pom_count = {}, root_pom_url = {}'.format(pom_count, root_pom.to_url()))


def update_property_dict(project_element, property_dict):
    # get properties
    properties = project_element.find('maven:properties', MAVEN_NAME_SPACE)
    # some property contain 'project.version', so put 'project.version' into property_dict.
    version_element = project_element.find('./maven:version', MAVEN_NAME_SPACE)
    if version_element is None:
        version_element = project_element.find('./maven:parent/maven:version', MAVEN_NAME_SPACE)
    project_version = version_element.text.strip()
    property_dict['project.version'] = project_version
    # some property contain 'project.groupId', so put 'project.groupId' into property_dict.
    group_id_element = project_element.find('./maven:groupId', MAVEN_NAME_SPACE)
    if group_id_element is None:
        group_id_element = project_element.find('./maven:parent/maven:groupId', MAVEN_NAME_SPACE)
    group_id = group_id_element.text.strip()
    property_dict['project.groupId'] = group_id
    if properties is not None:
        for p in properties:
            key = p.tag.split('}', 1)[1]
            value_text = p.text
            if value_text is None:
                # sometimes we have tag with no text, like: <release.arguments/>
                continue
            value = value_text.strip(' ${}')
            if value in property_dict:
                value = property_dict[value]
            property_dict[key] = value
    # sometimes project_version contains '${foo}', so update project_version.
    if project_version.startswith('${'):
        property_dict['project.version'] = property_dict[project_version.strip(' ${}')]
    return property_dict


def output_version_dict_to_file(dependency_dict, output_file):
    output_file = open(output_file, 'w''')
    for key, value in sorted(dependency_dict.items()):
        output_file.write('{};{}\n'.format(key, value))
    output_file.close()


def print_dict(d):
    for key, value in d.items():
        print('key = {}, value = {}.'.format(key, value))


def init():
    parser = argparse.ArgumentParser(
        description='Get spring-boot managed external dependencies and write into {}'.format(SPRING_BOOT_MANAGED_EXTERNAL_DEPENDENCIES_FILE_NAME)
    )
    parser.add_argument(
        '--log',
        type = str,
        choices = ['debug', 'info', 'warn', 'error', 'none'],
        required = False,
        default = 'info',
        help = 'Set log level.'
    )
    args = parser.parse_args()
    log.set_log_level(args.log)
    # log.log_level_test()


if __name__ == '__main__':
    # unittest.main()
    init()
    main()
