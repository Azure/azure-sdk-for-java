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
        module['name'] for module in ci['extends']['parameters']['Artifacts'] if module['name'] != 'azure-resourcemanager-samples'
    ]


def main():
    basedir = os.path.join(os.path.abspath(os.path.dirname(sys.argv[0])), '..')
    version_pattern = '\n## \d+\.\d+\.\d+(?:-[\w\d\.]+)? \((.*?)\)'
    date = datetime.date(datetime.now())

    for folder in read_module(basedir):
        filename = os.path.join(basedir, folder, 'CHANGELOG.md')

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
            current_changelog = left if left.strip() else '\n\n- Updated core dependency from resources\n'

        new_changelog = changelog[:first_version.start()] + first_version.group().replace(first_version.group(1), str(date)) + current_changelog
        if second_version:
            new_changelog += changelog[first_version.end() + second_version.start():]

        with open(filename, 'w') as fout:
            fout.write(new_changelog)

if __name__ == "__main__":
    logging.basicConfig(format = '%(asctime)s %(levelname)s %(message)s')
    main()