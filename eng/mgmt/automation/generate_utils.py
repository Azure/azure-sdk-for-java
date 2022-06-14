#!/usr/bin/env python3
import os
import re
import sys
import json
import yaml
import shutil
import logging
import requests
import tempfile
import subprocess
import urllib.parse
from typing import Tuple

pwd = os.getcwd()
#os.chdir(os.path.abspath(os.path.dirname(sys.argv[0])))
from parameters import *
from utils import update_service_ci_and_pom
from utils import update_root_pom
from utils import update_version
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
    output_folder: str,
    module: str,
    namespace: str,
    tag: str = None,
    version: str = None,
    autorest_options: str = '',
    **kwargs,
) -> bool:
    output_dir = os.path.join(
        sdk_root,
        'sdk/{0}'.format(service),
        module,
    )

    require_sdk_integration = not os.path.exists(os.path.join(output_dir, 'src'))

    shutil.rmtree(os.path.join(output_dir, 'src/main'), ignore_errors=True)
    if os.path.exists(os.path.join(output_dir, 'src/samples/README.md')):
        # samples contains hand-written code
        shutil.rmtree(os.path.join(output_dir, 'src/samples/java', namespace.replace('.', '/'), 'generated'),
                      ignore_errors=True)
    else:
        shutil.rmtree(os.path.join(output_dir, 'src/samples'), ignore_errors=True)

    if re.match(r'https?://', spec_root):
        readme = urllib.parse.urljoin(spec_root, readme)
    else:
        readme = os.path.join(spec_root, readme)

    tag_option = '--tag={0}'.format(tag) if tag else ''
    version_option = '--package-version={0}'.format(version) if version else ''

    command = 'autorest --version={0} --use={1} --java --java.java-sdks-folder={2} --java.output-folder={3} ' \
              '--java.namespace={4} {5}'\
        .format(
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

    group = GROUP_ID
    if require_sdk_integration:
        update_service_ci_and_pom(sdk_root, service, group, module)
        update_root_pom(sdk_root, service)
    update_version(sdk_root, output_folder)

    return True


def compile_package(sdk_root, module) -> bool:
    if os.system(
            'mvn --no-transfer-progress clean verify package -f {0}/pom.xml -Dmaven.javadoc.skip -Dgpg.skip -Drevapi.skip -pl {1}:{2} -am'.format(
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


def compare_with_maven_package(sdk_root: str, service: str, stable_version: str,
                               current_version: str, module: str):
    if stable_version == current_version:
        logging.info('[Changelog][Skip] no previous version')
        return

    if '-beta.' in current_version and '-beta.' not in stable_version:
        # if current version is preview, try compare it with a previous preview release

        version_pattern = r'\d+\.\d+\.\d+-beta\.(\d+)?'
        beta_version_int = int(re.match(version_pattern, current_version).group(1))
        if beta_version_int > 1:
            previous_beta_version_int = beta_version_int - 1
            previous_beta_version = current_version.replace(
                '-beta.' + str(beta_version_int),
                '-beta.' + str(previous_beta_version_int))
            stable_version = previous_beta_version

    logging.info('[Changelog] Compare stable version {0} with current version {1}'.format(stable_version, current_version))

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
    module: str,
) -> str:
    version_file = os.path.join(sdk_root, 'eng/versioning/version_client.txt')
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
        fout.write(yaml.dump(api_specs, Dumper=ListIndentDumper))


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

    return None
