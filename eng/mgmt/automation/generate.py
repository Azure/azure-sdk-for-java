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
import collections
import urllib.parse

pwd = os.getcwd()
os.chdir(os.path.abspath(os.path.dirname(sys.argv[0])))
from parameters import *
os.chdir(pwd)


def generate(
    sdk_root: str,
    service: str,
    spec_root: str,
    readme: str,
    autorest: str,
    use: str,
    tag: str = None,
    version: str = None,
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
        ' '.join((tag_option, version_option, FLUENTLITE_ARGUMENTS, readme)),
    )
    logging.info(command)
    if os.system(command) != 0:
        logging.error('[GENERATE] Autorest fail')
        return False

    update_service_ci_and_pom(sdk_root, service)
    update_root_pom(sdk_root, service)
    update_version(sdk_root, service)
    if os.system(
            'mvn clean verify package -f {0}/pom.xml -pl {1}:{2} -am'.format(
                sdk_root, GROUP_ID, module)) != 0:
        logging.error('[GENERATE] Maven build fail')
        return False

    return True


def add_module_to_pom(pom: str, module: str) -> (bool, str):
    if pom.find('<module>{0}</module>'.format(module)) >= 0:
        logging.info('[POM][Skip] pom already has module {0}'.format(module))
        return (False, '')

    if len(re.findall('<modules>', pom)) > 1:
        logging.error('[POM][Skip] find more than one <modules> in pom')
        return (False, '')

    pre_module = re.match(r'^[\s\S]*?<modules>', pom, re.M)
    if not pre_module:
        logging.error('[POM][Skip] Cannot find <modules> in pom')
        return (False, '')

    post_module = re.search(r'[^\S\r\n]*</modules>[\s\S]*$', pom)
    if not post_module:
        logging.error('[POM][Skip] Cannot find </modules> in pom')
        return (False, '')

    modules = set(re.findall(r'<module>(.*?)</module>', pom))
    modules.add(module)
    modules = [POM_MODULE_FORMAT.format(module) for module in sorted(modules)]

    return (True,
            pre_module.group() + '\n' + ''.join(modules) + post_module.group())


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
            ci_yml_str = yaml.safe_dump(ci_yml, sort_keys = False)
            ci_yml_str = re.sub('(\n\S)', r'\n\1', ci_yml_str)

            with open(ci_yml_file, 'w') as fout:
                fout.write(CI_HEADER)
                fout.write(ci_yml_str)
            logging.info('[CI][Success] Write to ci.yml')

    if os.path.exists(pom_xml_file):
        with open(pom_xml_file, 'r') as fin:
            pom_xml = fin.read()
    else:
        pom_xml = POM_FORMAT.format(service)

    logging.info('[POM][Process] dealing with pom.xml')
    success, pom_xml = add_module_to_pom(pom_xml, module)
    if success:
        with open(pom_xml_file, 'w') as fout:
            fout.write(pom_xml)
        logging.info('[POM][Success] Write to pom.xml')


def update_version(sdk_root: str, service: str):
    pwd = os.getcwd()
    try:
        os.chdir(sdk_root)
        print(os.getcwd())
        os.system(
            'python3 eng/versioning/update_versions.py --ut library --bt client --sr'
        )
        os.system(
            'python3 eng/versioning/update_versions.py --ut library --bt client --tf {0}/README.md'
            .format(OUTPUT_FOLDER_FORMAT.format(service)))
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


def set_or_increase_version_and_generate(
    sdk_root: str,
    service: str,
    preview = True,
    version = None,
    **kwargs,
):
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
            stable_version = current_version
        logging.info(
            '[VERSION][Set] set to given version "{0}"'.format(version))
        write_version(version_file, lines, version_index, project,
                      stable_version, current_version)
        generate(sdk_root, service, version = version, **kwargs)
        return

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
        generate(sdk_root, service, version = current_version, **kwargs)
    else:
        ##### Update version later
        ##### currently there always isn't stable version
        current_version = version_format.format(*current_versions)
        if not stable_version:
            stable_version = current_version
        logging.warning(
            '[VERSION][Not Implement] set to current version "{0}" by default'.
            format(current_version))

        write_version(version_file, lines, version_index, project,
                      stable_version, current_version)
        generate(sdk_root, service, version = current_version, **kwargs)


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
        '--auto-commit-generated-code',
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


def valid_service(service: str):
    return re.sub('[^a-z0-9_]', '', service.lower())


def get_and_update_api_specs(
    api_specs_file: str,
    spec: str,
    service: str = None,
):
    SPECIAL_SPEC = {'resources'}
    if spec in SPECIAL_SPEC:
        if not service:
            service = spec
        return valid_service(service)

    with open(api_specs_file) as fin:
        lines = fin.readlines()

    comment = ''

    for i, line in enumerate(lines):
        if not line.strip().startswith('#'):
            comment = ''.join(lines[:i])
            api_specs = yaml.safe_load(''.join(lines[i:]))
            break

    api_spec = api_specs.get(spec)
    if not service:
        if api_spec:
            service = api_spec.get('service')
        if not service:
            service = valid_service(spec)

    if service != spec:
        api_specs[spec] = dict() if not api_spec else api_spec
        api_specs[spec]['service'] = service

    with open(api_specs_file, 'w') as fout:
        fout.write(comment)
        fout.write(yaml.safe_dump(api_specs, sort_keys = False))

    return valid_service(service)


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
            service = get_and_update_api_specs(api_specs_file, spec)
            set_or_increase_version_and_generate(
                sdk_root,
                service,
                spec_root = config['specFolder'],
                readme = readme,
                autorest = AUTOREST_CORE_VERSION,
                use = AUTOREST_JAVA)

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

    service = get_and_update_api_specs(api_specs_file, spec, args['service'])
    args['service'] = service
    set_or_increase_version_and_generate(sdk_root, **args)

    if args.get('auto_commit_generated_code') and args.get(
            'user_name') and args.get('user_email'):
        os.system('git add {0}'.format(
            os.path.abspath(
                os.path.join(sdk_root, OUTPUT_FOLDER_FORMAT.format(service)))))
        os.system(
            'git -c user.name={0} -c user.email={1} commit -m "[Automation] Generate Fluent Lite from {2}#{3}"'
            .format(args['user_name'], args['user_email'], spec,
                    args.get('tag', '')))


if __name__ == '__main__':
    logging.basicConfig(
        level = logging.DEBUG,
        format = '%(asctime)s %(levelname)s %(message)s',
        datefmt = '%Y-%m-%d %X',
    )
    main()