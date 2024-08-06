#!/usr/bin/env python3
import os
import re
import sys
import json
import glob
import logging
import argparse
from typing import List

pwd = os.getcwd()
os.chdir(os.path.abspath(os.path.dirname(sys.argv[0])))
from parameters import *
from utils import (
    set_or_increase_version,
    update_service_files_for_new_lib,
    update_root_pom,
    update_version,
    get_latest_ga_version,
    get_latest_release_version,
)
from generate_data import (
    sdk_automation as sdk_automation_data,
    sdk_automation_typespec_project as sdk_automation_typespec_project_data,
)
from generate_utils import (
    compare_with_maven_package,
    compile_arm_package,
    generate,
    get_and_update_service_from_api_specs,
    get_suffix_from_api_specs,
    update_spec,
    generate_typespec_project,
)

os.chdir(pwd)


def update_parameters(suffix):
    # update changeable parameters in parameters.py
    global SUFFIX, NAMESPACE_SUFFIX, ARTIFACT_SUFFIX, NAMESPACE_FORMAT, ARTIFACT_FORMAT, OUTPUT_FOLDER_FORMAT

    SUFFIX = suffix

    NAMESPACE_SUFFIX = ".{0}".format(SUFFIX) if SUFFIX else ""
    ARTIFACT_SUFFIX = "-{0}".format(SUFFIX) if SUFFIX else ""
    NAMESPACE_FORMAT = "com.azure.resourcemanager.{{0}}{0}".format(NAMESPACE_SUFFIX)
    ARTIFACT_FORMAT = "azure-resourcemanager-{{0}}{0}".format(ARTIFACT_SUFFIX)
    OUTPUT_FOLDER_FORMAT = "sdk/{{0}}/{0}".format(ARTIFACT_FORMAT)


def parse_args() -> (argparse.ArgumentParser, argparse.Namespace):
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--spec-root",
        default="https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/",
        help="Spec root folder",
    )
    parser.add_argument(
        "-r",
        "--readme",
        help='Readme path, Sample: "storage" or "specification/storage/resource-manager/readme.md"',
    )
    parser.add_argument(
        "-c",
        "--tsp-config",
        help="The top level directory where the tspconfig.yaml for the service lives. "
        "Currently only support remote url with specific commitID "
        "e.g. https://github.com/Azure/azure-rest-api-specs/blob/042e4045dedff4baaf5ae551bf6c8087fbdacd40/specification/deviceregistry/DeviceRegistry.Management/tspconfig.yaml",
    )
    parser.add_argument("-t", "--tag", help="Specific tag")
    parser.add_argument("-v", "--version", help="Specific sdk version")
    parser.add_argument(
        "-s",
        "--service",
        help="Service Name if not the same as spec name",
    )
    parser.add_argument(
        "-u",
        "--use",
        default=AUTOREST_JAVA,
        help="Autorest java plugin",
    )
    parser.add_argument(
        "--autorest",
        default=AUTOREST_CORE_VERSION,
        help="Autorest version",
    )
    parser.add_argument(
        "--autorest-options",
        default="",
        help="Additional autorest options",
    )
    parser.add_argument("--suffix", help="Suffix for namespace and artifact")
    parser.add_argument(
        "--auto-commit-external-change",
        action="store_true",
        help="Automatic commit the generated code",
    )
    parser.add_argument("--user-name", help="User Name for commit")
    parser.add_argument("--user-email", help="User Email for commit")
    parser.add_argument(
        "config",
        nargs="*",
    )

    return (parser, parser.parse_args())


def sdk_automation(input_file: str, output_file: str):
    with open(input_file, "r") as fin:
        config = json.load(fin)

    # typespec
    packages = sdk_automation_typespec(config)
    # autorest
    if not packages:
        packages = sdk_automation_autorest(config)

    with open(output_file, "w", encoding="utf-8") as fout:
        output = {
            "packages": packages,
        }
        json.dump(output, fout)


def sdk_automation_autorest(config: dict) -> List[dict]:
    base_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
    sdk_root = os.path.abspath(os.path.join(base_dir, SDK_ROOT))
    api_specs_file = os.path.join(base_dir, API_SPECS_FILE)

    packages = []
    breaking = False
    if "relatedReadmeMdFiles" not in config or not config["relatedReadmeMdFiles"]:
        return packages

    for readme in config["relatedReadmeMdFiles"]:
        match = re.search(
            "specification/([^/]+)/resource-manager(/.*)*/readme.md",
            readme,
            re.IGNORECASE,
        )
        if not match:
            logging.info("[Skip] readme path does not format as specification/*/resource-manager/*/readme.md")
        else:
            spec = match.group(1)
            spec = update_spec(spec, match.group(2))
            service = get_and_update_service_from_api_specs(api_specs_file, spec)

            pre_suffix = SUFFIX
            suffix = get_suffix_from_api_specs(api_specs_file, spec)
            if suffix is None:
                suffix = SUFFIX
            update_parameters(suffix)

            # TODO: use specific function to detect tag in "resources"
            tag = None
            if service == "resources":
                with open(os.path.join(config["specFolder"], readme)) as fin:
                    tag_match = re.search(r"tag: (package-resources-\S+)", fin.read())
                    if tag_match:
                        tag = tag_match.group(1)
                    else:
                        tag = "package-resources-2021-01"

            module = ARTIFACT_FORMAT.format(service)
            output_folder = OUTPUT_FOLDER_FORMAT.format(service)
            namespace = NAMESPACE_FORMAT.format(service)
            stable_version, current_version = set_or_increase_version(sdk_root, GROUP_ID, module)
            succeeded = generate(
                sdk_root,
                service,
                spec_root=config["specFolder"],
                readme=readme,
                autorest=AUTOREST_CORE_VERSION,
                use=AUTOREST_JAVA,
                output_folder=output_folder,
                module=module,
                namespace=namespace,
                tag=tag,
            )
            if succeeded:
                compile_succeeded = compile_arm_package(sdk_root, module)
                if compile_succeeded:
                    stable_version = get_latest_ga_version(GROUP_ID, module, stable_version)
                    breaking, changelog = compare_with_maven_package(
                        sdk_root, GROUP_ID, service, stable_version, current_version, module
                    )

            packages.append(
                {
                    "packageName": "{0}".format(ARTIFACT_FORMAT.format(service)),
                    "path": [
                        output_folder,
                        CI_FILE_FORMAT.format(service),
                        POM_FILE_FORMAT.format(service),
                        "eng/versioning",
                        "pom.xml",
                    ],
                    "readmeMd": [readme],
                    "artifacts": ["{0}/pom.xml".format(output_folder)]
                    + [jar for jar in glob.glob("{0}/target/*.jar".format(output_folder))],
                    "apiViewArtifact": next(iter(glob.glob("{0}/target/*-sources.jar".format(output_folder))), None),
                    "language": "Java",
                    "result": "succeeded" if succeeded else "failed",
                    "changelog": {"content": changelog, "hasBreakingChange": breaking},
                }
            )

            update_parameters(pre_suffix)

    if not packages:
        # try data-plane codegen
        packages = sdk_automation_data(config)

    return packages


def sdk_automation_typespec(config: dict) -> List[dict]:

    packages = []
    if "relatedTypeSpecProjectFolder" not in config:
        return packages

    tsp_projects = config["relatedTypeSpecProjectFolder"]
    if isinstance(tsp_projects, str):
        tsp_projects = [tsp_projects]

    for tsp_project in tsp_projects:
        # mgmt tsp project folder follow the pattern, e.g. specification/deviceregistry/DeviceRegistry.Management
        if re.match(r"specification[\\/](.*)[\\/](.*)[\\.]Management", tsp_project):
            packages.append(sdk_automation_typespec_project(tsp_project, config))
        else:
            packages.append(sdk_automation_typespec_project_data(tsp_project, config))

    return packages


def sdk_automation_typespec_project(tsp_project: str, config: dict) -> dict:

    # TODO(xiaofei) support changelog, etc
    base_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
    sdk_root = os.path.abspath(os.path.join(base_dir, SDK_ROOT))
    spec_root = os.path.abspath(config["specFolder"])
    head_sha: str = config["headSha"]
    repo_url: str = config["repoHttpsUrl"]
    breaking: bool = False

    succeeded, require_sdk_integration, sdk_folder, service, module = generate_typespec_project(
        tsp_project, sdk_root, spec_root, head_sha, repo_url, remove_before_regen=True, group_id=GROUP_ID
    )

    if succeeded:
        # TODO (weidxu): move to typespec-java
        if require_sdk_integration:
            update_service_files_for_new_lib(sdk_root, service, GROUP_ID, module)
            update_root_pom(sdk_root, service)

        stable_version, current_version = set_or_increase_version(sdk_root, GROUP_ID, module)
        update_parameters(None)
        output_folder = OUTPUT_FOLDER_FORMAT.format(service)
        update_version(sdk_root, output_folder)

        # compile
        succeeded = compile_arm_package(sdk_root, module)
        if succeeded:
            breaking, changelog = compare_with_maven_package(
                sdk_root,
                GROUP_ID,
                service,
                get_latest_ga_version(GROUP_ID, module, stable_version),
                current_version,
                module,
            )

    # output
    if sdk_folder and module and service:
        artifacts = ["{0}/pom.xml".format(sdk_folder)]
        artifacts += [jar for jar in glob.glob("{0}/target/*.jar".format(sdk_folder))]
        result = "succeeded" if succeeded else "failed"

        return {
            "packageName": module,
            "path": [
                sdk_folder,
                CI_FILE_FORMAT.format(service),
                POM_FILE_FORMAT.format(service),
                "eng/versioning",
                "pom.xml",
            ],
            "typespecProject": [tsp_project],
            "packageFolder": sdk_folder,
            "artifacts": artifacts,
            "apiViewArtifact": next(iter(glob.glob("{0}/target/*-sources.jar".format(sdk_folder))), None),
            "language": "Java",
            "result": result,
            "changelog": {"content": changelog, "hasBreakingChange": breaking},
        }
    else:
        # no info about package, abort with result=failed
        return {
            "path": [],
            "result": "failed",
        }


def update_changelog_version(sdk_root: str, output_folder: str, current_version: str):
    pwd = os.getcwd()
    try:
        os.chdir(sdk_root)
        changelog_file = os.path.join(output_folder, "CHANGELOG.md")
        if os.path.exists(changelog_file):
            with open(changelog_file, "r") as fin:
                changelog_str = fin.read()
            logging.info("[CHANGELOG][Version] Update changelog latest version")
            version_pattern = r"^## (\d+\.\d+\.\d+(?:-[\w\d\.]+)?) \((?P<date>.*?)\)"

            changelog_str = re.sub(
                pattern=version_pattern,
                repl=f"## {current_version} (\\g<date>)",
                string=changelog_str,
                count=1,
                flags=re.M,
            )
            with open(changelog_file, "w") as fout:
                fout.write(changelog_str)

            logging.info("[Changelog][Success] Updated changelog latest version")
        else:
            logging.info("[Changelog][Skip] Cannot find changelog file under the given output folder")
    finally:
        os.chdir(pwd)


def main():
    (parser, args) = parse_args()
    args = vars(args)

    if args.get("config"):
        return sdk_automation(args["config"][0], args["config"][1])

    base_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
    sdk_root = os.path.abspath(os.path.join(base_dir, SDK_ROOT))
    api_specs_file = os.path.join(base_dir, API_SPECS_FILE)

    if args.get("tsp_config"):
        tsp_config = args["tsp_config"]

        succeeded, require_sdk_integration, sdk_folder, service, module = generate_typespec_project(
            tsp_project=tsp_config, sdk_root=sdk_root, remove_before_regen=True, group_id=GROUP_ID
        )

        stable_version, current_version = set_or_increase_version(sdk_root, GROUP_ID, module, **args)
        args["version"] = current_version

        if require_sdk_integration:
            update_service_files_for_new_lib(sdk_root, service, GROUP_ID, module)
            update_root_pom(sdk_root, service)

        update_parameters(None)
        output_folder = OUTPUT_FOLDER_FORMAT.format(service)
        update_version(sdk_root, output_folder)
        update_changelog_version(sdk_root, output_folder, current_version)
    else:
        if not args.get("readme"):
            parser.print_help()
            sys.exit(0)

        readme = args["readme"]
        match = re.match(
            "specification/([^/]+)/resource-manager(/.*)*/readme.md",
            readme,
            re.IGNORECASE,
        )
        if not match:
            spec = readme
            readme = "specification/{0}/resource-manager/readme.md".format(spec)
        else:
            spec = match.group(1)
            spec = update_spec(spec, match.group(2))

        args["readme"] = readme
        args["spec"] = spec

        update_parameters(args.get("suffix") or get_suffix_from_api_specs(api_specs_file, spec))
        service = get_and_update_service_from_api_specs(api_specs_file, spec, args["service"])
        args["service"] = service
        module = ARTIFACT_FORMAT.format(service)
        stable_version, current_version = set_or_increase_version(sdk_root, GROUP_ID, module, **args)
        args["version"] = current_version
        output_folder = OUTPUT_FOLDER_FORMAT.format(service)
        namespace = NAMESPACE_FORMAT.format(service)
        succeeded = generate(sdk_root, module=module, output_folder=output_folder, namespace=namespace, **args)

    if succeeded:
        succeeded = compile_arm_package(sdk_root, module)
        if succeeded:
            latest_release_version = get_latest_release_version(stable_version, current_version)
            compare_with_maven_package(sdk_root, GROUP_ID, service, latest_release_version, current_version, module)

            if args.get("auto_commit_external_change") and args.get("user_name") and args.get("user_email"):
                pwd = os.getcwd()
                try:
                    os.chdir(sdk_root)
                    os.system(
                        "git add eng/versioning eng/automation pom.xml {0} {1}".format(
                            CI_FILE_FORMAT.format(service), POM_FILE_FORMAT.format(service)
                        )
                    )
                    os.system(
                        'git -c user.name={0} -c user.email={1} commit -m "[Automation] External Change"'.format(
                            args["user_name"], args["user_email"]
                        )
                    )
                finally:
                    os.chdir(pwd)

    if not succeeded:
        raise RuntimeError("Failed to generate code or compile the package")


if __name__ == "__main__":
    logging.basicConfig(
        stream=sys.stdout,
        level=logging.INFO,
        format="%(asctime)s [%(levelname)s] %(message)s",
        datefmt="%Y-%m-%d %X",
    )
    main()
