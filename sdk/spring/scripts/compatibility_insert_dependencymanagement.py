# Python version 3.4 or higher is required to run this script.
#
# This script is used to insert dependency management, properties and repositories into ./sdk/spring/**/pom*.xml.
#
# Sample:
# 1. python .\sdk\spring\scripts\compatibility_insert_dependencymanagement.py --spring_boot_dependencies_version 2.7.0 --spring_cloud_dependencies_version 2021.0.3
# 2. python .\sdk\spring\scripts\compatibility_insert_dependencymanagement.py -b 2.7.0 -c 2021.0.3
#
# The script must be run at the root of azure-sdk-for-java.


import os
import time
import argparse

from log import log


def get_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-b', '--spring_boot_dependencies_version', type = str, required = True)
    parser.add_argument('-c', '--spring_cloud_dependencies_version', type = str, required = True)
    return parser.parse_args()


def main():
    start_time = time.time()
    change_to_repo_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    add_dependency_management_for_all_poms_files_in_directory("./sdk/spring", get_args().spring_boot_dependencies_version, get_args().spring_cloud_dependencies_version)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def change_to_repo_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def add_dependency_management_for_all_poms_files_in_directory(directory, spring_boot_dependencies_version, spring_cloud_dependencies_version):
    for root, dirs, files in os.walk(directory):
        for file_name in files:
            if file_name.startswith('pom') and file_name.endswith('.xml'):
                file_path = root + os.sep + file_name
                add_dependency_management_for_file(file_path, spring_boot_dependencies_version, spring_cloud_dependencies_version)


def add_dependency_management_for_file(file_path, spring_boot_dependencies_version, spring_cloud_dependencies_version):
    spring_cloud_version = spring_cloud_dependencies_version
    log.info("Add dependency management for file: " + file_path)
    if file_path == "./sdk/spring\pom.xml":
        return
    if spring_cloud_version.endswith("-SNAPSHOT"):
        with open(file_path, 'r', encoding = 'utf-8') as pom_file:
            pom_file_content = pom_file.read()
            insert_position = get_repo_position(pom_file_content)
            insert_content = get_repo_content(pom_file_content)
            repo_content = pom_file_content[:insert_position] + insert_content + pom_file_content[insert_position:]
            with open(file_path, 'r+', encoding = 'utf-8') as updated_pom_file:
                updated_pom_file.writelines(repo_content)
    with open(file_path, 'r', encoding = 'utf-8') as pom_file:
        pom_file_content = pom_file.read()
        insert_position = get_dependency_management_position(pom_file_content)
        insert_content = get_dependency_management_content(pom_file_content)
        dependency_content = pom_file_content[:insert_position] + insert_content + pom_file_content[insert_position:]
        insert_position = get_prop_position(pom_file_content)
        insert_content = get_prop_content(pom_file_content, spring_boot_dependencies_version, spring_cloud_dependencies_version)
        prop_content = dependency_content[:insert_position] + insert_content + dependency_content[insert_position:]
        with open(file_path, 'r+', encoding = 'utf-8') as updated_pom_file:
            updated_pom_file.writelines(prop_content)


def contains_dependency_management(pom_file_content):
    return pom_file_content.find("<dependencyManagement>") != -1


def get_dependency_management_position(pom_file_content):
    if contains_dependency_management(pom_file_content):
        return pom_file_content.find("</dependencies>")
    else:
        return pom_file_content.find("<dependencies>")


def get_dependency_management_content(pom_file_content):
    if contains_dependency_management(pom_file_content):
        return get_dependency_management_content_without_tag()
    else:
        return """
  <dependencyManagement>
    <dependencies>
      {}
    </dependencies>
  </dependencyManagement>
  
""".format(get_dependency_management_content_without_tag())


def get_dependency_management_content_without_tag():
    return """
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
    """


def contains_repositories(pom_file_content):
    return pom_file_content.find("<repositories>") != -1


def get_repo_position(pom_file_content):
    if contains_repositories(pom_file_content):
        return pom_file_content.find("</repositories>")
    else:
        return pom_file_content.find("<build>")


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


def get_prop_content(pom_file_content, spring_boot_dependencies_version, spring_cloud_dependencies_version):
    if contains_properties(pom_file_content):
        return get_properties_content(spring_boot_dependencies_version, spring_cloud_dependencies_version)
    else:
        return get_properties_content_with_tag(spring_boot_dependencies_version, spring_cloud_dependencies_version)


def get_properties_content_with_tag(spring_boot_dependencies_version, spring_cloud_dependencies_version):
    return """
  <properties>
    {}
  </properties>
  
  """.format(get_properties_content(spring_boot_dependencies_version, spring_cloud_dependencies_version))


def get_properties_content(spring_boot_dependencies_version, spring_cloud_dependencies_version):
    return """
    <spring.boot.version>{}</spring.boot.version>
    <spring.cloud.version>{}</spring.cloud.version>
  """.format(spring_boot_dependencies_version, spring_cloud_dependencies_version)


if __name__ == '__main__':
    main()
