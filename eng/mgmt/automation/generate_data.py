#!/usr/bin/env python3
import os
import sys
import shutil
import logging
import argparse
import re
import glob
import subprocess
import yaml
from typing import List

from parameters import *
from utils import set_or_default_version
from utils import update_service_ci_and_pom
from utils import update_root_pom
from utils import ListIndentDumper


GROUP_ID = 'com.azure'
LLC_ARGUMENTS = '--java --low-level-client --sdk-integration --generate-samples'


def sdk_automation(config: dict) -> List[dict]:
    base_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
    sdk_root = os.path.abspath(os.path.join(base_dir, SDK_ROOT))
    spec_root = config['specFolder']

    packages = []

    for file_path in config['changedFiles']:
        match = re.search(
            'specification/([^/]+)/data-plane/.*/([^/]+).json',
            file_path,
            re.IGNORECASE,
        )
        if match and '/examples/' not in file_path:
            service = match.group(1)
            file_name = match.group(2)
            file_name_sans = ''.join(c for c in file_name if c.isalnum())
            module = 'azure-{0}-{1}'.format(service, file_name_sans)
            input_file = os.path.join(spec_root, file_path)
            # placeholder, for lack of information
            credential_types = 'tokencredential'
            credential_scopes = 'https://{0}.azure.com/.default'.format(service)

            succeeded = generate(sdk_root, input_file,
                                 service, module, credential_types, credential_scopes, '',
                                 AUTOREST_CORE_VERSION, AUTOREST_JAVA, '')

            generated_folder = 'sdk/{0}/{1}'.format(service, module)

            if succeeded:
                compile_package(sdk_root, GROUP_ID, module)

            artifacts = [
                '{0}/pom.xml'.format(generated_folder)
            ]
            artifacts += [
                jar for jar in glob.glob('{0}/target/*.jar'.format(
                    generated_folder))
            ]
            result = 'succeeded' if succeeded else 'failed'

            packages.append({
                'packageName': module,
                'path': [generated_folder],
                'artifacts': artifacts,
                'result': result,
            })
        else:
            logging.info('[Skip] changed file {0}'.format(file_path))

    return packages


def generate(
    sdk_root: str,
    input_file: str,
    service: str,
    module: str,
    credential_types: str,
    credential_scopes: str,
    title: str,
    autorest: str,
    use: str,
    autorest_options: str = '',
    **kwargs,
) -> bool:
    namespace = 'com.{0}'.format(module.replace('-', '.'))
    output_dir = os.path.join(
        sdk_root,
        'sdk', service, module
    )
    shutil.rmtree(os.path.join(output_dir, 'src/main'), ignore_errors=True)
    shutil.rmtree(os.path.join(output_dir, 'src/samples/java', namespace.replace('.', '/'), 'generated'),
                  ignore_errors=True)

    readme_relative_path = update_readme(output_dir, input_file, credential_types, credential_scopes, title)
    if readme_relative_path:
        logging.info('[GENERATE] Autorest from README {}'.format(readme_relative_path))

        command = 'autorest --version={0} --use={1} {2}'.format(
            autorest,
            use,
            readme_relative_path
        )
        logging.info(command)
        try:
            subprocess.run(command, shell=True, cwd=output_dir, check=True)
        except subprocess.CalledProcessError:
            logging.error('[GENERATE] Autorest fail')
            return False
    else:
        logging.info('[GENERATE] Autorest from JSON {}'.format(input_file))

        credential_arguments = '--java.credential-types={0}'.format(credential_types)
        if credential_scopes:
            credential_arguments += ' --java.credential-scopes={0}'.format(credential_scopes)

        input_arguments = '--input-file={0}'.format(input_file)

        artifact_arguments = '--artifact-id={0}'.format(module)
        if title:
            artifact_arguments += ' --title={0}'.format(title)

        command = 'autorest --version={0} --use={1} ' \
                  '--java.azure-libraries-for-java-folder={2} --java.output-folder={3} ' \
                  '--java.namespace={4} {5}'\
            .format(
                autorest,
                use,
                os.path.abspath(sdk_root),
                os.path.abspath(output_dir),
                namespace,
                ' '.join((LLC_ARGUMENTS, input_arguments, credential_arguments, artifact_arguments, autorest_options))
            )
        logging.info(command)
        if os.system(command) != 0:
            logging.error('[GENERATE] Autorest fail')
            return False

        set_or_default_version(sdk_root, GROUP_ID, module)
        update_service_ci_and_pom(sdk_root, service, GROUP_ID, module)
        update_root_pom(sdk_root, service)
        # skip version update script, as current automation does not support automatic version increment
        # update_version(sdk_root, output_dir)

    return True


def compile_package(sdk_root: str, group_id: str, module: str) -> bool:
    command = 'mvn --no-transfer-progress clean verify package -f {0}/pom.xml -Dgpg.skip -Drevapi.skip -pl {1}:{2} -am'.format(
        sdk_root, group_id, module)
    logging.info(command)
    if os.system(command) != 0:
        logging.error('[COMPILE] Maven build fail')
        return False
    return True


def update_readme(output_dir: str, input_file: str, credential_types: str, credential_scopes: str, title: str) -> str:
    readme_relative_path = ''

    swagger_dir = os.path.join(output_dir, 'swagger')
    if os.path.isdir(swagger_dir):
        for filename in os.listdir(swagger_dir):
            if filename.lower().startswith('readme') and filename.lower().endswith('.md'):
                readme_yaml_found = False
                readme_path = os.path.join(swagger_dir, filename)
                with open(readme_path, 'r', encoding='utf-8') as f_in:
                    content = f_in.read()
                if content:
                    yaml_blocks = re.findall(r'```\s?(?:yaml|YAML)\n(.*?)```', content, re.DOTALL)
                    for yaml_str in yaml_blocks:
                        yaml_json = yaml.safe_load(yaml_str)
                        if 'low-level-client' in yaml_json and yaml_json['low-level-client']:
                            # yaml block found, update
                            yaml_json['input-file'] = [input_file]
                            if title:
                                yaml_json['title'] = title
                            if credential_types:
                                yaml_json['credential-types'] = credential_types
                            if credential_scopes:
                                yaml_json['credential-scopes'] = credential_scopes

                            # write updated yaml
                            updated_yaml_str = yaml.dump(yaml_json,
                                                         sort_keys=False,
                                                         Dumper=ListIndentDumper)

                            if not yaml_str == updated_yaml_str:
                                # update readme
                                updated_content = content.replace(yaml_str, updated_yaml_str, 1)
                                with open(readme_path, 'w', encoding='utf-8') as f_out:
                                    f_out.write(updated_content)

                                logging.info('[GENERATE] YAML block in README updated from\n{0}\nto\n{1}'.format(
                                    yaml_str, updated_yaml_str
                                ))

                            readme_yaml_found = True
                            break

                if readme_yaml_found:
                    readme_relative_path = 'swagger/{}'.format(filename)
                    break

    return readme_relative_path


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '--input-file',
        required=True,
        help='URL to OpenAPI 2.0 specification JSON as input file.',
    )
    parser.add_argument(
        '--service',
        required=True,
        help='Service name under sdk/. Sample: storage',
    )
    parser.add_argument(
        '--module',
        required=True,
        help='Module name under sdk/<service>/. Sample: azure-storage-blob',
    )
    parser.add_argument(
        '--credential-types',
        required=True,
        help='Credential types. '
             'Sample: "tokencredential" for AAD credential for OAuth 2.0 authentication; '
             '"azurekeycredential" for Azure key credential',
    )
    parser.add_argument(
        '--credential-scopes',
        required=False,
        help='OAuth 2.0 scopes when credential-types includes "tokencredential". '
             'Sample: https://storage.azure.com/.default',
    )
    parser.add_argument(
        '--title',
        required=False,
        help='The name of the client. The name should always ends with "Client". '
             'Sample: BlobClient, which makes BlobClientBuilder as builder class',
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

    succeeded = generate(sdk_root, **args)
    if succeeded:
        succeeded = compile_package(sdk_root, GROUP_ID, args['module'])

    if not succeeded:
        raise RuntimeError('Failed to generate code or compile the package')


if __name__ == '__main__':
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s %(levelname)s %(message)s',
        datefmt='%Y-%m-%d %X',
    )
    main()
