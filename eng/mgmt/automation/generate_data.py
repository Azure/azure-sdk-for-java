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
import requests
from typing import Dict, List, Tuple

from parameters import *
from utils import set_or_default_version
from utils import update_service_ci_and_pom
from utils import update_root_pom
from utils import ListIndentDumper


GROUP_ID = 'com.azure'
LLC_ARGUMENTS = '--data-plane --sdk-integration --generate-samples --generate-tests'


def sdk_automation(config: dict) -> List[dict]:
    # 1. README.java.md in spec repo, and it contains 'output-folder' option.
    # 2. swagger/README.md in sdk repo, and it is sdk/<service>/<module>/swagger/README.md
    # 3. Use default options, run integration task.

    base_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
    sdk_root = os.path.abspath(os.path.join(base_dir, SDK_ROOT))
    spec_root = os.path.abspath(config['specFolder'])

    packages = []

    readme_file_paths = []
    for file_path in config['relatedReadmeMdFiles']:
        match = re.search(
            r'specification/([^/]+)/data-plane(/.*)*/readme.md',
            file_path,
            re.IGNORECASE,
        )
        if match:
            readme_file_paths.append(file_path)

    processed_files = []

    for file_path in config['changedFiles']:
        match = re.search(
            r'specification/([^/]+)/data-plane/.*/([^/]+).json',
            file_path,
            re.IGNORECASE,
        )
        if match and '/examples/' not in file_path and os.path.isfile(os.path.join(spec_root, file_path)):
            service = match.group(1)
            file_name = match.group(2)

            readme_file_path = find_readme(file_path, readme_file_paths, spec_root)

            if (readme_file_path and readme_file_path in processed_files) \
                    or (file_name in processed_files):
                continue
            else:
                if readme_file_path:
                    processed_files.append(readme_file_path)
                else:
                    processed_files.append(file_name)

                file_path = os.path.join(spec_root, file_path)
                readme_file_path = os.path.join(spec_root, readme_file_path) if readme_file_path else None
                sdk_automation_readme(readme_file_path, file_name, file_path, packages, service, sdk_root)
        else:
            logging.info('[Skip] changed file {0}'.format(file_path))

    for readme_file_path in readme_file_paths:
        if readme_file_path in processed_files:
            pass
        else:
            match = re.search(
                r'specification/([^/]+)/data-plane(/.*)*/readme.md',
                readme_file_path,
                re.IGNORECASE,
            )
            if match:
                service = match.group(1)

                processed_files.append(readme_file_path)

                readme_file_path = os.path.join(spec_root, readme_file_path)
                sdk_automation_readme(readme_file_path, None, None, packages, service, sdk_root)

    return packages


def sdk_automation_readme(readme_file_abspath: str,
                          file_name: str, file_abspath: str,
                          packages: List[dict],
                          service: str, sdk_root: str):
    input_file, service, module = get_generate_parameters(service, file_name, file_abspath, readme_file_abspath)

    if module:
        succeeded = generate(sdk_root, input_file,
                             service=service, module=module, security='', security_scopes='', title='',
                             autorest=AUTOREST_CORE_VERSION, use=AUTOREST_JAVA,
                             autorest_options='', readme=readme_file_abspath)

        generated_folder = 'sdk/{0}/{1}'.format(service, module)

        if succeeded:
            compile_package(sdk_root, GROUP_ID, module)

        artifacts = [
            '{0}/pom.xml'.format(generated_folder)
        ]
        artifacts += [
            jar for jar in glob.glob('{0}/target/*.jar'.format(generated_folder))
        ]
        result = 'succeeded' if succeeded else 'failed'

        packages.append({
            'packageName': module,
            'path': [
                generated_folder,
                CI_FILE_FORMAT.format(service),
                POM_FILE_FORMAT.format(service),
                'eng/versioning',
                'pom.xml'
            ],
            'artifacts': artifacts,
            'apiViewArtifact': next(iter(glob.glob('{0}/target/*-sources.jar'.format(generated_folder))), None),
            'language': 'Java',
            'result': result,
        })


def generate(
    sdk_root: str,
    input_file: str,
    service: str,
    module: str,
    security: str,
    security_scopes: str,
    title: str,
    autorest: str,
    use: str,
    autorest_options: str = '',
    readme: str = None,
    **kwargs,
) -> bool:
    namespace = 'com.{0}'.format(module.replace('-', '.'))
    output_dir = os.path.join(
        sdk_root,
        'sdk', service, module
    )
    # shutil.rmtree(os.path.join(output_dir, 'src/main'), ignore_errors=True)
    shutil.rmtree(os.path.join(output_dir, 'src/samples/java', namespace.replace('.', '/'), 'generated'),
                  ignore_errors=True)
    shutil.rmtree(os.path.join(output_dir, 'src/tests/java', namespace.replace('.', '/'), 'generated'),
                  ignore_errors=True)

    if readme:
        # use readme from spec repo
        readme_file_path = readme

        require_sdk_integration = not os.path.exists(os.path.join(output_dir, 'src'))

        logging.info('[GENERATE] Autorest from README {}'.format(readme_file_path))

        command = 'autorest --version={0} --use={1} --java --java.java-sdks-folder={2} --java.output-folder={3} {4} '\
            .format(
                autorest,
                use,
                os.path.abspath(sdk_root),
                os.path.abspath(output_dir),
                readme_file_path
            )
        if require_sdk_integration:
            command += LLC_ARGUMENTS
        logging.info(command)
        try:
            subprocess.run(command, shell=True, check=True)
        except subprocess.CalledProcessError:
            logging.error('[GENERATE] Autorest fail')
            return False

        if require_sdk_integration:
            set_or_default_version(sdk_root, GROUP_ID, module)
            update_service_ci_and_pom(sdk_root, service, GROUP_ID, module)
            update_root_pom(sdk_root, service)
    else:
        readme_file_path = update_readme(output_dir, input_file, security, security_scopes, title)
        if readme_file_path:
            # use readme from SDK repo

            logging.info('[GENERATE] Autorest from README {}'.format(readme_file_path))

            command = 'autorest --version={0} --use={1} --java --java.java-sdks-folder={2} ' \
                      '--java.output-folder={2} {3}'\
                .format(
                    autorest,
                    use,
                    os.path.abspath(sdk_root),
                    os.path.abspath(output_dir),
                    readme_file_path
                )
            logging.info(command)
            try:
                subprocess.run(command, shell=True, cwd=output_dir, check=True)
            except subprocess.CalledProcessError:
                logging.error('[GENERATE] Autorest fail')
                return False
        else:
            # no readme

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
                      '--java.java-sdks-folder={2} --java.output-folder={3} ' \
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


def get_generate_parameters(
    service, file_name, json_file_path, readme_file_abspath: str
) -> Tuple[str, str, str]:
    # get parameters from README.java.md from spec repo, or fallback to parameters deduced from json file path

    input_file = json_file_path
    module = None
    if readme_file_abspath:
        # try readme.java.md, it must contain 'output-folder' and
        # match pattern $(java-sdks-folder)/sdk/<service>/<module>
        java_readme_file_path = readme_file_abspath
        if not java_readme_file_path.endswith('.java.md'):
            java_readme_file_path = readme_file_abspath.replace('.md', '.java.md')
        if uri_file_exists(java_readme_file_path):
            content = uri_file_read(java_readme_file_path)
            if content:
                yaml_blocks = re.findall(r'```\s?(?:yaml|YAML).*?\n(.*?)```', content, re.DOTALL)
                for yaml_str in yaml_blocks:
                    yaml_json = yaml.safe_load(yaml_str)
                    if 'output-folder' in yaml_json:
                        output_folder = yaml_json['output-folder']
                        output_folder_segments = output_folder.split('/')
                        if len(output_folder_segments) == 4 and output_folder_segments[0] == '$(java-sdks-folder)' \
                                and output_folder_segments[1] == 'sdk':
                            service = output_folder_segments[2]
                            module = output_folder_segments[3]

        # try swagger/readme.md for service and module
        if not module and os.path.basename(readme_file_abspath).lower() == 'readme.md':
            dir_name = os.path.dirname(readme_file_abspath).lower()
            if os.path.basename(dir_name) == 'swagger':
                dir_name = os.path.dirname(dir_name)
                module = os.path.basename(dir_name)
                dir_name = os.path.dirname(dir_name)
                service = os.path.basename(dir_name)
                dir_name = os.path.dirname(dir_name)
                if not os.path.basename(dir_name) == 'sdk':
                    module = None
                    service = None

    if not module and file_name:
        # deduce from json file path
        file_name_sans = ''.join(c for c in file_name if c.isalnum())
        module = 'azure-{0}-{1}'.format(service, file_name_sans).lower()

    return input_file, service, module


def uri_file_exists(file_path: str) -> bool:
    if file_path.startswith('http://') or file_path.startswith('https://'):
        return requests.head(file_path).status_code / 100 == 2
    else:
        return os.path.exists(file_path)


def uri_file_read(file_path: str) -> str:
    if file_path.startswith('http://') or file_path.startswith('https://'):
        return requests.get(file_path).text
    else:
        with open(file_path, 'r', encoding='utf-8') as f_in:
            return f_in.read()


def find_readme(json_file_path: str, readme_file_paths: List[str],
                spec_root: str) -> str:
    if not readme_file_paths:
        return None

    # ideally we'd like to match readme in more specific path
    readme_file_paths = sorted(readme_file_paths, key=len, reverse=True)

    json_dir_name = os.path.dirname(json_file_path)
    for readme_file_path in readme_file_paths:
        readme_dir_name = os.path.dirname(readme_file_path)
        if json_dir_name.startswith(readme_dir_name):
            java_readme_path = os.path.join(spec_root, readme_file_path).replace('.md', '.java.md')
            if os.path.exists(java_readme_path):
                return readme_file_path

    return None


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
                        if 'data-plane' in yaml_json and yaml_json['data-plane']:
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
        required=False,
        help='URL to OpenAPI 2.0 specification JSON as input file. "service" and "module" is required.',
    )
    parser.add_argument(
        '-r',
        '--readme',
        required=False,
        help='URL to "readme.md" as configuration file.',
    )
    parser.add_argument(
        '--service',
        required=False,
        help='Service name under sdk/. Sample: storage',
    )
    parser.add_argument(
        '--module',
        required=False,
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

    if args['readme']:
        readme_file_abspath = args['readme']
        if not os.path.isabs(readme_file_abspath):
            readme_file_abspath = os.path.abspath(readme_file_abspath)
        input_file, service, module = get_generate_parameters(None, None, None, readme_file_abspath)
        if not module:
            raise ValueError('readme.md not found or not well-formed')
        args['service'] = service
        args['module'] = module

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
