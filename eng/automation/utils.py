import os
import sys
import yaml
import logging
import re
import subprocess
import platform
import requests
from functools import cmp_to_key
from typing import Tuple

from parameters import CI_HEADER
from parameters import CI_FORMAT
from parameters import POM_FORMAT
from parameters import POM_MODULE_FORMAT
from parameters import DEFAULT_VERSION
from parameters import CHANGELOG_INITIAL_SECTION_FORMAT
from parameters import MAVEN_HOST


# Add two more indent for list in yaml dump
class ListIndentDumper(yaml.SafeDumper):

    def increase_indent(self, flow=False, indentless=False):
        return super(ListIndentDumper, self).increase_indent(flow, False)


def add_module_to_modules(modules: str, module: str) -> str:
    post_module = re.search(r"([^\S\n\r]*)</modules>", modules)
    indent = post_module.group(1)
    indent += "  "

    all_module = set(re.findall(r"<module>(.*?)</module>", modules))
    all_module.add(module)
    all_module = [indent + POM_MODULE_FORMAT.format(module) for module in sorted(all_module)]

    return "<modules>\n" + "".join(all_module) + post_module.group()


def add_module_to_default_profile(pom: str, module: str) -> Tuple[bool, str]:
    for profile in re.finditer(r"<profile>[\s\S]*?</profile>", pom):
        profile_value = profile.group()
        if re.search(r"<id>default</id>", profile_value):
            if len(re.findall("<modules>", profile_value)) > 1:
                logging.error("[POM][Profile][Skip] find more than one <modules> in <profile> default")
                return (False, "")
            modules = re.search(r"<modules>[\s\S]*</modules>", profile_value)
            if not modules:
                logging.error("[POM][Profile][Skip] Cannot find <modules> in <profile> default")
                return (False, "")
            modules_update = add_module_to_modules(modules.group(), module)
            pre_modules = pom[: profile.start() + modules.start()]
            post_modules = pom[profile.start() + modules.end() :]
            return (True, pre_modules + modules_update + post_modules)
    logging.error("[POM][Profile][Skip] cannot find <profile> with <id> default")
    return (False, "")


def add_module_to_pom(pom: str, module: str) -> Tuple[bool, str]:
    if pom.find("<module>{0}</module>".format(module)) >= 0:
        logging.info("[POM][Skip] pom already has module {0}".format(module))
        return (True, pom)

    if len(re.findall("<modules>", pom)) > 1:
        if pom.find("<profiles>") >= 0:
            return add_module_to_default_profile(pom, module)
        logging.error("[POM][Skip] find more than one <modules> in pom")
        return (False, "")

    modules = re.search(r"<modules>[\s\S]*</modules>", pom)
    if not modules:
        logging.error("[POM][Skip] Cannot find <modules> in pom")
        return (False, "")

    modules_update = add_module_to_modules(modules.group(), module)
    pre_modules = pom[: modules.start()]
    post_modules = pom[modules.end() :]
    return (True, pre_modules + modules_update + post_modules)


def update_root_pom(sdk_root: str, service: str):
    pom_file = os.path.join(sdk_root, "pom.xml")
    if not os.path.exists(pom_file):
        logging.error("[POM][Skip] cannot find root pom")
        return

    module = "sdk/{0}".format(service)
    with open(pom_file, "r") as fin:
        pom = fin.read()

    logging.info("[POM][Process] dealing with root pom")
    success, pom = add_module_to_pom(pom, module)
    if success:
        with open(pom_file, "w") as fout:
            fout.write(pom)
        logging.info("[POM][Success] Write to root pom")


def update_service_files_for_new_lib(sdk_root: str, service: str, group: str, module: str):
    """
    For new lib, update ci.yml, pom.xml and changelog.md accordingly.
    """
    folder = os.path.join(sdk_root, "sdk/{0}".format(service))
    ci_yml_file = os.path.join(folder, "ci.yml")
    pom_xml_file = os.path.join(folder, "pom.xml")
    module_folder = os.path.join(folder, module)
    changelog_file = os.path.join(module_folder, "CHANGELOG.md")

    if os.path.exists(ci_yml_file):
        with open(ci_yml_file, "r") as fin:
            ci_yml = yaml.safe_load(fin)
        sdk_type: str = ci_yml.get("extends", dict()).get("parameters", dict()).get("SDKType", "")
        if type(sdk_type) == str and sdk_type.lower() == "data":
            os.rename(ci_yml_file, os.path.join(os.path.dirname(ci_yml_file), "ci.data.yml"))
            ci_yml = yaml.safe_load(CI_FORMAT.format(service, module))
    else:
        ci_yml = yaml.safe_load(CI_FORMAT.format(service, module))

    if not (
        type(ci_yml.get("extends")) == dict
        and type(ci_yml["extends"].get("parameters")) == dict
        and type(ci_yml["extends"]["parameters"].get("Artifacts")) == list
    ):
        logging.error("[CI][Skip] Unexpected ci.yml format")
    else:
        artifacts: list = ci_yml["extends"]["parameters"]["Artifacts"]
        for artifact in artifacts:
            if artifact.get("name") == module and artifact.get("groupId") == group:
                logging.info("[CI][Skip] ci.yml already has module {0}".format(module))
                break
        else:
            safe_name = module.replace("-", "")
            if "parameters" not in ci_yml or not (type(ci_yml.get("parameters")) == list):
                # add artifact without "release_" parameter

                artifacts.append({"name": module, "groupId": group, "safeName": safe_name})
            else:
                # add artifact with "release_" parameter

                release_parameter_name = "release_{}".format(safe_name)
                release_in_batch_parameter_ref = "${{{{ parameters.{} }}}}".format(release_parameter_name)
                artifacts.append(
                    {
                        "name": module,
                        "groupId": group,
                        "safeName": safe_name,
                        "releaseInBatch": release_in_batch_parameter_ref,
                    }
                )

                if "parameters" not in ci_yml:
                    ci_yml["parameters"] = []

                # True for data-plane, False for management-plane
                release_in_batch_default = "-resourcemanager-" not in module
                parameters: list = ci_yml["parameters"]
                parameters.append(
                    {
                        "name": release_parameter_name,
                        "displayName": module,
                        "type": "boolean",
                        "default": release_in_batch_default,
                    }
                )

            ci_yml_str = yaml.dump(ci_yml, width=sys.maxsize, sort_keys=False, Dumper=ListIndentDumper)
            ci_yml_str = re.sub("(\n\S)", r"\n\1", ci_yml_str)

            with open(ci_yml_file, "w") as fout:
                fout.write(CI_HEADER)
                fout.write(ci_yml_str)
            logging.info("[CI][Success] Write to ci.yml")

    if os.path.exists(pom_xml_file):
        with open(pom_xml_file, "r") as fin:
            pom_xml = fin.read()
    else:
        pom_xml = POM_FORMAT.format(service=service, group_id=group, artifact_id=module)

    logging.info("[POM][Process] dealing with pom.xml")
    success, pom_xml = add_module_to_pom(pom_xml, module)
    if success:
        with open(pom_xml_file, "w") as fout:
            fout.write(pom_xml)
        logging.info("[POM][Success] Write to pom.xml")

    if os.path.exists(changelog_file):
        with open(changelog_file, "r") as fin:
            changelog_str = fin.read()
    logging.info("[CHANGELOG][Process] Adding initial section to changelog.md.")
    initial_section = CHANGELOG_INITIAL_SECTION_FORMAT.format(artifact_id=module)
    if initial_section not in changelog_str:
        changelog_str += initial_section
        with open(changelog_file, "w") as fout:
            fout.write(changelog_str)
        logging.info("[CHANGLOG][Success] Write to changelog.md.")
    else:
        logging.warning("[CHANGELOG] changelog.md already contains necessary sections. Will not add initial sections.")


def update_version(sdk_root: str, output_folder: str):
    # find the python command
    python_cmd = "python"
    try:
        subprocess.check_call([python_cmd, "--version"])
    except subprocess.CalledProcessError:
        python_cmd = "python3"
        try:
            subprocess.check_call([python_cmd, "--version"])
        except subprocess.CalledProcessError:
            raise Exception("python or python3 not found")

    pwd = os.getcwd()
    try:
        os.chdir(sdk_root)
        print(os.getcwd())
        subprocess.run(
            [python_cmd, "eng/versioning/update_versions.py", "--ut", "library", "--bt", "client", "--sr"],
            stdout=subprocess.DEVNULL,
            stderr=sys.stderr,
        )
        subprocess.run(
            [
                python_cmd,
                "eng/versioning/update_versions.py",
                "--ut",
                "library",
                "--bt",
                "client",
                "--tf",
                "{0}/README.md".format(output_folder),
            ],
            stdout=subprocess.DEVNULL,
            stderr=sys.stderr,
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
    lines[index] = "{0};{1};{2}".format(project, stable_version, current_version)
    with open(version_file, "w") as fout:
        fout.write("\n".join(lines))
        fout.write("\n")


def set_or_default_version(
    sdk_root: str,
    group: str,
    module: str,
) -> Tuple[str, str]:
    version_file = os.path.join(sdk_root, "eng/versioning/version_client.txt")
    project = "{0}:{1}".format(group, module)
    default_version = DEFAULT_VERSION

    with open(version_file, "r") as fin:
        lines = fin.read().splitlines()
        version_index = -1
        for i, version_line in enumerate(lines):
            version_line = version_line.strip()
            if version_line.startswith("#"):
                continue
            versions = version_line.split(";")
            if versions[0] == project:
                if len(versions) != 3:
                    logging.error('[VERSION][Fallback] Unexpected version format "{0}"'.format(version_line))
                    stable_version = ""
                    current_version = default_version
                else:
                    logging.info('[VERSION][Found] current version "{0}"'.format(version_line))
                    stable_version = versions[1]
                    current_version = versions[2]
                version_index = i
                break
        else:
            logging.info('[VERSION][Not Found] cannot find version for "{0}"'.format(project))
            for i, version_line in enumerate(lines):
                if version_line.startswith("{0}:".format(group)):
                    version_index = i + 1
            lines = lines[:version_index] + [""] + lines[version_index:]
            stable_version = ""
            current_version = default_version

    if not stable_version:
        stable_version = current_version

    write_version(version_file, lines, version_index, project, stable_version, current_version)

    return stable_version, current_version


def set_or_increase_version(
    sdk_root: str,
    group: str,
    module: str,
    preview=True,
    version=None,
    **kwargs,
) -> Tuple[str, str]:
    version_file = os.path.join(sdk_root, "eng/versioning/version_client.txt")
    project = "{0}:{1}".format(group, module)
    version_pattern = "(\d+)\.(\d+)\.(\d+)(-beta\.\d+)?"
    version_format = "{0}.{1}.{2}{3}"
    default_version = version if version else DEFAULT_VERSION

    with open(version_file, "r") as fin:
        lines = fin.read().splitlines()
        version_index = -1
        for i, version_line in enumerate(lines):
            version_line = version_line.strip()
            if version_line.startswith("#"):
                continue
            versions = version_line.split(";")
            if versions[0] == project:
                if len(versions) != 3:
                    logging.error('[VERSION][Fallback] Unexpected version format "{0}"'.format(version_line))
                    stable_version = ""
                    current_version = default_version
                else:
                    logging.info('[VERSION][Found] current version "{0}"'.format(version_line))
                    stable_version = versions[1]
                    current_version = versions[2]
                version_index = i
                break
        else:
            logging.info('[VERSION][Not Found] cannot find version for "{0}"'.format(project))
            for i, version_line in enumerate(lines):
                if version_line.startswith("{0}:".format(group)):
                    version_index = i + 1
            lines = lines[:version_index] + [""] + lines[version_index:]
            stable_version = ""
            current_version = default_version

    # version is given, set and return
    if version:
        if not stable_version:
            stable_version = version
        logging.info('[VERSION][Set] set to given version "{0}"'.format(version))
        write_version(version_file, lines, version_index, project, stable_version, version)
        return stable_version, version

    current_versions = list(re.findall(version_pattern, current_version)[0])
    stable_versions = re.findall(version_pattern, stable_version)
    # no stable version
    if len(stable_versions) < 1 or stable_versions[0][-1] != "":
        if not preview:
            current_versions[-1] = ""
        current_version = version_format.format(*current_versions)
        if not stable_version:
            stable_version = current_version
        logging.info('[VERSION][Not Found] cannot find stable version, current version "{0}"'.format(current_version))

        write_version(version_file, lines, version_index, project, stable_version, current_version)
    else:
        # TODO: auto-increase for stable version and beta version if possible
        current_version = version_format.format(*current_versions)
        if not stable_version:
            stable_version = current_version
        logging.warning('[VERSION][Not Implement] set to current version "{0}" by default'.format(current_version))

        write_version(version_file, lines, version_index, project, stable_version, current_version)

    return stable_version, current_version


def is_windows():
    return platform.system().lower() == "windows"


def get_latest_release_version(previous_version: str, current_version: str) -> str:
    if "-beta." in current_version and "-beta." not in previous_version:
        # if current version is preview, try compare it with a previous preview release

        version_pattern = r"\d+\.\d+\.\d+-beta\.(\d+)?"
        beta_version_int = int(re.match(version_pattern, current_version).group(1))
        if beta_version_int > 1:
            previous_beta_version_int = beta_version_int - 1
            previous_beta_version = current_version.replace(
                "-beta." + str(beta_version_int),
                "-beta." + str(previous_beta_version_int),
            )
            previous_version = previous_beta_version
    return previous_version


def get_latest_ga_version(group_id: str, module: str, previous_version: str) -> str:
    """
    If previous_version is GA, return previous_version,
    Otherwise, return previous GA version before previous beta.
    None if no previous GA version.
    """
    if "-beta." not in previous_version:
        return previous_version

    # No previous GA version
    if previous_version.startswith("1.0.0-beta."):
        return None

    group_path = group_id.replace(".", "/")

    response = requests.get(f"{MAVEN_HOST}/{group_path}/{module}")

    response.raise_for_status()

    ga_version_pattern = r"<a href=\"(\d+\.\d+\.\d+)\/?"
    ga_versions = [v.group(1) for v in re.finditer(ga_version_pattern, response.text, re.S)]
    previous_ga_versions = sorted(
        [v for v in ga_versions if compare_version(v, previous_version) < 0],
        key=cmp_to_key(compare_version),
        reverse=True,
    )

    if len(previous_ga_versions) > 0:
        return previous_ga_versions[0]

    return None


def compare_version(v1: str, v2: str) -> int:
    """
    If v1 < v2, return -1;
    Else if v1 > v2, return 1;
    Else return 0;
    """
    ga_version_pattern = r"(\d+)\.(\d)+\.(\d+)?"
    v1_major_version = int(re.match(ga_version_pattern, v1).group(1))
    v1_minor_version = int(re.match(ga_version_pattern, v1).group(2))
    v1_patch_version = int(re.match(ga_version_pattern, v1).group(3))

    v2_major_version = int(re.match(ga_version_pattern, v2).group(1))
    v2_minor_version = int(re.match(ga_version_pattern, v2).group(2))
    v2_patch_version = int(re.match(ga_version_pattern, v2).group(3))

    if v1_major_version < v2_major_version:
        return -1
    if v1_major_version > v2_major_version:
        return 1
    if v1_minor_version < v2_minor_version:
        return -1
    if v1_minor_version > v2_minor_version:
        return 1
    if v1_patch_version < v2_patch_version:
        return -1
    if v1_patch_version > v2_patch_version:
        return 1
    return 0
