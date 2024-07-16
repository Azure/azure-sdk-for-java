############################################################################################################################################
# This script is used to get all 3rd party dependencies managed by spring-boot-dependencies and spring-cloud-dependencies.
#
# How to use this script.
#  1. Get `SPRING_BOOT_VERSION` from https://github.com/spring-projects/spring-boot/tags.
#     Get `SPRING_CLOUD_VERSION` from https://github.com/spring-cloud/spring-cloud-release/tags.
#     Note that spring-cloud version should compatible with spring-boot version.
#     Refs: https://spring.io/projects/spring-cloud.
#  2. Run command: `python .\sdk\spring\scripts\get_spring_boot_managed_external_dependencies.py -b 3.0.0-M5 -c 2022.0.0-M5`.
#     Or `python .\sdk\spring\scripts\get_spring_boot_managed_external_dependencies.py --spring_boot_dependencies_version 3.0.0-M5 --spring_cloud_dependencies_version 2022.0.0-M5`.
#  3. Then a file named `spring_boot_${SPRING_BOOT_VERSION}_managed_external_dependencies.txt` will be created.
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

MAVEN_NAME_SPACE = {'maven': 'http://maven.apache.org/POM/4.0.0'}


def get_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-b', '--spring_boot_dependencies_version', type = str, required = True)
    parser.add_argument('-c', '--spring_cloud_dependencies_version', type = str, required = True)
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
    return args


def get_root_poms(spring_boot_dependencies_version, spring_cloud_dependencies_version):
    return [
        'org.springframework.boot:spring-boot-starter-parent;{}'.format(spring_boot_dependencies_version),
        'org.springframework.boot:spring-boot-dependencies;{}'.format(spring_boot_dependencies_version),
        'org.springframework.cloud:spring-cloud-dependencies;{}'.format(spring_cloud_dependencies_version)
    ]


def get_spring_boot_managed_external_dependencies_file_name(spring_boot_dependencies_version):
    return 'sdk/spring/scripts/spring_boot_{}_managed_external_dependencies.txt'.format(spring_boot_dependencies_version)


def main():
    start_time = time.time()
    change_to_repo_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    dependency_dict = {}
    args = get_args()
    spring_boot_dependencies_version = args.spring_boot_dependencies_version
    spring_cloud_dependencies_version = args.spring_cloud_dependencies_version
    for root_pom in get_root_poms(spring_boot_dependencies_version, spring_cloud_dependencies_version):
        update_dependency_dict(dependency_dict, root_pom)
    output_version_dict_to_file(dependency_dict, get_spring_boot_managed_external_dependencies_file_name(spring_boot_dependencies_version))
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def change_to_repo_root_dir():
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
        plugin_dict = {}
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
            update_plugin_dict(parent_project_element, property_dict, plugin_dict)
        update_property_dict(project_element, property_dict)
        update_plugin_dict(project_element, property_dict, plugin_dict)
        # get dependencies
        build_elements = project_element.findall('./maven:build/maven:pluginManagement/maven:plugins/maven:plugin', MAVEN_NAME_SPACE) + project_element.findall('./maven:dependencyManagement/maven:dependencies/maven:dependency', MAVEN_NAME_SPACE)
        for dependency_element in build_elements:
            artifact_id = dependency_element.find('./maven:artifactId', MAVEN_NAME_SPACE).text.strip(' ')
            try:
                group_id = dependency_element.find('./maven:groupId', MAVEN_NAME_SPACE).text.strip(' ${}')
            except AttributeError:
                if artifact_id == 'maven-deploy-plugin' or 'maven-jar-plugin' or 'maven-javadoc-plugin' or 'maven-release-plugin' or 'maven-source-plugin':
                    group_id = 'org.apache.maven.plugins'
            # some group_id contain 'project.groupId', so put project_version first.
            if group_id in property_dict:
                group_id = property_dict[group_id]
            try:
                version = dependency_element.find('./maven:version', MAVEN_NAME_SPACE).text.strip(' ${}')
            except AttributeError:
                version = plugin_dict[artifact_id]
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


def update_plugin_dict(project_element, property_dict, plugin_dict):
    build_elements = project_element.findall('./maven:build/maven:pluginManagement/maven:plugins/maven:plugin', MAVEN_NAME_SPACE)
    for dependency_element in build_elements:
        artifact_id = dependency_element.find('./maven:artifactId', MAVEN_NAME_SPACE).text.strip(' ')
        try:
            version = dependency_element.find('./maven:version', MAVEN_NAME_SPACE).text.strip(' ${}')
            plugin_dict[artifact_id] = version
        except AttributeError:
            if property_dict.get(artifact_id+'.version') is not None:
                plugin_dict[artifact_id] = property_dict[artifact_id+'.version']
            elif artifact_id == 'native-maven-plugin':
                plugin_dict[artifact_id] = property_dict['native-build-tools-plugin.version']
            elif artifact_id == 'git-commit-id-maven-plugin':
                plugin_dict[artifact_id] = property_dict['git-commit-id-plugin.version']


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


if __name__ == '__main__':
    # unittest.main()
    main()
