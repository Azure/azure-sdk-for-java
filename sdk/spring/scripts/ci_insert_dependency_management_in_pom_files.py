# Python version 3.4 or higher is required to run this script.
#
# This script is used to insert dependency management into ./sdk/spring/**/pom*.xml.
#
# Here is sample of inserted fragment:
#    <dependencyManagement>
#       <dependency>
#         <groupId>org.springframework.boot</groupId>
#         <artifactId>spring-boot-dependencies</artifactId>
#         <version>${spring_boot_dependencies_version}</version>
#         <type>pom</type>
#         <scope>import</scope>
#       </dependency>
#       <dependency>
#         <groupId>org.springframework.cloud</groupId>
#         <artifactId>spring-cloud-dependencies</artifactId>
#         <version>${spring_cloud_dependencies_version}</version>
#         <type>pom</type>
#         <scope>import</scope>
#       </dependency>
#    </dependencyManagement>
#
# Sample:
# 1. python .\sdk\spring\scripts\ci_insert_dependency_management_in_pom_files.py --spring_boot_dependencies_version 2.5.4 --spring_cloud_dependencies_version 2020.0.5
# 2. python .\sdk\spring\scripts\ci_insert_dependency_management_in_pom_files.py -b 2.5.4 -c 2020.0.5
#
# The script must be run at the root of azure-sdk-for-java.


import os
import time
import argparse

from log import log


def main():
    start_time = time.time()
    change_to_root_dir()
    args = get_args()
    log.set_log_level(args.log)
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    add_dependency_management_for_all_poms_files_in_directory("./sdk/spring", args.spring_boot_dependencies_version, args.spring_cloud_dependencies_version)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def get_args():
    parser = argparse.ArgumentParser(description='Insert dependencyManagement in pom files.')
    parser.add_argument('-b', '--spring_boot_dependencies_version', type=str, required=True)
    parser.add_argument('-c', '--spring_cloud_dependencies_version', type=str, required=True)
    parser.add_argument(
        '--log',
        type=str,
        choices=['debug', 'info', 'warn', 'error', 'none'],
        required=False,
        default='info',
        help='Set log level.'
    )
    return parser.parse_args()


def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def add_dependency_management_for_all_poms_files_in_directory(directory, spring_boot_dependencies_version, spring_cloud_dependencies_version):
    for root, dirs, files in os.walk(directory):
        for file_name in files:
            if file_name.startswith('pom') and file_name.endswith('.xml'):
                file_path = root + os.sep + file_name
                add_dependency_management_for_file(file_path, spring_boot_dependencies_version, spring_cloud_dependencies_version)


def add_dependency_management_for_file(file_path, spring_boot_dependencies_version, spring_cloud_dependencies_version):
    log.info("Add dependency management for file: " + file_path)
    with open(file_path, 'r', encoding='utf-8') as pom_file:
        pom_file_content = pom_file.read()
        insert_position = get_insert_position(pom_file_content)
        insert_content = get_insert_content(pom_file_content, spring_boot_dependencies_version, spring_cloud_dependencies_version)
        new_content = pom_file_content[:insert_position] + insert_content + pom_file_content[insert_position:]
        with open(file_path, 'r+', encoding='utf-8') as updated_pom_file:
            updated_pom_file.writelines(new_content)


def get_insert_position(pom_file_content):
    if contains_dependency_management(pom_file_content):
        return pom_file_content.find('<dependencies>') + len('<dependencies>')
    else:
        return pom_file_content.find('<dependencies>')


def contains_dependency_management(pom_file_content):
    return pom_file_content.find('<dependencyManagement>') != -1


def get_insert_content(pom_file_content, spring_boot_dependencies_version, spring_cloud_dependencies_version):
    if contains_dependency_management(pom_file_content):
        return get_dependency_content(spring_boot_dependencies_version, spring_cloud_dependencies_version)
    else:
        return get_dependency_management_content(spring_boot_dependencies_version, spring_cloud_dependencies_version)


def get_dependency_content(spring_boot_dependencies_version, spring_cloud_dependencies_version):
    return """
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>{}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
      <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dependencies</artifactId>
        <version>{}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
""".format(spring_boot_dependencies_version, spring_cloud_dependencies_version)


def get_dependency_management_content(spring_boot_dependencies_version, spring_cloud_dependencies_version):
    dependency_content = get_dependency_content(spring_boot_dependencies_version, spring_cloud_dependencies_version)
    return """
  <dependencyManagement>
    <dependencies>
{}
    </dependencies>
  </dependencyManagement>
""".format(dependency_content)


if __name__ == '__main__':
    main()
