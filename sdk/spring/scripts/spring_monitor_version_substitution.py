# Python version 3.4 or higher is required to run this script.
#
# This script is used to update sdk\spring\spring-cloud-azure-starter-monitor-test\pom.xml before compatibility check.
# Sample:
# 1. python .\sdk\spring\scripts\spring_monitor_version_substitution.py -b 3.1.2
#
# The script must be run at the root of azure-sdk-for-java.


import time
from log import log
import os
import argparse
import re
import xml.etree.ElementTree as ET


def replace_version(file_path, current_version, new_version):
    with open(file_path, 'r') as file:
        lines = file.readlines()

    with open(file_path, 'w') as file:
        for line in lines:
            if '<!-- {x-version-update;org.springframework.boot:' in line and current_version in line:
                line = re.sub(current_version, new_version, line)
            file.write(line)


def main():
    start_time = time.time()
    change_to_repo_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    file_path = 'sdk/spring/spring-cloud-azure-starter-monitor-test/pom.xml'
    current_spring_boot_version = get_current_spring_boot_version(file_path)
    replace_version(file_path, current_spring_boot_version, get_args().spring_boot_version)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def get_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-b', '--spring-boot-version', type = str, required = True)
    return parser.parse_args()


def change_to_repo_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def get_current_spring_boot_version(filepath):
    tree = ET.parse(filepath)
    root = tree.getroot()
    return root.find("{http://maven.apache.org/POM/4.0.0}parent/{http://maven.apache.org/POM/4.0.0}version").text


if __name__ == '__main__':
    main()
