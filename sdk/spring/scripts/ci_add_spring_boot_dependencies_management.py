# Python version 3.4 or higher is required to run this script.
#
# This script works for ci.
#
# How to use:
# 1. Change `SPRING_BOOT_DEPENDENCIES_VERSION` and 'SPRING_CLOUD_DEPENDENCIES_VERSION'
#    to the right version in sdk/spring/ci.yml manually.
# 2. Then the ci will automatically run command
#    `python .\sdk\spring\scripts\ci_add_spring_boot_dependencies_management.py
#    --boot SPRING_BOOT_DEPENDENCIES_VERSION --cloud SPRING_CLOUD_DEPENDENCIES_VERSION`.
#
# The script must be run at the root of azure-sdk-for-java.

import os
import time
import argparse
from log import log, Log

def add_dependency_management(content1,content2):
    for root, _, files in os.walk("./sdk/spring"):
    # for root, _, files in os.walk("D:/java/azure-sdk-for-java/sdk/spring/"):
        for file_name in files:
            file_path = root + os.sep + file_name
            if file_name.startswith('pom') and file_name.endswith('.xml'):
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                    pos1 = content.find('<dependencies>')
                    pos2 = content.find('<dependencyManagement>')
                    if pos2 != -1:
                        log.info("processing:" + file_path)
                        content = content[:pos2+41] + content1 + content[pos2+41:]
                        with open(file_path, 'r+', encoding='utf-8') as f:
                            f.writelines(content)
                    else:
                        log.info("processing:" + file_path)
                        content = content[:pos1] + content2 + content[pos1:]
                        with open(file_path, 'r+', encoding='utf-8') as f:
                            f.writelines(content)

def add_dependency_management_all(spring_boot_dependencies_version, spring_cloud_dependencies_version):
    cores = """
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
    content = '\n  <dependencyManagement>' + '\n    <dependencies>' + cores + '    </dependencies>' + '\n  </dependencyManagement>\n\n'
    add_dependency_management(cores, content)


def main():
    parser = argparse.ArgumentParser(description='Add dependencies management in poms.')
    parser.add_argument('--spring_boot_dependencies_version', '--boot', type=str, required=True)
    parser.add_argument('--spring_cloud_dependencies_version', '--cloud', type=str, required=True)
    parser.add_argument(
        '--log',
        type=str,
        choices=['debug', 'info', 'warn', 'error', 'none'],
        required=False,
        default='info',
        help='Set log level.'
    )
    args = parser.parse_args()
    log_dict = {
        'debug': Log.DEBUG,
        'info': Log.INFO,
        'warn': Log.WARN,
        'error': Log.ERROR,
        'none': Log.NONE
    }
    log.set_log_level(log_dict[args.log])
    start_time = time.time()
    log.info('Current working directory = {}.'.format(os.getcwd()))
    add_dependency_management_all(args.spring_boot_dependencies_version, args.spring_cloud_dependencies_version)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))

if __name__ == '__main__':
    main()
