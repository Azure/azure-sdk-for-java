# Python version 3.4 or higher is required to run this script.
#
# This script is used to delete dependency version in ./sdk/spring/**/pom*.xml for compatibility check.
# Sample:
# 1. python .\sdk\spring\scripts\compatibility_delete_version.py
#
# The script must be run at the root of azure-sdk-for-java.import time


import os
from os.path import join
import time

from log import log


def main():
    start_time = time.time()
    change_to_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    find_all_poms_do_version_control("./sdk/spring")
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def find_all_poms_do_version_control(directory):
    for root, dirs, files in os.walk(directory):
        for file_name in files:
            if file_name.startswith('pom') and file_name.endswith('.xml'):
                file_path = join(root, file_name)
                delete_dependency_version(file_path)
                # replace_banned_dependencies(file_path)


def delete_dependency_version(file_path):
    log.info("delete dependency version in " + file_path)
    with open(file_path, 'r', encoding = 'utf-8') as pom_file:
        lines = pom_file.readlines()
    with open(file_path, 'w', encoding = 'utf-8') as new_pom_file:
        for line in lines:
            if '<!-- {x-version-update;org.springframework' not in line:
                new_pom_file.write(line)


# def replace_banned_dependencies(file_path):
#     with open(file_path, 'r', encoding = 'utf-8') as pom_file:
#         pom_file_content = pom_file.read()
#         if pom_file_content.find('<!-- {x-include-update;org.springframework') != -1:
#             log.info("replace bannedDependencies in " + file_path)
#             insert_position = pom_file_content.find('''<bannedDependencies>
#               <includes>''') + len('''<bannedDependencies>
#               <includes>''')
#             new_content = pom_file_content[:insert_position] \
#                           + '\n                <include>org.springframework.*</include>\n                <include>org.springframework:*</include>' \
#                           + pom_file_content[insert_position:]
#             with open(file_path, 'r+', encoding = 'utf-8') as updated_pom_file:
#                 updated_pom_file.writelines(new_content)


if __name__ == '__main__':
    main()
