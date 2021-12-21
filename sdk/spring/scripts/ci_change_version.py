# Python version 3.4 or higher is required to run this script.
#
# This script works for ci.
#
# How to use:
# 1. Change `SPRING_BOOT_DEPENDENCIES_VERSION` to make sure it's consistent with the `SPRING_BOOT_DEPENDENCIES_VERSION`
#    version in 'ci_add_spring_boot_dependencies_management.py'.
# 2. Change 'ANOTHER_SPRING_BOOT_DEPENDENCIES_VERSION' to the last version in this script manually.
# 3. Then the ci will automatically run command `python .\sdk\spring\scripts\ci_change_version.py`.
#
# The script must be run at the root of azure-sdk-for-java.

import os
import time

SPRING_BOOT_DEPENDENCIES_VERSION = '2.6.1'
ANOTHER_SPRING_BOOT_DEPENDENCIES_VERSION = '2.5.4'


def replace(file_path, v1, v2):
    with open(file_path, encoding='utf-8') as f:
        content = f.read()
        content = content.replace(v1, v2)
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)

def change_ci_dependency_management():
    version_now = '<version>'+SPRING_BOOT_DEPENDENCIES_VERSION+'</version><!-- version test for ci -->'
    version_change = '<version>'+ANOTHER_SPRING_BOOT_DEPENDENCIES_VERSION+'</version><!-- version test for ci -->'
    for root, _, files in os.walk("./sdk/spring"):
    # for root, _, files in os.walk("D:/java/azure-sdk-for-java/sdk/spring/"):
        for file_name in files:
            file_path = root + os.sep + file_name
            if file_name.startswith('pom') and file_name.endswith('.xml'):
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                    pos = content.find('<artifactId>spring-boot-dependencies</artifactId>')
                    if pos != -1:
                        print("processing:" + file_path)
                        print("changing springboot version...")
                        replace(file_path, version_now, version_change)

def change_ci_update_versions():
    # file_path = '/ci_update_versions.py'
    file_path = './sdk/spring/scripts/ci_update_versions.py'
    version1 = "SPRING_BOOT_VERSION = '"+SPRING_BOOT_DEPENDENCIES_VERSION+"'"
    version2 = "SPRING_BOOT_VERSION = '"+ANOTHER_SPRING_BOOT_DEPENDENCIES_VERSION+"'"
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
        pos = content.find(version1)
        if pos != -1:
            print("processing:" + file_path)
            print("changing script version...")
            replace(file_path, version1, version2)

def main():
    start_time = time.time()
    print('Current working directory = {}.'.format(os.getcwd()))
    change_ci_dependency_management()
    change_ci_update_versions()
    elapsed_time = time.time() - start_time
    print('elapsed_time = {}'.format(elapsed_time))

if __name__ == '__main__':
    main()
