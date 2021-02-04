#!/usr/bin/env python3
import os
import sys
import logging
import argparse

pwd = os.getcwd()
os.chdir(os.path.abspath(os.path.dirname(sys.argv[0])))
from parameters import *
import generate
os.chdir(pwd)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument('-s', '--service', required = True)
    parser.add_argument('--suffix')
    parser.add_argument('-c', '--compile', action = 'store_true')
    return parser.parse_args()


def main():
    args = vars(parse_args())
    sdk_root = os.path.abspath(
        os.path.join(os.path.dirname(sys.argv[0]), SDK_ROOT))
    service = args['service']
    generate.update_parameters(args.get('suffix'))

    if args.get('compile'):
        generate.compile_package(sdk_root, service)

    versions = generate.get_version(sdk_root, service).split(';')
    stable_version = versions[1]
    current_version = versions[2]
    generate.compare_with_maven_package(sdk_root, service, stable_version,
                                        current_version)


if __name__ == "__main__":
    logging.basicConfig(
        level = logging.INFO,
        format = '%(asctime)s %(levelname)s %(message)s',
        datefmt = '%Y-%m-%d %X',
    )
    main()