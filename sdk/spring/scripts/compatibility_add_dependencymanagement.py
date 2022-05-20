# Python version 3.4 or higher is required to run this script.
#
# This script is used to insert dependency management into ./sdk/spring/**/pom*.xml.
#
# Here is sample of inserted fragment:
#    <properties>
#       <spring-boot-dependencies-version>${spring_boot_dependencies_version}</spring-boot-dependencies-version>
#       <spring-cloud-dependencies-version>${spring_cloud_dependencies_version}</spring-cloud-dependencies-version>
#    </properties>
#
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
# 1. python .\sdk\spring\scripts\compatibility_add_dependencymanagement.py
#
# The script must be run at the root of azure-sdk-for-java.


import os
import time
import json

from log import log


def main():
    start_time = time.time()
    change_to_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    spring_cloud_version = get_spring_cloud_version("./sdk/spring/spring-cloud-azure-supported-spring.json")
    add_dependency_management_for_all_poms_files_in_directory("./sdk/spring", spring_cloud_version)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def get_spring_cloud_version(filepath):
    spring_boot_version = os.getenv("SPRING_CLOUD_AZURE_TEST_SUPPORTED_SPRING_BOOT_VERSION")
    spring_cloud_version = "0"
    with open(filepath, 'r') as file:
        data = json.load(file)
    for entry in data:
        for key in entry:
            if entry[key] == spring_boot_version:
            # if entry[key] == "2.6.7":
                spring_cloud_version = entry["spring-cloud-version"]
                print("Get spring-cloud version:"+spring_cloud_version)
                break
    return spring_cloud_version



def add_dependency_management_for_all_poms_files_in_directory(directory, spring_boot_version):
    for root, dirs, files in os.walk(directory):
        for file_name in files:
            if file_name.startswith('pom') and file_name.endswith('.xml'):
                file_path = root + os.sep + file_name
                add_dependency_management_for_file(file_path, spring_boot_version)


def add_dependency_management_for_file(file_path, spring_boot_version):
    log.info("Add dependency management for file: " + file_path)
    with open(file_path, 'r', encoding='utf-8') as pom_file:
        pom_file_content = pom_file.read()
        insert_position = pom_file_content.find('<dependencies>')
        insert_content = get_dependency_management_content()
        new_content = pom_file_content[:insert_position] + insert_content + pom_file_content[insert_position:]
        if '<properties>' not in pom_file_content:
            insert_position = pom_file_content.find('<name>')
            insert_content = get_properties_contend_with_tag(spring_boot_version)
            finally_content = new_content[:insert_position] + insert_content + new_content[insert_position:]
            with open(file_path, 'r+', encoding='utf-8') as updated_pom_file:
                updated_pom_file.writelines(finally_content)
        else:
            insert_position = pom_file_content.find('</properties>')
            insert_content = get_properties_contend(spring_boot_version)
            finally_content = new_content[:insert_position] + insert_content + new_content[insert_position:]
            with open(file_path, 'r+', encoding='utf-8') as updated_pom_file:
                updated_pom_file.writelines(finally_content)


def get_dependency_management_content():
    return """
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${spring.boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
      <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dependencies</artifactId>
        <version>${spring.cloud.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
  
"""


def get_properties_contend_with_tag(spring_boot_version):
    return """
  <properties>
    <spring.boot.version>${env.SPRING_CLOUD_AZURE_TEST_SUPPORTED_SPRING_BOOT_VERSION}</spring.boot.version>
    <spring.cloud.version>"""+spring_boot_version+"""</spring.cloud.version>
  </properties>
  
    """


def get_properties_contend(spring_boot_version):
    return """
    <spring.boot.version>${env.SPRING_CLOUD_AZURE_TEST_SUPPORTED_SPRING_BOOT_VERSION}</spring.boot.version>
    <spring.cloud.version>"""+spring_boot_version+"""</spring.cloud.version>
    """


if __name__ == '__main__':
    main()
