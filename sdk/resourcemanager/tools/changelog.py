#!/usr/bin/env python3
import os
import re
import sys
import yaml
import fnmatch
import logging
from datetime import datetime


def read_module(root = os.curdir):
    with open(os.path.join(root, 'ci.yml')) as fin:
        ci = yaml.safe_load(fin)

    return [
        module for module in ci['extends']['parameters']['Artifacts'] if module['name'] != 'azure-resourcemanager-samples'
    ]

def get_version(module: dict, version_file: str):
    module_name = '{}:{}'.format(module['groupId'], module['name'])
    with open(version_file) as fin:
        for line in fin.readlines():
            line = line.strip()
            if line and not line.startswith('#'):
                name, _, version = line.split(';')
                if name == module_name:
                    return version

    raise KeyError('Cannot found version of {} in {}'.format(module_name, version_file))

def main():
    basedir = os.path.join(os.path.abspath(os.path.dirname(sys.argv[0])), '..')
    os.chdir(basedir)

    version_file = '../../eng/versioning/version_client.txt'
    version_pattern = '\n## (\d+\.\d+\.\d+(?:-[\w\d\.]+)?) \((.*?)\)'
    date = datetime.date(datetime.now())

    for module in read_module():
        filename = os.path.join(module['name'], 'CHANGELOG.md')

        with open(filename) as fin:
            changelog = fin.read()

        first_version = re.search(version_pattern, changelog)
        if not first_version:
            logging.error('Cannot read version from {}'.format(filename))
            continue

        left = changelog[first_version.end():]
        second_version = re.search(version_pattern, left)
        if not second_version:
            current_changelog = left if left.strip() else '\n\n- Migrated from previous sdk\n'
        else:
            left = left[:second_version.start()]
            current_changelog = left if left.strip() else '\n\n### Dependency Updates\n\n- Updated core dependency from resources\n'

        version: str = first_version.group().replace(
            first_version.group(2), str(date)).replace(
                first_version.group(1), get_version(module, version_file))

        new_changelog = changelog[:first_version.start()] + version + current_changelog
        if second_version:
            new_changelog += changelog[first_version.end() + second_version.start():]

        with open(filename, 'w') as fout:
            fout.write(new_changelog)

if __name__ == "__main__":
    logging.basicConfig(format = '%(asctime)s %(levelname)s %(message)s')
    main()