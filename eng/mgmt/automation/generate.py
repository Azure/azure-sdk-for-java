#!/usr/bin/env python3
import os
import re
import sys
import json
import glob
import yaml
import shutil
import logging
import argparse
import requests
import tempfile
import subprocess
import urllib.parse
from typing import Tuple

pwd = os.getcwd()
os.chdir(os.path.abspath(os.path.dirname(sys.argv[0])))
from parameters import *
from utils import set_or_increase_version
from utils import update_service_ci_and_pom
from utils import update_root_pom
from utils import update_version
from generate_data import sdk_automation as sdk_automation_data
os.chdir(pwd)


# Add two more indent for list in yaml dump
class ListIndentDumper(yaml.SafeDumper):

    def increase_indent(self, flow = False, indentless = False):
        return super(ListIndentDumper, self).increase_indent(flow, False)


def generate(
    sdk_root: str,
    service: str,
    spec_root: str,
    readme: str,
    autorest: str,
    use: str,
    tag: str = None,
    version: str = None,
    autorest_options: str = '',
    **kwargs,
) -> bool:
    module = ARTIFACT_FORMAT.format(service)
    namespace = NAMESPACE_FORMAT.format(service)
    output_dir = os.path.join(
        sdk_root,
        'sdk/{0}'.format(service),
        module,
    )
    shutil.rmtree(os.path.join(output_dir, 'src/main'), ignore_errors = True)
    if os.path.exists(os.path.join(output_dir, 'src/samples/README.md')):
        # samples contains hand-written code
        shutil.rmtree(os.path.join(output_dir, 'src/samples/java', namespace.replace('.', '/'), 'generated'), ignore_errors = True)
    else:
        shutil.rmtree(os.path.join(output_dir, 'src/samples'), ignore_errors = True)

    if re.match(r'https?://', spec_root):
        readme = urllib.parse.urljoin(spec_root, readme)
    else:
        readme = os.path.join(spec_root, readme)

    tag_option = '--tag={0}'.format(tag) if tag else ''
    version_option = '--package-version={0}'.format(version) if version else ''

    command = 'autorest --version={0} --use={1} --java.azure-libraries-for-java-folder={2} --java.output-folder={3} --java.namespace={4} {5}'.format(
        autorest,
        use,
        os.path.abspath(sdk_root),
        os.path.abspath(output_dir),
        namespace,
        ' '.join((tag_option, version_option, FLUENTLITE_ARGUMENTS, autorest_options, readme)),
    )
    logging.info(command)
    if os.system(command) != 0:
        logging.error('[GENERATE] Autorest fail')
        return False

    module = ARTIFACT_FORMAT.format(service)
    group = GROUP_ID
    output_folder = OUTPUT_FOLDER_FORMAT.format(service)
    update_service_ci_and_pom(sdk_root, service, group, module)
    update_root_pom(sdk_root, service)
    update_version(sdk_root, output_folder)

    return True


def compile_package(sdk_root, service) -> bool:
    module = ARTIFACT_FORMAT.format(service)
    if os.system(
            'mvn --no-transfer-progress clean verify package -f {0}/pom.xml -Dgpg.skip -Drevapi.skip -pl {1}:{2} -am'.format(
                sdk_root, GROUP_ID, module)) != 0:
        logging.error('[COMPILE] Maven build fail')
        return False
    return True


def generate_changelog_and_breaking_change(
    sdk_root,
    old_jar,
    new_jar,
    **kwargs,
) -> Tuple[bool, str]:
    logging.info('[CHANGELOG] changelog jar: {0} -> {1}'.format(
        old_jar, new_jar))
    stdout = subprocess.run(
        'mvn --no-transfer-progress clean compile exec:java -q -f {0}/eng/mgmt/changelog/pom.xml -DOLD_JAR="{1}" -DNEW_JAR="{2}"'
        .format(sdk_root, old_jar, new_jar),
        stdout = subprocess.PIPE,
        shell = True,
    ).stdout
    logging.info('[CHANGELOG] changelog output: {0}'.format(stdout))

    config = json.loads(stdout)
    return (config.get('breaking', False), config.get('changelog', ''))


def update_changelog(changelog_file, changelog):
    version_pattern = '^## (\d+\.\d+\.\d+(?:-[\w\d\.]+)?) \((.*?)\)'
    with open(changelog_file, 'r') as fin:
        old_changelog = fin.read()

    first_version = re.search(version_pattern, old_changelog, re.M)
    if not first_version:
        logging.error(
            '[Changelog][Skip] Cannot read first version from {}'.format(
                changelog_file))
        return

    left = old_changelog[first_version.end():]
    second_version = re.search(version_pattern, left, re.M)
    if not second_version:
        logging.error(
            '[Changelog][Skip] Cannot read second version from {}'.format(
                changelog_file))
        return

    first_version_part = old_changelog[:first_version.end() +
                                       second_version.start()]
    # remove text starting from the first '###' (usually the block '### Features Added')
    first_version_part = re.sub('\n###.*', '\n', first_version_part, re.S)
    first_version_part = re.sub('\s+$', '', first_version_part)

    first_version_part += '\n\n'
    if changelog.strip() != '':
        first_version_part += changelog.strip() + '\n\n'

    with open(changelog_file, 'w') as fout:
        fout.write(first_version_part +
                   old_changelog[first_version.end() + second_version.start():])

    logging.info('[Changelog][Success] Write to changelog')


def compare_with_maven_package(sdk_root, service, stable_version,
                               current_version):
    logging.info('[Changelog] Compare stable version {0} with current version {1}'.format(stable_version, current_version))

    if stable_version == current_version:
        logging.info('[Changelog][Skip] no previous version')
        return

    module = ARTIFACT_FORMAT.format(service)
    r = requests.get(
        MAVEN_URL.format(group_id = GROUP_ID.replace('.', '/'),
                         artifact_id = module,
                         version = stable_version))
    r.raise_for_status()
    old_jar_fd, old_jar = tempfile.mkstemp('.jar')
    try:
        with os.fdopen(old_jar_fd, 'wb') as tmp:
            tmp.write(r.content)
        new_jar = os.path.join(
            sdk_root,
            JAR_FORMAT.format(service = service,
                              artifact_id = module,
                              version = current_version))
        if not os.path.exists(new_jar):
            raise Exception('Cannot found built jar in {0}'.format(new_jar))
        breaking, changelog = generate_changelog_and_breaking_change(
            sdk_root, old_jar, new_jar)
        if changelog:
            changelog_file = os.path.join(
                sdk_root,
                CHANGELOG_FORMAT.format(service = service,
                                        artifact_id = module))
            update_changelog(changelog_file, changelog)
        else:
            logging.error('[Changelog][Skip] Cannot get changelog')
    finally:
        os.remove(old_jar)


def get_version(
    sdk_root: str,
    service: str,
) -> str:
    version_file = os.path.join(sdk_root, 'eng/versioning/version_client.txt')
    module = ARTIFACT_FORMAT.format(service)
    project = '{0}:{1}'.format(GROUP_ID, module)

    with open(version_file, 'r') as fin:
        for line in fin.readlines():
            version_line = line.strip()
            if version_line.startswith('#'):
                continue
            versions = version_line.split(';')
            if versions[0] == project:
                return version_line
    logging.error('Cannot get version of {0}'.format(project))
    return None


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '--spec-root',
        default =
        'https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/',
        help = 'Spec root folder',
    )
    parser.add_argument(
        '-r',
        '--readme',
        help =
        'Readme path, Sample: "storage" or "specification/storage/resource-manager/readme.md"',
    )
    parser.add_argument('-t', '--tag', help = 'Specific tag')
    parser.add_argument('-v', '--version', help = 'Specific sdk version')
    parser.add_argument(
        '-s',
        '--service',
        help = 'Service Name if not the same as spec name',
    )
    parser.add_argument(
        '-u',
        '--use',
        default = AUTOREST_JAVA,
        help = 'Autorest java plugin',
    )
    parser.add_argument(
        '--autorest',
        default = AUTOREST_CORE_VERSION,
        help = 'Autorest version',
    )
    parser.add_argument(
        '--autorest-options',
        default = '',
        help = 'Additional autorest options',
    )
    parser.add_argument('--suffix', help = 'Suffix for namespace and artifact')
    parser.add_argument(
        '--auto-commit-external-change',
        action = 'store_true',
        help = 'Automatic commit the generated code',
    )
    parser.add_argument('--user-name', help = 'User Name for commit')
    parser.add_argument('--user-email', help = 'User Email for commit')
    parser.add_argument(
        'config',
        nargs = '*',
    )

    return parser.parse_args()


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


def valid_service(service: str):
    return re.sub('[^a-z0-9_]', '', service.lower())


def read_api_specs(api_specs_file: str) -> Tuple[str, dict]:
    # return comment and api_specs

    with open(api_specs_file) as fin:
        lines = fin.readlines()

    comment = ''

    for i, line in enumerate(lines):
        if not line.strip().startswith('#'):
            comment = ''.join(lines[:i])
            api_specs = yaml.safe_load(''.join(lines[i:]))
            break
    else:
        raise Exception('api-specs.yml should has non comment line')

    return comment, api_specs


def write_api_specs(api_specs_file: str, comment: str, api_specs: dict):
    with open(api_specs_file, 'w') as fout:
        fout.write(comment)
        fout.write(yaml.dump(api_specs, Dumper = ListIndentDumper))


def get_and_update_service_from_api_specs(
    api_specs_file: str,
    spec: str,
    service: str = None,
):
    SPECIAL_SPEC = {'resources'}
    if spec in SPECIAL_SPEC:
        if not service:
            service = spec
        return valid_service(service)

    comment, api_specs = read_api_specs(api_specs_file)

    api_spec = api_specs.get(spec)
    if not service:
        if api_spec:
            service = api_spec.get('service')
        if not service:
            service = spec
    service = valid_service(service)

    if service != spec:
        api_specs[spec] = dict() if not api_spec else api_spec
        api_specs[spec]['service'] = service

    write_api_specs(api_specs_file, comment, api_specs)

    return service


def get_suffix_from_api_specs(api_specs_file: str, spec: str):
    comment, api_specs = read_api_specs(api_specs_file)

    api_spec = api_specs.get(spec)
    if api_spec and api_spec.get('suffix'):
        return api_spec.get('suffix')

    return SUFFIX


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
            stable_version, current_version = set_or_increase_version(
                sdk_root,
                GROUP_ID,
                module
            )
            succeeded = generate(
                sdk_root,
                service,
                spec_root = config['specFolder'],
                readme = readme,
                autorest = AUTOREST_CORE_VERSION,
                use = AUTOREST_JAVA,
                tag = tag,
            )
            if succeeded:
                compile_package(sdk_root, service)

            generated_folder = OUTPUT_FOLDER_FORMAT.format(service)
            packages.append({
                'packageName':
                    '{0}'.format(ARTIFACT_FORMAT.format(service)),
                'path': [
                    generated_folder,
                    CI_FILE_FORMAT.format(service),
                    POM_FILE_FORMAT.format(service),
                    'eng/versioning',
                    'pom.xml',
                ],
                'readmeMd': [readme],
                'artifacts': [
                    '{0}/pom.xml'.format(generated_folder),
                ] + [
                    jar for jar in glob.glob('{0}/target/*.jar'.format(
                        generated_folder))
                ],
                'result':
                    'succeeded' if succeeded else 'failed',
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
    succeeded = generate(sdk_root, **args)

    if succeeded:
        succeeded = compile_package(sdk_root, service)
        if succeeded:
            compare_with_maven_package(sdk_root, service, stable_version,
                                    current_version)

            if args.get('auto_commit_external_change') and args.get(
                    'user_name') and args.get('user_email'):
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
        level = logging.INFO,
        format = '%(asctime)s %(levelname)s %(message)s',
        datefmt = '%Y-%m-%d %X',
    )
    main()
