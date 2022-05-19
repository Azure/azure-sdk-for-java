
import time
from log import log
import os
import json


def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def set_env_of_springcloud_version(filepath):
    with open(filepath, 'r') as file:
        data = json.load(file)
        # print(data)
    for key in data:
        os.environ[key+"springCloud"] = data[key]
        print("set env.${"+key+"springCloud}="+os.environ[key+"springCloud"])
    #     print(json.dumps(file))


def main():
    start_time = time.time()
    change_to_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    set_env_of_springcloud_version("./sdk/spring/compatibility-version-management.json")
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


if __name__ == '__main__':
    main()