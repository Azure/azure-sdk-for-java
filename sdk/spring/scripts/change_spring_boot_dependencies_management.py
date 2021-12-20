# Python version 3.4 or higher is required to run this script.
#
# This script works for ci.
#
# How to use:
# 1. Change `SPRING_BOOT_DEPENDENCIES_VERSION` to make sure it's consistent with the `SPRING_BOOT_DEPENDENCIES_VERSION`
#    version in 'add_spring_boot_dependencies_management.py'.
# 2. Change 'ANOTHER_SPRING_BOOT_DEPENDENCIES_VERSION' to the last version in this script manually.
# 3. Then the ci will automatically run command `python .\sdk\spring\scripts\change_spring_boot_dependencies_management.py`.
#
# The script must be run at the root of azure-sdk-for-java.

import os
import time
from log import log

SPRING_BOOT_DEPENDENCIES_VERSION = '2.6.1'
ANOTHER_SPRING_BOOT_DEPENDENCIES_VERSION = '2.5.4'


def replace(file_path, v1, v2):
    with open(file_path, encoding='utf-8') as f:
        content = f.read()
        content = content.replace(v1, v2)
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)

def change_dependency_management():
    version_now = '<version>'+SPRING_BOOT_DEPENDENCIES_VERSION+'</version><!-- version test for ci -->'
    vserion_change = '<version>'+ANOTHER_SPRING_BOOT_DEPENDENCIES_VERSION+'</version><!-- version test for ci -->'
    # for root, _, files in os.walk("./sdk/spring"):
    for root, _, files in os.walk("D:/java/azure-sdk-for-java/sdk/spring/"):
        for file_name in files:
            file_path = root + os.sep + file_name
            if file_name.startswith('pom') and file_name.endswith('.xml'):
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                    pos = content.find('<artifactId>spring-boot-dependencies</artifactId>')
                    if pos != -1:
                        # print("processing:" + file_path)
                        replace(file_path, version_now, vserion_change)



def main():
    start_time = time.time()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    change_dependency_management()
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))

if __name__ == '__main__':
    main()
