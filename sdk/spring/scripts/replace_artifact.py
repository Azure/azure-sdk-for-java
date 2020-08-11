# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.


import argparse
import os
import time

import in_place

import file_loader
from log import log, Log


def main():
    start_time = time.time()
    change_to_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    args = get_args()
    init_log(args)
    replace_artifact(args.module)
    elapsed_time = time.time() - start_time
    log.info('elapsed_time = {}'.format(elapsed_time))


def change_to_root_dir():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    os.chdir('../../..')


def get_args():
    parser = argparse.ArgumentParser(
        description = 'Replace artifact id in pom file.'
    )
    parser.add_argument(
        '--log',
        type = str,
        choices = ['debug', 'info', 'warn', 'error', 'none'],
        required = False,
        default = 'info',
        help = 'Set log level.'
    )
    parser.add_argument(
        '--module',
        type = str,
        choices = ['spring', 'cosmos'],
        required = False,
        default = 'cosmos',
        help = 'Specify the target module.'
    )
    return parser.parse_args()


def init_log(args):
    log_dict = {
        'debug': Log.DEBUG,
        'info': Log.INFO,
        'warn': Log.WARN,
        'error': Log.ERROR,
        'none': Log.NONE
    }
    log.set_log_level(log_dict[args.log])


def replace_artifact(module):
    artifact_map_file = 'sdk/spring/scripts/replace_artifact_config_{}/artifact_dict.txt'.format(module)
    pom_list_file = 'sdk/spring/scripts/replace_artifact_config_{}/pom_list.txt'.format(module)
    artifact_dict = file_loader.load_dict(artifact_map_file)
    pom_list = file_loader.load_list(pom_list_file)
    for pom in pom_list:
        log.info('Processing file: {}'.format(pom))
        with in_place.InPlace(pom) as file:
            for line in file:
                for key, value in artifact_dict.items():
                    line = line.replace(key, value)
                file.write(line)


if __name__ == '__main__':
    main()
