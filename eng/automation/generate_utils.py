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
from typing import Tuple, List, Union
from typespec_utils import validate_tspconfig

pwd = os.getcwd()
# os.chdir(os.path.abspath(os.path.dirname(sys.argv[0])))
from parameters import *
from utils import update_service_files_for_new_lib
from utils import update_root_pom
from utils import update_version
from utils import is_windows

os.chdir(pwd)


# Add two more indent for list in yaml dump
class ListIndentDumper(yaml.SafeDumper):

    def increase_indent(self, flow=False, indentless=False):
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
    autorest_options: str = "",
    **kwargs,
) -> bool:
    output_dir = os.path.join(
        sdk_root,
        "sdk/{0}".format(service),
        module,
    )

    require_sdk_integration = not os.path.exists(os.path.join(output_dir, "src"))

    remove_generated_source_code(output_dir, namespace)

    if re.match(r"https?://", spec_root):
        readme = urllib.parse.urljoin(spec_root, readme)
    else:
        readme = os.path.join(spec_root, readme)

    tag_option = "--tag={0}".format(tag) if tag else ""
    version_option = "--package-version={0}".format(version) if version else ""

    command = (
        "autorest --version={0} --use={1} --java --java.java-sdks-folder={2} --java.output-folder={3} "
        "--java.namespace={4} {5}".format(
            autorest,
            use,
            os.path.abspath(sdk_root),
            os.path.abspath(output_dir),
            namespace,
            " ".join(
                (
                    tag_option,
                    version_option,
                    FLUENTLITE_ARGUMENTS,
                    autorest_options,
                    readme,
                )
            ),
        )
    )
    logging.info(command)
    if os.system(command) != 0:
        logging.error("[GENERATE] Code generation failed.")
        logging.error(
            "Please first check if the failure happens only to Java automation, or for all SDK automations. "
            "If it happens for all SDK automations, please double check your Swagger, and check whether there is errors in ModelValidation and LintDiff. "
            "If it happens to Java alone, you can open an issue to https://github.com/Azure/autorest.java/issues. Please include the link of this Pull Request in the issue."
        )
        return False

    group = GROUP_ID
    if require_sdk_integration:
        update_service_files_for_new_lib(sdk_root, service, group, module)
        update_root_pom(sdk_root, service)
    update_version(sdk_root, output_folder)

    return True


def remove_generated_source_code(sdk_folder: str, namespace: str):
    main_folder = os.path.join(sdk_folder, "src/main")
    test_folder = os.path.join(sdk_folder, "src/test/java", namespace.replace(".", "/"), "generated")
    sample_folder = os.path.join(sdk_folder, "src/samples/java", namespace.replace(".", "/"), "generated")

    logging.info(f"Removing main source folder: {main_folder}")
    shutil.rmtree(main_folder, ignore_errors=True)

    logging.info(f"Removing generated test folder: {test_folder}")
    shutil.rmtree(test_folder, ignore_errors=True)

    logging.info(f"Removing generated samples folder: {sample_folder}")
    shutil.rmtree(sample_folder, ignore_errors=True)


def compile_arm_package(sdk_root: str, module: str) -> bool:
    if (
        os.system(
            "mvn --no-transfer-progress clean verify -f {0}/pom.xml -Dmaven.javadoc.skip -Dgpg.skip -DskipTestCompile -Djacoco.skip -Drevapi.skip -pl {1}:{2} -am".format(
                sdk_root, GROUP_ID, module
            )
        )
        != 0
    ):
        logging.error("[COMPILE] Maven build fail.")
        logging.error(
            'You can inquire in "Language - Java" Teams channel. Please include the link of this Pull Request in the query.'
        )
        return False
    return True


def generate_changelog_and_breaking_change(
    sdk_root,
    old_jar,
    new_jar,
    **kwargs,
) -> Tuple[bool, str]:
    logging.info("[CHANGELOG] changelog jar: {0} -> {1}".format(old_jar, new_jar))
    stdout = subprocess.run(
        'mvn --no-transfer-progress clean compile exec:java -q -f {0}/eng/automation/changelog/pom.xml -DOLD_JAR="{1}" -DNEW_JAR="{2}"'.format(
            sdk_root, old_jar, new_jar
        ),
        stdout=subprocess.PIPE,
        shell=True,
    ).stdout
    logging.info("[CHANGELOG] changelog output: {0}".format(stdout))

    config = json.loads(stdout)
    return (config.get("breaking", False), config.get("changelog", ""))


def update_changelog(changelog_file, changelog):
    version_pattern = "^## (\d+\.\d+\.\d+(?:-[\w\d\.]+)?) \((.*?)\)"
    with open(changelog_file, "r") as fin:
        old_changelog = fin.read()

    first_version = re.search(version_pattern, old_changelog, re.M)
    if not first_version:
        logging.error("[Changelog][Skip] Cannot read first version from {}".format(changelog_file))
        return

    left = old_changelog[first_version.end() :]
    second_version = re.search(version_pattern, left, re.M)
    if not second_version:
        logging.error("[Changelog][Skip] Cannot read second version from {}".format(changelog_file))
        return

    first_version_part = old_changelog[: first_version.end() + second_version.start()]
    # remove text starting from the first '###' (usually the block '### Features Added')
    first_version_part = re.sub("\n###.*", "\n", first_version_part, flags=re.S)
    first_version_part = re.sub("\s+$", "", first_version_part)

    first_version_part += "\n\n"
    if changelog.strip() != "":
        first_version_part += changelog.strip() + "\n\n"

    with open(changelog_file, "w") as fout:
        fout.write(first_version_part + old_changelog[first_version.end() + second_version.start() :])

    logging.info("[Changelog][Success] Write to changelog")


def compare_with_maven_package(
    sdk_root: str, group_id: str, service: str, previous_version: str, current_version: str, module: str
):
    if previous_version == current_version or previous_version is None:
        logging.info("[Changelog][Skip] no previous version")
        return False, ""

    logging.info(
        "[Changelog] Compare stable version {0} with current version {1}".format(previous_version, current_version)
    )

    r = requests.get(
        MAVEN_URL.format(
            group_id=group_id.replace(".", "/"),
            artifact_id=module,
            version=previous_version,
        )
    )
    r.raise_for_status()
    old_jar_fd, old_jar = tempfile.mkstemp(".jar")
    try:
        with os.fdopen(old_jar_fd, "wb") as tmp:
            tmp.write(r.content)
        new_jar = os.path.join(
            sdk_root,
            JAR_FORMAT.format(service=service, artifact_id=module, version=current_version),
        )
        if not os.path.exists(new_jar):
            raise Exception("Cannot found built jar in {0}".format(new_jar))
        breaking, changelog = generate_changelog_and_breaking_change(sdk_root, old_jar, new_jar)
        if changelog is not None:
            changelog_file = os.path.join(sdk_root, CHANGELOG_FORMAT.format(service=service, artifact_id=module))
            update_changelog(changelog_file, changelog)
        else:
            logging.error("[Changelog][Skip] Cannot get changelog")
    finally:
        os.remove(old_jar)
    return breaking, changelog


def get_version(
    sdk_root: str,
    group_id: str,
    module: str,
) -> Union[str, None]:
    version_file = os.path.join(sdk_root, "eng/versioning/version_client.txt")
    project = "{0}:{1}".format(group_id, module)

    with open(version_file, "r") as fin:
        for line in fin.readlines():
            version_line = line.strip()
            if version_line.startswith("#"):
                continue
            versions = version_line.split(";")
            if versions[0] == project:
                return version_line
    logging.error("Cannot get version of {0}".format(project))
    return None


def valid_service(service: str):
    return re.sub("[^a-z0-9_]", "", service.lower())


def read_api_specs(api_specs_file: str) -> Tuple[str, dict]:
    # return comment and api_specs

    with open(api_specs_file) as fin:
        lines = fin.readlines()

    comment = ""

    for i, line in enumerate(lines):
        if not line.strip().startswith("#"):
            comment = "".join(lines[:i])
            api_specs = yaml.safe_load("".join(lines[i:]))
            break
    else:
        raise Exception("api-specs.yml should has non comment line")

    return comment, api_specs


def write_api_specs(api_specs_file: str, comment: str, api_specs: dict):
    with open(api_specs_file, "w") as fout:
        fout.write(comment)
        fout.write(yaml.dump(api_specs, width=sys.maxsize, Dumper=ListIndentDumper))


def get_and_update_service_from_api_specs(
    api_specs_file: str,
    spec: str,
    service: str = None,
):
    SPECIAL_SPEC = {"resources"}
    if spec in SPECIAL_SPEC:
        if not service:
            service = spec
        return valid_service(service)

    comment, api_specs = read_api_specs(api_specs_file)

    api_spec = api_specs.get(spec)
    if not service:
        if api_spec:
            service = api_spec.get("service")
        if not service:
            service = spec
    service = valid_service(service)

    if service != spec:
        api_specs[spec] = dict() if not api_spec else api_spec
        api_specs[spec]["service"] = service

    write_api_specs(api_specs_file, comment, api_specs)

    return service


def get_suffix_from_api_specs(api_specs_file: str, spec: str):
    comment, api_specs = read_api_specs(api_specs_file)

    api_spec = api_specs.get(spec)
    if api_spec and api_spec.get("suffix"):
        return api_spec.get("suffix")

    return None


def update_spec(spec: str, subspec: str) -> str:
    if subspec:
        spec = spec + subspec
    return spec


def generate_typespec_project(
    tsp_project: str,
    sdk_root: str,
    spec_root: str = None,
    head_sha: str = "",
    repo_url: str = "",
    remove_before_regen: bool = False,
    group_id: str = None,
):

    if not tsp_project:
        return False

    succeeded = False
    sdk_folder = None
    service = None
    module = None
    require_sdk_integration = False

    try:
        url_match = re.match(
            r"^https://github.com/(?P<repo>[^/]*/azure-rest-api-specs(-pr)?)/blob/(?P<commit>[0-9a-f]{40})/(?P<path>.*)/tspconfig.yaml$",
            tsp_project,
            re.IGNORECASE,
        )

        tspconfig_valid = True
        if url_match:
            # generate from remote url
            tsp_cmd = [
                "npx" + (".cmd" if is_windows() else ""),
                "tsp-client",
                "init",
                "--debug",
                "--tsp-config",
                tsp_project,
            ]
        else:
            # sdk automation
            tsp_dir = os.path.join(spec_root, tsp_project) if spec_root else tsp_project
            tspconfig_valid = validate_tspconfig(tsp_dir)
            repo = remove_prefix(repo_url, "https://github.com/")
            tsp_cmd = [
                "npx" + (".cmd" if is_windows() else ""),
                "tsp-client",
                "init",
                "--debug",
                "--tsp-config",
                tsp_dir,
                "--commit",
                head_sha,
                "--repo",
                repo,
                "--local-spec-repo",
                tsp_dir,
            ]

        if tspconfig_valid:
            check_call(tsp_cmd, sdk_root)

            sdk_folder = find_sdk_folder(sdk_root)
            logging.info("SDK folder: " + sdk_folder)
            if sdk_folder:
                # parse service and module
                match = re.match(r"sdk[\\/](.*)[\\/](.*)", sdk_folder)
                service = match.group(1)
                module = match.group(2)
                # check require_sdk_integration
                cmd = ["git", "add", "."]
                check_call(cmd, sdk_root)
                cmd = [
                    "git",
                    "status",
                    "--porcelain",
                    os.path.join(sdk_folder, "pom.xml"),
                ]
                logging.info("Command line: " + " ".join(cmd))
                output = subprocess.check_output(cmd, cwd=sdk_root)
                output_str = str(output, "utf-8")
                git_items = output_str.splitlines()
                if len(git_items) > 0:
                    git_pom_item = git_items[0]
                    # new pom.xml implies new SDK
                    require_sdk_integration = git_pom_item.startswith("A ")
                if remove_before_regen and group_id:
                    # clear existing generated source code, and regenerate
                    drop_changes(sdk_root)
                    remove_generated_source_code(sdk_folder, f"{group_id}.{service}")
                    # regenerate
                    check_call(tsp_cmd, sdk_root)
                succeeded = True
    except subprocess.CalledProcessError as error:
        logging.error(f"[GENERATE] Code generation failed. tsp-client init fails: {error}")

    return succeeded, require_sdk_integration, sdk_folder, service, module


def check_call(cmd: List[str], work_dir: str, shell: bool = False):
    logging.info("Command line: " + " ".join(cmd))
    subprocess.check_call(cmd, cwd=work_dir, shell=shell)


def drop_changes(work_dir: str):
    check_call(["git", "checkout", "--", "."], work_dir)
    check_call(["git", "clean", "-qf", "."], work_dir)


def remove_prefix(text, prefix):
    if text.startswith(prefix):
        return text[len(prefix) :]
    return text


def find_sdk_folder(sdk_root: str):
    cmd = ["git", "add", "."]
    check_call(cmd, sdk_root)

    cmd = ["git", "status", "--porcelain", "**/tsp-location.yaml"]
    logging.info("Command line: " + " ".join(cmd))
    output = subprocess.check_output(cmd, cwd=sdk_root)
    output_str = str(output, "utf-8")
    git_items = output_str.splitlines()
    sdk_folder = None
    if len(git_items) > 0:
        tsp_location_item: str = git_items[0]
        sdk_folder = tsp_location_item[1:].strip()[0 : -len("/tsp-location.yaml")]

    cmd = ["git", "reset", ".", "-q"]
    check_call(cmd, sdk_root)

    return sdk_folder


def clean_sdk_folder_if_swagger(sdk_root: str, sdk_folder: str) -> bool:
    succeeded = False
    # try to find the sdk_folder
    if not sdk_folder:
        sdk_folder = find_sdk_folder(sdk_root)
    if sdk_folder:
        sdk_path = os.path.join(sdk_root, sdk_folder)
        # check whether this is migration from Swagger
        if os.path.exists(os.path.join(sdk_path, "swagger")):
            logging.info(f"[GENERATE] Delete folder: {sdk_folder}")
            print(
                "Existing package in SDK was from Swagger. It cannot be automatically converted to package from TypeSpec. Generate a fresh package from TypeSpec.",
                file=sys.stderr,
            )
            # delete the folder
            shutil.rmtree(sdk_path, ignore_errors=True)

            succeeded = True
    return succeeded
