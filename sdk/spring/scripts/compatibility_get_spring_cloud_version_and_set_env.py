import time
import os
import json
from pipes import quote

from log import log


def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def get_spring_cloud_version_and_set_as_env(filepath):
    spring_boot_version = os.getenv("SPRING_CLOUD_AZURE_TEST_SUPPORTED_SPRING_BOOT_VERSION")
    with open(filepath, 'r') as file:
        data = json.load(file)
    for entry in data:
        for key in entry:
            if spring_boot_version == entry[key]:
                spring_cloud_version = entry["spring-cloud-version"]
                # os.environ['SPRING_CLOUD_AZURE_TEST_SUPPORTED_SPRING_CLOUD_VERSION'] = spring_cloud_version
                print("export SPRING_CLOUD_AZURE_TEST_SUPPORTED_SPRING_CLOUD_VERSION={}".format(quote(spring_cloud_version)))
                # print("export env.SPRING_CLOUD_AZURE_TEST_SUPPORTED_SPRING_CLOUD_VERSION={}".format(spring_cloud_version))
                # print("Spring-cloud version:" + spring_cloud_version)


def main():
    # start_time = time.time()
    change_to_root_dir()
    # log.debug('Current working directory = {}.'.format(os.getcwd()))
    get_spring_cloud_version_and_set_as_env("./sdk/spring/spring-cloud-azure-supported-spring.json")
    # elapsed_time = time.time() - start_time
    # log.info('elapsed_time = {}'.format(elapsed_time))


if __name__ == '__main__':
    main()
