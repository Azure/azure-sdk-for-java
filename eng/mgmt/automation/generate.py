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
import collections
import urllib.parse
from typing import Tuple

pwd = os.getcwd()
os.chdir(os.path.abspath(os.path.dirname(sys.argv[0])))
from parameters import *
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
):
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

    update_service_ci_and_pom(sdk_root, service)
    update_root_pom(sdk_root, service)
    update_version(sdk_root, service)

    return True


def compile_package(sdk_root, service):
    module = ARTIFACT_FORMAT.format(service)
    if os.system(
            'mvn --no-transfer-progress clean verify package -f {0}/pom.xml -pl {1}:{2} -am'.format(
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
    first_version_part += '\n\n' + changelog.strip() + '\n\n'

    with open(changelog_file, 'w') as fout:
        fout.write(first_version_part +
                   old_changelog[first_version.end() + second_version.start():])

    logging.info('[Changelog][Success] Write to changelog')


def compare_with_maven_package(sdk_root, service, stable_version,
                               current_version):
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
        if changelog and changelog.strip() != '':
            changelog_file = os.path.join(
                sdk_root,
                CHANGELOG_FORMAT.format(service = service,
                                        artifact_id = module))
            update_changelog(changelog_file, changelog)
        else:
            logging.error('[Changelog][Skip] Cannot get changelog')
    finally:
        os.remove(old_jar)


def add_module_to_modules(modules: str, module: str) -> str:
    post_module = re.search(r'([^\S\n\r]*)</modules>', modules)
    indent = post_module.group(1)
    indent += '  '

    all_module = set(re.findall(r'<module>(.*?)</module>', modules))
    all_module.add(module)
    all_module = [
        indent + POM_MODULE_FORMAT.format(module)
        for module in sorted(all_module)
    ]

    return '<modules>\n' + ''.join(all_module) + post_module.group()


def add_module_to_default_profile(pom: str, module: str) -> Tuple[bool, str]:
    for profile in re.finditer(r'<profile>[\s\S]*?</profile>', pom):
        profile_value = profile.group()
        if re.search(r'<id>default</id>', profile_value):
            if len(re.findall('<modules>', profile_value)) > 1:
                logging.error(
                    '[POM][Profile][Skip] find more than one <modules> in <profile> default'
                )
                return (False, '')
            modules = re.search(r'<modules>[\s\S]*</modules>', profile_value)
            if not modules:
                logging.error(
                    '[POM][Profile][Skip] Cannot find <modules> in <profile> default'
                )
                return (False, '')
            modules_update = add_module_to_modules(modules.group(), module)
            pre_modules = pom[:profile.start() + modules.start()]
            post_modules = pom[profile.start() + modules.end():]
            return (True, pre_modules + modules_update + post_modules)
    logging.error(
        '[POM][Profile][Skip] cannot find <profile> with <id> default')
    return (False, '')


def add_module_to_pom(pom: str, module: str) -> Tuple[bool, str]:
    if pom.find('<module>{0}</module>'.format(module)) >= 0:
        logging.info('[POM][Skip] pom already has module {0}'.format(module))
        return (True, pom)

    if len(re.findall('<modules>', pom)) > 1:
        if pom.find('<profiles>') >= 0:
            return add_module_to_default_profile(pom, module)
        logging.error('[POM][Skip] find more than one <modules> in pom')
        return (False, '')

    modules = re.search(r'<modules>[\s\S]*</modules>', pom)
    if not modules:
        logging.error('[POM][Skip] Cannot find <modules> in pom')
        return (False, '')

    modules_update = add_module_to_modules(modules.group(), module)
    pre_modules = pom[:modules.start()]
    post_modules = pom[modules.end():]
    return (True, pre_modules + modules_update + post_modules)


def update_root_pom(sdk_root: str, service: str):
    pom_file = os.path.join(sdk_root, 'pom.xml')
    if not os.path.exists(pom_file):
        logging.error('[POM][Skip] cannot find root pom')
        return

    module = 'sdk/{0}'.format(service)
    with open(pom_file, 'r') as fin:
        pom = fin.read()

    logging.info('[POM][Process] dealing with root pom')
    success, pom = add_module_to_pom(pom, module)
    if success:
        with open(pom_file, 'w') as fout:
            fout.write(pom)
        logging.info('[POM][Success] Write to root pom')


def update_service_ci_and_pom(sdk_root: str, service: str):
    folder = os.path.join(sdk_root, 'sdk/{0}'.format(service))
    module = ARTIFACT_FORMAT.format(service)
    ci_yml_file = os.path.join(folder, 'ci.yml')
    pom_xml_file = os.path.join(folder, 'pom.xml')

    if os.path.exists(ci_yml_file):
        with open(ci_yml_file, 'r') as fin:
            ci_yml = yaml.safe_load(fin)
        sdk_type: str = ci_yml.get('extends', dict()).get('parameters', dict()).get('SDKType', '')
        if type(sdk_type) == str and sdk_type.lower() == 'data':
            os.rename(ci_yml_file, os.path.join(os.path.dirname(ci_yml_file), 'ci.data.yml'))
            ci_yml = yaml.safe_load(CI_FORMAT.format(service, module))
    else:
        ci_yml = yaml.safe_load(CI_FORMAT.format(service, module))

    if not (type(ci_yml.get('extends')) == dict and
            type(ci_yml['extends'].get('parameters')) == dict and
            type(ci_yml['extends']['parameters'].get('Artifacts')) == list):
        logging.error('[CI][Skip] Unexpected ci.yml format')
    else:
        artifacts: list = ci_yml['extends']['parameters']['Artifacts']
        for artifact in artifacts:
            if (artifact.get('name') == module and
                    artifact.get('groupId') == GROUP_ID):
                logging.info(
                    '[CI][Skip] ci.yml already has module {0}'.format(module))
                break
        else:
            artifacts.append({
                'name': module,
                'groupId': GROUP_ID,
                'safeName': module.replace('-', '')
            })
            ci_yml_str = yaml.dump(ci_yml,
                                   sort_keys = False,
                                   Dumper = ListIndentDumper)
            ci_yml_str = re.sub('(\n\S)', r'\n\1', ci_yml_str)

            with open(ci_yml_file, 'w') as fout:
                fout.write(CI_HEADER)
                fout.write(ci_yml_str)
            logging.info('[CI][Success] Write to ci.yml')

    if os.path.exists(pom_xml_file):
        with open(pom_xml_file, 'r') as fin:
            pom_xml = fin.read()
    else:
        pom_xml = POM_FORMAT.format(service = service,
                                    group_id = GROUP_ID,
                                    artifact_id = module)

    logging.info('[POM][Process] dealing with pom.xml')
    success, pom_xml = add_module_to_pom(pom_xml, module)
    if success:
        with open(pom_xml_file, 'w') as fout:
            fout.write(pom_xml)
        logging.info('[POM][Success] Write to pom.xml')


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


def update_version(sdk_root: str, service: str):
    pwd = os.getcwd()
    try:
        os.chdir(sdk_root)
        print(os.getcwd())
        subprocess.run(
            'python3 eng/versioning/update_versions.py --ut library --bt client --sr',
            stdout = subprocess.DEVNULL,
            stderr = sys.stderr,
            shell = True,
        )
        subprocess.run(
            'python3 eng/versioning/update_versions.py --ut library --bt client --tf {0}/README.md'
            .format(OUTPUT_FOLDER_FORMAT.format(service)),
            stdout = subprocess.DEVNULL,
            stderr = sys.stderr,
            shell = True,
        )
    finally:
        os.chdir(pwd)


def write_version(
    version_file: str,
    lines: list,
    index: int,
    project: str,
    stable_version: str,
    current_version: str,
):
    lines[index] = '{0};{1};{2}'.format(project, stable_version,
                                        current_version)
    with open(version_file, 'w') as fout:
        fout.write('\n'.join(lines))
        fout.write('\n')


def set_or_increase_version(
    sdk_root: str,
    service: str,
    preview = True,
    version = None,
    **kwargs,
) -> Tuple[str, str]:
    version_file = os.path.join(sdk_root, 'eng/versioning/version_client.txt')
    module = ARTIFACT_FORMAT.format(service)
    project = '{0}:{1}'.format(GROUP_ID, module)
    version_pattern = '(\d+)\.(\d+)\.(\d+)(-beta\.\d+)?'
    version_format = '{0}.{1}.{2}{3}'
    default_version = version if version else DEFAULT_VERSION

    with open(version_file, 'r') as fin:
        lines = fin.read().splitlines()
        version_index = -1
        for i, version_line in enumerate(lines):
            version_line = version_line.strip()
            if version_line.startswith('#'):
                continue
            versions = version_line.split(';')
            if versions[0] == project:
                if len(versions) != 3:
                    logging.error(
                        '[VERSION][Fallback] Unexpected version format "{0}"'.
                        format(version_line))
                    stable_version = ''
                    current_version = default_version
                else:
                    logging.info(
                        '[VERSION][Found] current version "{0}"'.format(
                            version_line))
                    stable_version = versions[1]
                    current_version = versions[2]
                version_index = i
                break
        else:
            logging.info(
                '[VERSION][Not Found] cannot find version for "{0}"'.format(
                    project))
            for i, version_line in enumerate(lines):
                if version_line.startswith('{0}:'.format(GROUP_ID)):
                    version_index = i + 1
            lines = lines[:version_index] + [''] + lines[version_index:]
            stable_version = ''
            current_version = default_version

    # version is given, set and return
    if version:
        if not stable_version:
            stable_version = version
        logging.info(
            '[VERSION][Set] set to given version "{0}"'.format(version))
        write_version(version_file, lines, version_index, project,
                      stable_version, version)
        return stable_version, version

    current_versions = list(re.findall(version_pattern, current_version)[0])
    stable_versions = re.findall(version_pattern, stable_version)
    # no stable version
    if len(stable_versions) < 1 or stable_versions[0][-1] != '':
        if not preview:
            current_versions[-1] = ''
        current_version = version_format.format(*current_versions)
        if not stable_version:
            stable_version = current_version
        logging.info(
            '[VERSION][Not Found] cannot find stable version, current version "{0}"'
            .format(current_version))

        write_version(version_file, lines, version_index, project,
                      stable_version, current_version)
    else:
        # TODO: auto-increase for stable version and beta version if possible
        current_version = version_format.format(*current_versions)
        if not stable_version:
            stable_version = current_version
        logging.warning(
            '[VERSION][Not Implement] set to current version "{0}" by default'.
            format(current_version))

        write_version(version_file, lines, version_index, project,
                      stable_version, current_version)

    return stable_version, current_version


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
            logging.error(
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

            stable_version, current_version = set_or_increase_version(
                sdk_root,
                service,
            )
            generate(
                sdk_root,
                service,
                spec_root = config['specFolder'],
                readme = readme,
                autorest = AUTOREST_CORE_VERSION,
                use = AUTOREST_JAVA,
                tag = tag,
            )
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
                    'succeeded',
            })

            update_parameters(pre_suffix)

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
    stable_version, current_version = set_or_increase_version(sdk_root, **args)
    args['version'] = current_version
    generate(sdk_root, **args)

    compile_package(sdk_root, service)
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


if __name__ == '__main__':
    logging.basicConfig(
        level = logging.INFO,
        format = '%(asctime)s %(levelname)s %(message)s',
        datefmt = '%Y-%m-%d %X',
    )
    main()