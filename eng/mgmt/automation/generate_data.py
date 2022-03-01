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
from typing import Dict, List, Tuple

from parameters import *
from utils import set_or_default_version
from utils import update_service_ci_and_pom
from utils import update_root_pom
from utils import ListIndentDumper


GROUP_ID = 'com.azure'
LLC_ARGUMENTS = '--low-level-client --sdk-integration --generate-samples'


def sdk_automation(config: dict) -> List[dict]:
    base_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
    sdk_root = os.path.abspath(os.path.join(base_dir, SDK_ROOT))
    spec_root = os.path.abspath(config['specFolder'])

    packages = []

    readme_file_path = None
    for file_path in config['relatedReadmeMdFiles']:
        match = re.search(
            'specification/([^/]+)/data-plane/readme.md',
            file_path,
            re.IGNORECASE,
        )
        if match:
            readme_file_path = file_path
            break

    for file_path in config['changedFiles']:
        match = re.search(
            'specification/([^/]+)/data-plane/.*/([^/]+).json',
            file_path,
            re.IGNORECASE,
        )
        if match and '/examples/' not in file_path:
            service = match.group(1)
            file_name = match.group(2)

            input_file, service, module, module_readme = get_generation_parameters(
                service, file_name, spec_root, file_path, readme_file_path)
            if module_readme and not os.path.isabs(module_readme):
                module_readme = os.path.join(os.path.dirname(os.path.join(spec_root, readme_file_path)), module_readme)

            succeeded = generate(sdk_root, input_file, module_readme,
                                 service, module, '', '', '',
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
    readme_file: str,
    service: str,
    module: str,
    security: str,
    security_scopes: str,
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
    shutil.rmtree(os.path.join(output_dir, 'src/tests/java', namespace.replace('.', '/'), 'generated'),
                  ignore_errors=True)

    if readme_file:
        readme_file_path = update_spec_readme(readme_file, input_file, security, security_scopes, title)
    else:
        readme_file_path = update_readme(output_dir, input_file, security, security_scopes, title)
    if readme_file_path:
        logging.info('[GENERATE] Autorest from README {}'.format(readme_file_path))

        command = 'autorest --version={0} --use={1} --java --java.output-folder={2} {3}'.format(
            autorest,
            use,
            output_dir,
            readme_file_path
        )
        logging.info(command)
        try:
            subprocess.run(command, shell=True, cwd=output_dir, check=True)
        except subprocess.CalledProcessError:
            logging.error('[GENERATE] Autorest fail')
            return False
    else:
        logging.info('[GENERATE] Autorest from JSON {}'.format(input_file))

        security_arguments = ''
        if security:
            security_arguments += '--security={0}'.format(security)
        if security_scopes:
            security_arguments += ' --security-scopes={0}'.format(security_scopes)

        input_arguments = '--input-file={0}'.format(input_file)

        artifact_arguments = '--artifact-id={0}'.format(module)
        if title:
            artifact_arguments += ' --title={0}'.format(title)

        command = 'autorest --version={0} --use={1} --java ' \
                  '--java.azure-libraries-for-java-folder={2} --java.output-folder={3} ' \
                  '--java.namespace={4} {5}'\
            .format(
                autorest,
                use,
                os.path.abspath(sdk_root),
                os.path.abspath(output_dir),
                namespace,
                ' '.join((LLC_ARGUMENTS, input_arguments, security_arguments, artifact_arguments, autorest_options))
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
    command = 'mvn --no-transfer-progress clean verify package -f {0}/pom.xml -Dmaven.javadoc.skip -Dgpg.skip -Drevapi.skip -pl {1}:{2} -am'.format(
        sdk_root, group_id, module)
    logging.info(command)
    if os.system(command) != 0:
        logging.error('[COMPILE] Maven build fail')
        return False
    return True


def get_generation_parameters(
    service, file_name, spec_root, json_file_path, readme_file_path: str
) -> Tuple[str, str, str, str]:
    # get parameters from README.java.md from spec repo, or fallback to parameters deduced from json file path

    input_file = os.path.join(spec_root, json_file_path)
    module = None
    module_readme = None
    if readme_file_path:
        # try readme, it must contain 'batch' and match the json file by name
        java_readme_file_path = os.path.join(spec_root, readme_file_path.replace('.md', '.java.md'))
        if os.path.exists(java_readme_file_path):
            with open(java_readme_file_path, 'r', encoding='utf-8') as f_in:
                content = f_in.read()
            if content:
                yaml_blocks = re.findall(r'```\s?(?:yaml|YAML).*?\n(.*?)```', content, re.DOTALL)
                for yaml_str in yaml_blocks:
                    yaml_json = yaml.safe_load(yaml_str)
                    if 'batch' in yaml_json:
                        for item in yaml_json['batch']:
                            input_files = item['input-file']
                            for file in input_files:
                                if os.path.basename(file) == os.path.basename(input_file):
                                    # found in README
                                    service = item['service']
                                    module = item['module']
                                    logging.info('[GENERATE] service {0} and module {1} found for {2}'.format(
                                        service, module, json_file_path))
                                    if 'readme' in item:
                                        module_readme = item['readme']
                                    break

    if not module:
        # deduce from json file path
        file_name_sans = ''.join(c for c in file_name if c.isalnum())
        module = 'azure-{0}-{1}'.format(service, file_name_sans).lower()
    return input_file, service, module, module_readme


def update_spec_readme(
    readme_path: str, input_file: str, security: str, security_scopes: str, title: str
) -> str:
    # update README.java.##.md in spec repo

    with open(readme_path, 'r', encoding='utf-8') as f_in:
        content = f_in.read()
    if content:
        yaml_blocks = re.findall(r'```\s?(?:yaml|YAML).*?\n(.*?)```', content, re.DOTALL)
        for yaml_str in yaml_blocks:
            yaml_json = yaml.safe_load(yaml_str)
            if 'low-level-client' in yaml_json and yaml_json['low-level-client']:
                match_found, input_files = update_yaml_input_files(yaml_json, input_file)
                if match_found:
                    # yaml block found, update
                    yaml_json['input-file'] = input_files
                    if title:
                        yaml_json['title'] = title
                    if security:
                        yaml_json['security'] = security
                    if security_scopes:
                        yaml_json['security-scopes'] = security_scopes

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
                    break

    return readme_path


def update_readme(
    module_dir: str, input_file: str, security: str, security_scopes: str, title: str
) -> str:
    # update README_SPEC.md in SDK repo

    readme_relative_path = ''

    swagger_dir = os.path.join(module_dir, 'swagger')
    if os.path.isdir(swagger_dir):
        for filename in os.listdir(swagger_dir):
            if filename.lower().startswith('readme') and filename.lower().endswith('.md'):
                readme_yaml_found = False
                readme_path = os.path.join(swagger_dir, filename)
                with open(readme_path, 'r', encoding='utf-8') as f_in:
                    content = f_in.read()
                if content:
                    yaml_blocks = re.findall(r'```\s?(?:yaml|YAML).*?\n(.*?)```', content, re.DOTALL)
                    for yaml_str in yaml_blocks:
                        yaml_json = yaml.safe_load(yaml_str)
                        if 'low-level-client' in yaml_json and yaml_json['low-level-client']:
                            match_found, input_files = update_yaml_input_files(yaml_json, input_file)
                            if match_found:
                                # yaml block found, update
                                yaml_json['input-file'] = input_files
                                if title:
                                    yaml_json['title'] = title
                                if security:
                                    yaml_json['security'] = security
                                if security_scopes:
                                    yaml_json['security-scopes'] = security_scopes

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


def update_yaml_input_files(yaml_json: Dict[str, dict], input_json_file: str) -> Tuple[bool, List[str]]:
    # update input-file with the json file

    if 'input-file' in yaml_json:
        input_files = yaml_json['input-file']
        if not isinstance(input_files, List):
            # str to List
            input_files = [input_files]
        updated_input_files = []
        match_found = False
        for file in input_files:
            if os.path.basename(file) == os.path.basename(input_json_file):
                match_found = True
                updated_input_files.append(input_json_file)
            else:
                updated_input_files.append(file)
        return match_found, updated_input_files
    else:
        return False, []


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
        '--security',
        required=False,
        help='Security schemes for authentication. '
             'Sample: "AADToken" for AAD credential for OAuth 2.0 authentication; '
             '"AzureKey" for Azure key credential',
    )
    parser.add_argument(
        '--security-scopes',
        required=False,
        help='OAuth 2.0 scopes when "security" includes "AADToken". '
             'Sample: https://storage.azure.com/.default',
    )
    parser.add_argument(
        '--credential-types',
        required=False,
        help='[DEPRECATED] Credential types. '
             'Sample: "tokencredential" for AAD credential for OAuth 2.0 authentication; '
             '"azurekeycredential" for Azure key credential',
    )
    parser.add_argument(
        '--credential-scopes',
        required=False,
        help='[DEPRECATED] OAuth 2.0 scopes when "credential-types" includes "tokencredential". '
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

    # convert credential-types/credential-scopes to security/security-scopes for backward-compatibility
    if not args['security'] and args['credential_types']:
        if args['credential_types'] == 'tokencredential':
            args['security'] = 'AADToken'
        elif args['credential_types'] == 'azurekeycredential':
            args['security'] = 'AzureKey'
    if not args['security_scopes'] and args['credential_scopes']:
        args['security_scopes'] = args['credential_scopes']

    succeeded = generate(sdk_root, **args)
    if succeeded:
        succeeded = compile_package(sdk_root, GROUP_ID, args['module'])

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
