# Python version 3.4 or higher is required to run this script.
#
# This script is used to insert dependency management, properties and repositories into ./sdk/spring/**/pom*.xml.
#
# Sample:
# 1. python .\sdk\spring\scripts\compatibility_insert_dependencymanagement.py
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
    add_dependency_management_for_all_poms_files_in_directory("./sdk/spring")
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def add_dependency_management_for_all_poms_files_in_directory(directory):
    for root, dirs, files in os.walk(directory):
        for file_name in files:
            if file_name.startswith('pom') and file_name.endswith('.xml'):
                file_path = root + os.sep + file_name
                add_dependency_management_for_file(file_path)


def contains_repositories(pom_file_content):
    return pom_file_content.find("<repositories>") != -1


def get_repo_position(pom_file_content):
    if contains_repositories(pom_file_content):
        return pom_file_content.find("</repositories>")
    else:
        return pom_file_content.find("<properties>")


def get_repo_content_without_tag():
    return """
    <repository>
      <id>repository.springframework.maven.milestone</id>
      <name>Spring Framework Maven Milestone Repository</name>
      <url>https://repo.spring.io/snapshot/</url>
    </repository>
  """


def get_repo_content(pom_file_content):
    if contains_repositories(pom_file_content):
        return get_repo_content_without_tag()
    else:
        return """  
  <repositories>
    {}
  </repositories>
  """.format(get_repo_content_without_tag())


def contains_properties(pom_file_content):
    return pom_file_content.find("<properties>") != -1


def get_prop_position(pom_file_content):
    if contains_properties(pom_file_content):
        return pom_file_content.find("</properties>")
    else:
        return pom_file_content.find("<name>")


def get_prop_content(pom_file_content):
    if contains_properties(pom_file_content):
        return get_properties_contend()
    else:
        return get_properties_contend_with_tag()


def add_dependency_management_for_file(file_path):
    spring_boot_version = os.getenv("SPRING_CLOUD_AZURE_TEST_SUPPORTED_SPRING_BOOT_VERSION")
    spring_cloud_version = []
    with open("./sdk/spring/spring-cloud-azure-supported-spring.json", 'r') as file:
        data = json.load(file)
    for entry in data:
        for key in entry:
            if spring_boot_version == entry[key]:
                spring_cloud_version = entry["spring-cloud-version"]
    # spring_cloud_version = os.getenv("SPRING_CLOUD_AZURE_TEST_SUPPORTED_SPRING_CLOUD_VERSION")
    # print(spring_cloud_version)
    log.info("Add dependency management for file: " + file_path)
    with open(file_path, 'r', encoding = 'utf-8') as pom_file:
        pom_file_content = pom_file.read()
        insert_position = pom_file_content.find('<dependencies>')
        insert_content = get_dependency_management_content()
        dependency_content = pom_file_content[:insert_position] + insert_content + pom_file_content[insert_position:]
        insert_position = get_prop_position(pom_file_content)
        insert_content = get_prop_content(pom_file_content)
        prop_content = dependency_content[:insert_position] + insert_content + dependency_content[insert_position:]
        with open(file_path, 'r+', encoding = 'utf-8') as updated_pom_file:
            updated_pom_file.writelines(prop_content)
    if spring_cloud_version.endswith("-SNAPSHOT"):
        with open(file_path, 'r', encoding = 'utf-8') as pom_file:
            pom_file_content = pom_file.read()
            insert_position = get_repo_position(pom_file_content)
            insert_content = get_repo_content(pom_file_content)
            repo_content = pom_file_content[:insert_position] + insert_content + pom_file_content[insert_position:]
            with open(file_path, 'r+', encoding = 'utf-8') as updated_pom_file:
                updated_pom_file.writelines(repo_content)


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


def get_properties_contend_with_tag():
    return """
  <properties>
    {}
  </properties>
  
  """.format(get_properties_contend())


def get_properties_contend():
    return """
    <spring.boot.version>${env.SPRING_CLOUD_AZURE_TEST_SUPPORTED_SPRING_BOOT_VERSION}</spring.boot.version>
    <spring.cloud.version>${env.SPRING_CLOUD_AZURE_TEST_SUPPORTED_SPRING_CLOUD_VERSION}</spring.cloud.version>
  """


if __name__ == '__main__':
    main()
