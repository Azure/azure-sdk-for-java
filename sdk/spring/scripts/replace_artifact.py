# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.


import argparse
import os
import time

import in_place

from log import log, Log

config = {
    'cosmos': {
        'artifact_dict': {
            'azure-spring-data-2-3-cosmos': 'azure-spring-data-2-2-cosmos'
        },
        'pom_list': [
            'sdk/cosmos/azure-spring-data-cosmos-test/pom.xml'
        ]
    }
}


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
        '--module',
        type = str,
        choices = ['spring', 'cosmos'],
        required = False,
        default = 'cosmos',
        help = 'Specify the target module.'
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
        '--color',
        type = str,
        choices = ['true', 'false'],
        required = False,
        default = 'true',
        help = 'Whether need colorful log.'
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
    color_dict = {
        'true': True,
        'false': False
    }
    log.set_color(color_dict[args.color])


def replace_artifact(module):
    artifact_dict = config[module]['artifact_dict']
    pom_list = config[module]['pom_list']
    for pom in pom_list:
        log.info('Processing file: {}'.format(pom))
        with in_place.InPlace(pom) as file:
            line_num = 0
            for line in file:
                line_num = line_num + 1
                for key, value in artifact_dict.items():
                    new_line = line.replace(key, value)
                    if line != new_line:
                        log.debug('Updated line {}'.format(line_num))
                        log.debug('    old_line = {}.'.format(line.strip('\n')))
                        log.debug('    new_line = {}.'.format(new_line.strip('\n')))
                        line = new_line
                file.write(line)


if __name__ == '__main__':
    main()
