# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.


import argparse
import os
import time

import in_place

from log import log, Log

config = {
    'cosmos': {
        'sdk/cosmos/azure-spring-data-cosmos-test/pom.xml': {
            'artifact_id_tuple': (
                ('azure-spring-data-2-3-cosmos', 'azure-spring-data-2-2-cosmos'),
            )
        }
    },
    'spring': {
        'sdk/spring/azure-spring-boot-test-cosmosdb/pom.xml': {
            'artifact_id_tuple': (
                ('azure-cosmosdb-spring-boot-2-3-starter', 'azure-cosmosdb-spring-boot-2-2-starter'),
            ),
            'version_tuple': (
                ('org.springframework.boot:spring-boot-starter-web', '2.2.9.RELEASE'),
                ('org.springframework.boot:spring-boot-starter-actuator', '2.2.9.RELEASE'),
                ('org.springframework.boot:spring-boot-starter-test', '2.2.9.RELEASE')
            )
        }
    }
}


def main():
    start_time = time.time()
    change_to_root_dir()
    log.debug('Current working directory = {}.'.format(os.getcwd()))
    args = get_args()
    init_log(args)
    replace(args.module)
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


def replace(module):
    """
    Replace action
    :param module:
    :return:
    """
    module = 'spring'
    for pom in config[module].keys():
        replace_artifact_id(module, pom)
        replace_version(module, pom)


def replace_artifact_id(module, pom):
    """
    Replace artifactId in dependency
    :param module module name
    :param pom: pom file path
    :return:
    """

    log.info('Replacing artifact id in file: {}'.format(pom, module))
    artifact_id_tuple = config[module][pom]['artifact_id_tuple']
    log.info('Module: {}, artifact ids: {}'.format(module, artifact_id_tuple))
    with in_place.InPlace(pom) as file:
        line_num = 0
        for line in file:
            line_num = line_num + 1
            for artifact_id_mapping_tuple in artifact_id_tuple:
                new_line = line.replace(artifact_id_mapping_tuple[0], artifact_id_mapping_tuple[1])
                if line != new_line:
                    log.debug('Updated artifact id line {}'.format(line_num))
                    log.debug('    old_line = {}.'.format(line.strip('\n')))
                    log.debug('    new_line = {}.'.format(new_line.strip('\n')))
                    line = new_line
            file.write(line)


def replace_version(module, pom):
    """
    Replace version, contains dependency and plugin parts.
    :param module module name
    :param pom: pom file path
    :return:
    """

    log.info('Replacing version in file: {}'.format(pom))
    version_tuple = config[module][pom]['version_tuple']
    log.info('Module: {}, versions: {}'.format(module, version_tuple))
    with in_place.InPlace(pom) as file:
        line_num = 0
        for line in file:
            line_num = line_num + 1
            for version_mapping_tuple in version_tuple:
                dependency_update_flag = '<!-- {x-version-update;' + version_mapping_tuple[0] + ';'
                plugin_update_flag = '<!-- {x-include-update;' + version_mapping_tuple[0] + ';'
                # update version in dependency part
                if dependency_update_flag in line:
                    old_version = line[(line.index('<version>') + 9):line.index('</version>')]
                    log.debug('Updated version line {}'.format(line_num))
                    new_line = line.replace(old_version, version_mapping_tuple[1])
                    if line != new_line:
                        log.debug('Updated dependency line {}'.format(line_num))
                        log.debug('    old_line = {}.'.format(line.strip('\n')))
                        log.debug('    new_line = {}.'.format(new_line.strip('\n')))
                        line = new_line
                # update version in plugin part
                elif plugin_update_flag in line:
                    old_version = line[(line.index('[')+1):line.index(']')]
                    log.debug('Updated version line {}'.format(line_num))
                    new_line = line.replace(old_version, version_mapping_tuple[1])
                    if line != new_line:
                        log.debug('Updated plugin line {}'.format(line_num))
                        log.debug('    old_line = {}.'.format(line.strip('\n')))
                        log.debug('    new_line = {}.'.format(new_line.strip('\n')))
                        line = new_line
            file.write(line)


if __name__ == '__main__':
    main()
