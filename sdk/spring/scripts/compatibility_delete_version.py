# Python version 3.4 or higher is required to run this script.
#
# This script is used to delete dependency version in ./sdk/spring/**/pom*.xml for compatibility check.
# Sample:
# 1. python .\sdk\spring\scripts\compatibility_delete_version.py --spring_boot_dependencies_version 3.2.0
# 2. python .\sdk\spring\scripts\compatibility_delete_version.py -b 3.2.0
#
# The script must be run at the root of azure-sdk-for-java.import time


import os
from os.path import join
import time
import argparse

from log import log
from _constants import (
    COMPATIBILITY_USAGE_TYPE,
    INTEGRATION_USAGE_TYPE,
    get_spring_boot_version_tag_prefix,
    SPRING_BOOT_MAJOR_3_VERSION_TAG_PREFIX,
    should_skip_artifacts_when_adding_dependency_management,
    should_skip_artifacts_when_adding_dependency_management_with_spring_version,
    is_integration_tests_artifact)


IGNORED_ARTIFACTS = {'com.github.tomakehurst:wiremock-jre8'}
IGNORED_SPRINGBOOT_ARTIFACTS = {
    "3.0.13": {
        SPRING_BOOT_MAJOR_3_VERSION_TAG_PREFIX + "net.bytebuddy:byte-buddy",
        SPRING_BOOT_MAJOR_3_VERSION_TAG_PREFIX + "net.bytebuddy:byte-buddy-agent",
        SPRING_BOOT_MAJOR_3_VERSION_TAG_PREFIX + "org.mockito:mockito-core"
    }
}


def get_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-b', '--spring_boot_dependencies_version', type=str, required=True)
    parser.add_argument(
        '-u',
        '--usage',
        type=str,
        choices=[COMPATIBILITY_USAGE_TYPE, INTEGRATION_USAGE_TYPE],
        default=COMPATIBILITY_USAGE_TYPE,
        help='Which usage type of this python script is target to use. The default is ' + COMPATIBILITY_USAGE_TYPE+ '.'
    )
    return parser.parse_args()


def get_ignored_artifacts(spring_boot_dependencies_version):
    return IGNORED_ARTIFACTS.union(IGNORED_SPRINGBOOT_ARTIFACTS.get(spring_boot_dependencies_version, {}))


def main():
    start_time = time.time()
    change_to_repo_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    args = get_args()
    spring_version_tag_prefix = get_spring_boot_version_tag_prefix(args.spring_boot_dependencies_version)
    ignored_spring_managed_external_artifacts = get_ignored_artifacts(args.spring_boot_dependencies_version)
    if COMPATIBILITY_USAGE_TYPE == args.usage:
        find_all_poms_do_version_control_for_compatibility(
            "./sdk/spring",
            args.spring_boot_dependencies_version,
            spring_version_tag_prefix,
            ignored_spring_managed_external_artifacts)
    elif INTEGRATION_USAGE_TYPE == args.usage:
        find_all_poms_do_version_control_for_integration(
            "./sdk/spring",
            spring_version_tag_prefix,
            ignored_spring_managed_external_artifacts)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def change_to_repo_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def find_all_poms_do_version_control_for_compatibility(directory, spring_boot_version, spring_version_tag_prefix, ignored_artifacts):
    external_dependencies_set = external_dependencies_managed_list(spring_version_tag_prefix)
    for root, dirs, files in os.walk(directory):
        for file_name in files:
            if file_name.startswith('pom') and file_name.endswith('.xml'):
                file_path = join(root, file_name)
                if (should_skip_artifacts_when_adding_dependency_management_with_spring_version(spring_boot_version, file_path)
                    or should_skip_artifacts_when_adding_dependency_management(file_path)):
                    log.warn("Skip deleting version for file: " + file_path)
                    continue
                delete_dependency_version(file_path, ignored_artifacts, external_dependencies_set)


def find_all_poms_do_version_control_for_integration(directory, spring_version_tag_prefix, ignored_artifacts):
    external_dependencies_set = external_dependencies_managed_list(spring_version_tag_prefix)
    for root, dirs, files in os.walk(directory):
        for file_name in files:
            if file_name.startswith('pom') and file_name.endswith('.xml'):
                file_path = join(root, file_name)
                if not is_integration_tests_artifact(file_path):
                    log.warn("Skip non-integration test module for file: " + file_path)
                    continue
                delete_dependency_version(file_path, ignored_artifacts, external_dependencies_set)


# Delete explicit versions, and use versions from Spring Boot or Spring Cloud BOMs
def delete_dependency_version(file_path, ignored_artifacts, external_dependencies):
    log.info("delete dependency version in " + file_path)
    with open(file_path, 'r', encoding='utf-8') as pom_file:
        lines = pom_file.readlines()
    with open(file_path, 'w', encoding='utf-8') as new_pom_file:
        for line in lines:
            if ';external_dependency} -->' not in line:
                new_pom_file.write(line)
            elif line.split(";")[1] in ignored_artifacts:
                new_pom_file.write(line)
            elif line.split(";")[1] not in external_dependencies:
                # listed in external-dependencies.txt but not managed by spring
                new_pom_file.write(line)


def external_dependencies_managed_list(version_tag_prefix):
    dependencies = set()
    with open(get_managed_file_name(version_tag_prefix), 'r', encoding='utf-8') as managed_file:
        lines = managed_file.readlines()
        for dependency in lines:
            dependencies.add(version_tag_prefix + dependency.split(";")[0])
    return dependencies


def get_managed_file_name(version_tag_prefix):
    spring_boo_dependencies_tag = version_tag_prefix + "org.springframework.boot:spring-boot-dependencies;"
    with open("./eng/versioning/external_dependencies.txt", "r", encoding='utf-8') as external_file:
        lines = external_file.readlines()
        for line in lines:
            if spring_boo_dependencies_tag in line:
                return "sdk/spring/scripts/spring_boot_{}_managed_external_dependencies.txt".format(line.split(";")[1].replace("\n", ""))


if __name__ == '__main__':
    main()
