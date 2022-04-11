#!/usr/bin/env python3
import os
import re
import sys
import json
import glob
import logging
import argparse

pwd = os.getcwd()
os.chdir(os.path.abspath(os.path.dirname(sys.argv[0])))
from parameters import *
from utils import set_or_increase_version
from generate_data import sdk_automation as sdk_automation_data
from generate_utils import (
    compare_with_maven_package,
    compile_package,
    generate,
    get_and_update_service_from_api_specs,
    get_suffix_from_api_specs,
)

os.chdir(pwd)


def update_parameters(suffix):
    # update changeable parameters in parameters.py
    global SUFFIX, NAMESPACE_SUFFIX, ARTIFACT_SUFFIX, NAMESPACE_FORMAT, ARTIFACT_FORMAT, OUTPUT_FOLDER_FORMAT

    SUFFIX = suffix

    NAMESPACE_SUFFIX = '.{0}'.format(SUFFIX) if SUFFIX else ''
    ARTIFACT_SUFFIX = '-{0}'.format(SUFFIX) if SUFFIX else ''
    NAMESPACE_FORMAT = 'com.azure.resourcemanager.{{0}}{0}'.format(
        NAMESPACE_SUFFIX)
    ARTIFACT_FORMAT = 'azure-resourcemanager-{{0}}{0}'.format(ARTIFACT_SUFFIX)
    OUTPUT_FOLDER_FORMAT = 'sdk/{{0}}/{0}'.format(ARTIFACT_FORMAT)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '--spec-root',
        default='https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/',
        help='Spec root folder',
    )
    parser.add_argument(
        '-r',
        '--readme',
        help='Readme path, Sample: "storage" or "specification/storage/resource-manager/readme.md"',
    )
    parser.add_argument('-t', '--tag', help='Specific tag')
    parser.add_argument('-v', '--version', help='Specific sdk version')
    parser.add_argument(
        '-s',
        '--service',
        help='Service Name if not the same as spec name',
    )
    parser.add_argument(
        '-u',
        '--use',
        default=AUTOREST_JAVA,
        help='Autorest java plugin',
    )
    parser.add_argument(
        '--autorest',
        default=AUTOREST_CORE_VERSION,
        help='Autorest version',
    )
    parser.add_argument(
        '--autorest-options',
        default='',
        help='Additional autorest options',
    )
    parser.add_argument('--suffix', help='Suffix for namespace and artifact')
    parser.add_argument(
        '--auto-commit-external-change',
        action='store_true',
        help='Automatic commit the generated code',
    )
    parser.add_argument('--user-name', help='User Name for commit')
    parser.add_argument('--user-email', help='User Email for commit')
    parser.add_argument(
        'config',
        nargs='*',
    )

    return parser.parse_args()


def sdk_automation(input_file: str, output_file: str):
    base_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
    sdk_root = os.path.abspath(os.path.join(base_dir, SDK_ROOT))
    api_specs_file = os.path.join(base_dir, API_SPECS_FILE)
    with open(input_file, 'r') as fin:
        config = json.load(fin)

    packages = []
    for readme in config['relatedReadmeMdFiles']:
        match = re.search(
            'specification/([^/]+)/resource-manager/readme.md',
            readme,
            re.IGNORECASE,
        )
        if not match:
            logging.info(
                '[Skip] readme path does not format as specification/*/resource-manager/readme.md'
            )
        else:
            spec = match.group(1)
            service = get_and_update_service_from_api_specs(
                api_specs_file, spec)

            pre_suffix = SUFFIX
            suffix = get_suffix_from_api_specs(api_specs_file, spec)
            if suffix == None:
                suffix = SUFFIX
            update_parameters(suffix)

            # TODO: use specific function to detect tag in "resources"
            tag = None
            if service == 'resources':
                with open(os.path.join(config['specFolder'], readme)) as fin:
                    tag_match = re.search('tag: (package-resources-[\S]+)',
                                          fin.read())
                    if tag_match:
                        tag = tag_match.group(1)
                    else:
                        tag = 'package-resources-2020-10'

            module = ARTIFACT_FORMAT.format(service)
            output_folder = OUTPUT_FOLDER_FORMAT.format(service)
            namespace = NAMESPACE_FORMAT.format(service)
            stable_version, current_version = set_or_increase_version(
                sdk_root,
                GROUP_ID,
                module
            )
            succeeded = generate(
                sdk_root,
                service,
                spec_root=config['specFolder'],
                readme=readme,
                autorest=AUTOREST_CORE_VERSION,
                use=AUTOREST_JAVA,
                output_folder=output_folder,
                module=module,
                namespace=namespace,
                tag=tag,
            )
            if succeeded:
                compile_package(sdk_root, module)

            packages.append({
                'packageName':
                    '{0}'.format(ARTIFACT_FORMAT.format(service)),
                'path': [
                    output_folder,
                    CI_FILE_FORMAT.format(service),
                    POM_FILE_FORMAT.format(service),
                    'eng/versioning',
                    'pom.xml',
                ],
                'readmeMd': [readme],
                'artifacts': ['{0}/pom.xml'.format(output_folder)] +
                             [jar for jar in glob.glob('{0}/target/*.jar'.format(output_folder))],
                'apiViewArtifact': next(iter(glob.glob('{0}/target/*-sources.jar'.format(output_folder))), None),
                'language': 'Java',
                'result': 'succeeded' if succeeded else 'failed',
            })

            update_parameters(pre_suffix)

    if not packages:
        # try data-plane codegen
        packages = sdk_automation_data(config)

    with open(output_file, 'w') as fout:
        output = {
            'packages': packages,
        }
        json.dump(output, fout)


def main():
    args = vars(parse_args())

    if args.get('config'):
        return sdk_automation(args['config'][0], args['config'][1])

    base_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
    sdk_root = os.path.abspath(os.path.join(base_dir, SDK_ROOT))
    api_specs_file = os.path.join(base_dir, API_SPECS_FILE)

    readme = args['readme']
    match = re.match(
        'specification/([^/]+)/resource-manager/readme.md',
        readme,
        re.IGNORECASE,
    )
    if not match:
        spec = readme
        readme = 'specification/{0}/resource-manager/readme.md'.format(spec)
    else:
        spec = match.group(1)

    args['readme'] = readme
    args['spec'] = spec

    # update_parameters(
    #     args.get('suffix') or get_suffix_from_api_specs(api_specs_file, spec))
    update_parameters(args.get('suffix'))
    service = get_and_update_service_from_api_specs(api_specs_file, spec,
                                                    args['service'])
    args['service'] = service
    module = ARTIFACT_FORMAT.format(service)
    stable_version, current_version = set_or_increase_version(sdk_root, GROUP_ID, module, **args)
    args['version'] = current_version
    output_folder = OUTPUT_FOLDER_FORMAT.format(service),
    namespace = NAMESPACE_FORMAT.format(service)
    succeeded = generate(
        sdk_root,
        module=module,
        output_folder=output_folder,
        namespace=namespace,
        **args
    )

    if succeeded:
        succeeded = compile_package(sdk_root, module)
        if succeeded:
            compare_with_maven_package(sdk_root, service, stable_version,
                                       current_version, module)

            if args.get('auto_commit_external_change') and args.get('user_name') and args.get('user_email'):
                pwd = os.getcwd()
                try:
                    os.chdir(sdk_root)
                    os.system('git add eng/versioning eng/mgmt pom.xml {0} {1}'.format(
                        CI_FILE_FORMAT.format(service),
                        POM_FILE_FORMAT.format(service)))
                    os.system(
                        'git -c user.name={0} -c user.email={1} commit -m "[Automation] External Change"'
                            .format(args['user_name'], args['user_email']))
                finally:
                    os.chdir(pwd)

    if not succeeded:
        raise RuntimeError('Failed to generate code or compile the package')


if __name__ == '__main__':
    logging.basicConfig(
        stream=sys.stdout,
        level=logging.INFO,
        format='%(asctime)s %(levelname)s %(message)s',
        datefmt='%Y-%m-%d %X',
    )
    main()
