# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.


import argparse
import os
import time

import in_place

from log import log, Log
from artifact_id_pair import ArtifactIdPair
from version_update_item import VersionUpdateItem

X_VERSION_UPDATE = 'x-version-update'
X_INCLUDE_UPDATE = 'x-include-update'

config = {
    'cosmos': {
        'sdk/cosmos/azure-spring-data-cosmos-test/pom.xml': {
            'artifact_id_pairs': (
                ArtifactIdPair('azure-spring-data-2-3-cosmos', 'azure-spring-data-2-2-cosmos'),
            )
        }
    },
    'spring': {
        'sdk/spring/azure-spring-boot-test-cosmosdb/pom.xml': {
            'artifact_id_pairs': (
                ArtifactIdPair('azure-cosmosdb-spring-boot-2-3-starter', 'azure-cosmosdb-spring-boot-2-2-starter'),
            ),
            'version_update_items': (
                VersionUpdateItem('org.springframework.boot:spring-boot-starter-web', '2.2.9.RELEASE'),
                VersionUpdateItem('org.springframework.boot:spring-boot-starter-actuator', '2.2.9.RELEASE'),
                VersionUpdateItem('org.springframework.boot:spring-boot-starter-test', '2.2.9.RELEASE')
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
    :param module: module name
    :return:
    """
    for pom in config[module].keys():
        replace_artifact_id(module, pom)
        replace_version(module, pom)


def replace_artifact_id(module, pom):
    """
    Replace artifactId in dependency
    :param module: module name
    :param pom: pom file path
    :return:
    """
    log.debug('Replacing artifact id in file: {}'.format(pom, module))
    artifact_id_pairs = config[module][pom]['artifact_id_pairs']
    log.debug('Module: {}, artifact ids: {}'.format(module, artifact_id_pairs))
    with in_place.InPlace(pom) as file:
        line_num = 0
        for line in file:
            line_num = line_num + 1
            for artifact_id_pair in artifact_id_pairs:
                new_line = line.replace(artifact_id_pair.old_artifact_id, artifact_id_pair.new_artifact_id)
                if line != new_line:
                    line = new_line
                    log.debug('Updated artifact id in line {}'.format(line_num))
                    log.debug('    old_line = {}.'.format(line.strip('\n')))
                    log.debug('    new_line = {}.'.format(new_line.strip('\n')))
            file.write(line)


def replace_version(module, pom):
    """
    Replace version, contains dependency and plugin parts.
    :param module: module name
    :param pom: pom file path
    :return:
    """
    log.debug('Replacing version in file: {}'.format(pom))
    version_update_items = config[module][pom]['version_update_items']
    log.debug('Module: {}, versions: {}'.format(module, version_update_items))
    with in_place.InPlace(pom) as file:
        line_num = 0
        for line in file:
            line_num = line_num + 1
            for version_update_item in version_update_items:
                if version_update_item.id in line:
                    log.debug('Updated line {}'.format(line_num))
                    # update version in dependency part
                    if X_VERSION_UPDATE in line:
                        old_version = line[(line.index('<version>') + 9):line.index('</version>')]
                        new_line = line.replace(old_version, version_update_item.new_version)
                        if line != new_line:
                            line = new_line
                            log.debug('Updated version of dependency in line {}'.format(line_num))
                            log.debug('    old_line = {}.'.format(line.strip('\n')))
                            log.debug('    new_line = {}.'.format(new_line.strip('\n')))
                    # update version in plugin part
                    elif X_INCLUDE_UPDATE in line:
                        old_version = line[(line.index('[')+1):line.index(']')]
                        new_line = line.replace(old_version, version_update_item.new_version)
                        if line != new_line:
                            line = new_line
                            log.debug('Updated version of plugin in line {}'.format(line_num))
                            log.debug('    old_line = {}.'.format(line.strip('\n')))
                            log.debug('    new_line = {}.'.format(new_line.strip('\n')))
            file.write(line)


if __name__ == '__main__':
    main()
