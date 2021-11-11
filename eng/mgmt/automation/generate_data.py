#!/usr/bin/env python3
import os
import sys
import shutil
import logging
import argparse

pwd = os.getcwd()
os.chdir(os.path.abspath(os.path.dirname(sys.argv[0])))
from parameters import *
os.chdir(pwd)


LLC_ARGUMENTS = '--java --low-level-client --sdk-integration --generate-samples'


def generate(
    sdk_root: str,
    input_file: str,
    group: str,
    module: str,
    credential_types: str,
    credential_scopes: str,
    autorest: str,
    use: str,
    autorest_options: str = '',
    **kwargs,
):
    namespace = 'com.{0}'.format(module.replace('-', '.'))
    output_dir = os.path.join(
        sdk_root,
        'sdk', group, module
    )
    shutil.rmtree(os.path.join(output_dir, 'src/main'), ignore_errors = True)
    shutil.rmtree(os.path.join(output_dir, 'src/samples/java', namespace.replace('.', '/'), 'generated'), ignore_errors = True)

    credential_arguments = '--java.credential-types={0}'.format(credential_types)
    if credential_scopes:
        credential_arguments += ' --java.credential-scopes={0}'.format(credential_scopes)

    input_arguments = '--input-file={0}'.format(input_file)

    command = 'autorest --version={0} --use={1} --java.azure-libraries-for-java-folder={2} --java.output-folder={3} --java.namespace={4} {5}'.format(
        autorest,
        use,
        os.path.abspath(sdk_root),
        os.path.abspath(output_dir),
        namespace,
        ' '.join((LLC_ARGUMENTS, input_arguments, credential_arguments, autorest_options)),
    )
    logging.info(command)
    if os.system(command) != 0:
        logging.error('[GENERATE] Autorest fail')
        return False

    return True


def compile_package(output_dir):
    command = 'mvn --no-transfer-progress clean verify package -f {0}'.format(os.path.join(output_dir, 'pom.xml'))
    logging.info(command)
    if os.system(command) != 0:
        logging.error('[COMPILE] Maven build fail')
        return False
    return True


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '--input-file',
        required=True,
        help='URL to OpenAPI 2.0 specification JSON as input file',
    )
    parser.add_argument(
        '--group',
        required=True,
        help='Group name under sdk/, sample: storage',
    )
    parser.add_argument(
        '--module',
        required=True,
        help='Module name under sdk/<group>/, sample: azure-storage-blob',
    )
    parser.add_argument(
        '--credential-types',
        required=True,
        help='Credential types, '
             'Sample: "tokencredential" for AAD credential for OAuth 2.0 authentication; '
             '"azurekeycredential" for Azure key credential',
    )
    parser.add_argument(
        '--credential-scopes',
        required=False,
        help='OAuth 2.0 scopes when credential-types includes tokencredential, '
             'Sample: https://storage.azure.com/.default',
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

    return parser.parse_args()


def main():
    args = vars(parse_args())

    base_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
    sdk_root = os.path.abspath(os.path.join(base_dir, SDK_ROOT))

    generate(sdk_root, **args)

    output_dir = os.path.join(
        sdk_root,
        'sdk', args['group'], args['module']
    )
    compile_package(output_dir)


if __name__ == '__main__':
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s %(levelname)s %(message)s',
        datefmt='%Y-%m-%d %X',
    )
    main()
