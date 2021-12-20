# Python version 3.4 or higher is required to run this script.
#
# This script works for ci.
#
# How to use:
# 1. Change `SPRING_BOOT_DEPENDENCIES_VERSION` to the latest version in this script manually.
# 2. Then the ci will automatically run command `python .\sdk\spring\scripts\ci_add_spring_boot_dependencies_management.py`.
#
# The script must be run at the root of azure-sdk-for-java.

import os
import time
from log import log

SPRING_BOOT_DEPENDENCIES_VERSION = '2.6.1'

def add_dependency_management(c1,c2):
    for root, dirs, files in os.walk("./sdk/spring"):
    # for root, _, files in os.walk("D:/java/azure-sdk-for-java/sdk/spring/"):
        for file_name in files:
            file_path = root + os.sep + file_name
            if file_name.startswith('pom') and file_name.endswith('.xml'):
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                    pos1 = content.find('<dependencies>')
                    pos2 = content.find('<dependencyManagement>')
                    if pos2 != -1:
                        print("processing:" + file_path)
                        print("add dependency management...")
                        content = content[:pos2+41] + c1 + content[pos2+41:]
                        with open(file_path, 'r+', encoding='utf-8') as f:
                            f.writelines(content)
                    else:
                        print("processing:" + file_path)
                        print("add dependency management...")
                        content = content[:pos1] + c2 + content[pos1:]
                        with open(file_path, 'r+', encoding='utf-8') as f:
                            f.writelines(content)

def add_dependency_management_all():
    cores = '\n        <groupId>org.springframework.boot</groupId>'
    cores += '\n        <artifactId>spring-boot-dependencies</artifactId>'
    cores += '\n        <version>'+SPRING_BOOT_DEPENDENCIES_VERSION+'</version><!-- version test for ci -->'
    cores += '\n        <type>pom</type>'
    cores += '\n        <scope>import</scope>'

    content1 = '\n      <dependency>' + cores + '\n      </dependency>\n'
    content2 = '\n  <dependencyManagement>' + '\n    <dependencies>' + '\n      <dependency>' \
            + cores + '\n      </dependency>' + '\n    </dependencies>' + '\n  </dependencyManagement>\n\n'
    add_dependency_management(content1, content2)


def main():
    start_time = time.time()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    add_dependency_management_all()
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))

if __name__ == '__main__':
    main()
