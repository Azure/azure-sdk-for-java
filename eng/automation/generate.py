#!/usr/bin/env python3
import os
import re
import sys
import json
import glob
import logging
import argparse
import shutil
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
    is_mgmt_premium,
    copy_folder_recursive_sync,
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
    # this function is for SDK automation from CI in specs or "spec-gen-sdk - java" pipeline

    with open(input_file, "r") as fin:
        config = json.load(fin)

    packages = []
    try:
        # typespec
        packages = sdk_automation_typespec(config)
        # autorest
        if not packages:
            packages = sdk_automation_autorest(config)
    except ValueError:
        logging.error("[VALIDATION] Parameter validation failed.", exc_info=True)
        sys.exit(1)
    except Exception:
        logging.error("[GENERATE] Code generation failed. Unknown exception", exc_info=True)
        if packages and len(packages) == 1:
            packages[0]["result"] = "failed"
        sys.exit(1)

    with open(output_file, "w", encoding="utf-8") as fout:
        output = {
            "packages": packages,
        }
        json.dump(output, fout)

    if packages and len(packages) == 1 and packages[0]["result"] == "failed":
        sys.exit(1)


def sdk_automation_autorest(config: dict) -> List[dict]:
    base_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
    sdk_root = os.path.abspath(os.path.join(base_dir, SDK_ROOT))
    api_specs_file = os.path.join(base_dir, API_SPECS_FILE)

    packages = []
    breaking = False
    changelog = ""
    breaking_change_items = []
    if "relatedReadmeMdFiles" not in config or not config["relatedReadmeMdFiles"]:
        return packages

    for readme in config["relatedReadmeMdFiles"]:
        match = re.search(
            r"specification/([^/]+)/resource-manager((?:/[^/]+)*)/readme.md",
            readme,
            re.IGNORECASE,
        )
        if not match:
            logging.info("[Skip] readme path does not format as specification/*/resource-manager/*/readme.md")
        else:
            spec = match.group(1)
            spec = update_spec(spec, match.group(2))
            service = get_and_update_service_from_api_specs(api_specs_file, spec, truncate_service=True)

            pre_suffix = SUFFIX
            suffix = get_suffix_from_api_specs(api_specs_file, spec)
            if suffix is None:
                suffix = SUFFIX
            update_parameters(suffix)

            # TODO: use specific function to detect tag in "resources" spec/service
            tag = None
            if service == "resources" and spec == service:
                with open(os.path.join(config["specFolder"], readme)) as fin:
                    tag_match = re.search(r"tag: (package-resources-\S+)", fin.read())
                    if tag_match:
                        tag = tag_match.group(1)
                    else:
                        tag = "package-resources-2025-04"

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
                premium=is_mgmt_premium(module),
            )
            if succeeded:
                succeeded = compile_arm_package(sdk_root, module)
                if succeeded:
                    stable_version = get_latest_ga_version(GROUP_ID, module, stable_version)
                    breaking, changelog, breaking_change_items = compare_with_maven_package(
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
                    "artifacts": (
                        ["{0}/pom.xml".format(output_folder)]
                        + [jar for jar in glob.glob("{0}/target/*.jar".format(output_folder))]
                        if succeeded
                        else []
                    ),
                    "apiViewArtifact": next(iter(glob.glob("{0}/target/*-sources.jar".format(output_folder))), None),
                    "language": "Java",
                    "result": "succeeded" if succeeded else "failed",
                    "changelog": {
                        "content": changelog,
                        "hasBreakingChange": breaking,
                        "breakingChangeItems": breaking_change_items,
                    },
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
        # folder structure v2: specification/{service}/[data-plane|resource-manager]/{provider}/
        folder_structure_v2_pattern = r"specification/.*/(data-plane|resource-manager)"
        match = re.compile(folder_structure_v2_pattern).search(tsp_project)
        if match:
            sdk_type = match.group(1)
            if sdk_type == "data-plane":
                logging.info("[GENERATE] Generating data-plane from folder structure v2: " + tsp_project)
                packages.append(sdk_automation_typespec_project_data(tsp_project, config))
            elif sdk_type == "resource-manager":
                logging.info("[GENERATE] Generating mgmt-plane from folder structure v2: " + tsp_project)
                packages.append(sdk_automation_typespec_project(tsp_project, config))
            else:
                raise ValueError("Unexpected sdk type: " + sdk_type)
        # folder structure v1
        # mgmt tsp project folder follow the pattern, e.g. specification/deviceregistry/DeviceRegistry.Management
        elif re.match(r"specification[\\/](.*)[\\/](.*)[\\.]Management", tsp_project):
            packages.append(sdk_automation_typespec_project(tsp_project, config))
        else:
            packages.append(sdk_automation_typespec_project_data(tsp_project, config))

    return packages


def verify_self_serve_parameters(api_version, sdk_release_type):
    if sdk_release_type and sdk_release_type not in ["stable", "beta"]:
        raise ValueError(f"Invalid SDK release type [{sdk_release_type}], only support 'stable' or 'beta'.")
    if api_version and sdk_release_type:
        if api_version.endswith("-preview") and sdk_release_type == "stable":
            raise ValueError(f"SDK release type is [stable], but API version [{api_version}] is preview.")
        logging.info(f"[SelfServe] Generate with apiVersion: {api_version} and sdkReleaseType: {sdk_release_type}")
    elif api_version or sdk_release_type:
        raise ValueError(
            "Both [API version] and [SDK release type] parameters are required for self-serve SDK generation."
        )


def sdk_automation_typespec_project(tsp_project: str, config: dict) -> dict:

    base_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
    sdk_root = os.path.abspath(os.path.join(base_dir, SDK_ROOT))
    spec_root = os.path.abspath(config["specFolder"])
    head_sha: str = config["headSha"]
    repo_url: str = config["repoHttpsUrl"]
    sdk_release_type: str = config["sdkReleaseType"] if "sdkReleaseType" in config else None
    api_version = config["apiVersion"] if "apiVersion" in config else None
    release_beta_sdk: bool = not sdk_release_type or sdk_release_type == "beta"
    breaking: bool = False
    changelog = ""
    breaking_change_items = []
    run_mode: str = config["runMode"] if "runMode" in config else None

    if run_mode == "release" or run_mode == "local":
        verify_self_serve_parameters(api_version, sdk_release_type)

    succeeded, require_sdk_integration, sdk_folder, service, module = generate_typespec_project(
        tsp_project,
        sdk_root,
        spec_root,
        head_sha,
        repo_url,
        remove_before_regen=True,
        group_id=GROUP_ID,
        api_version=api_version,
        generate_beta_sdk=release_beta_sdk,
    )

    if succeeded:
        # TODO (weidxu): move to typespec-java
        if require_sdk_integration:
            update_service_files_for_new_lib(sdk_root, service, GROUP_ID, module)
            update_root_pom(sdk_root, service)

        # get the stable version and current version from version_client.txt, current version in version_client will be updated if the release type is GA. 
        # e.g. If current version is 1.2.0-beta.1 and the release type is GA, then current version will be updated to 1.2.0
        stable_version, current_version = set_or_increase_version(sdk_root, GROUP_ID, module, preview=release_beta_sdk)
        update_parameters(None)
        output_folder = OUTPUT_FOLDER_FORMAT.format(service)
        update_version(sdk_root, output_folder)

        # compile
        succeeded = compile_arm_package(sdk_root, module)
        if succeeded:
            if is_mgmt_premium(module):
                move_premium_samples(sdk_root, service, module)
                update_azure_resourcemanager_pom(sdk_root, service, module)
            # For output breaking changes, useful in sdk validation pipeline
            logging.info("[Changelog] Start breaking change detection for SDK automation.")
            breaking, changelog, breaking_change_items = compare_with_maven_package(
                sdk_root,
                GROUP_ID,
                service,
                get_latest_ga_version(GROUP_ID, module, stable_version),
                current_version,
                module,
            )
            logging.info("[Changelog] Complete breaking change detection for SDK automation.")
            # For changelog content update
            logging.info("[Changelog] Start generating changelog.")
            compare_with_maven_package(
                sdk_root,
                GROUP_ID,
                service,
                get_latest_release_version(stable_version, current_version),
                current_version,
                module,
            )
            update_changelog_version(sdk_root, output_folder, current_version)
            logging.info("[Changelog] Complete generating changelog.")

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
            "artifacts": artifacts if succeeded else [],
            "apiViewArtifact": next(iter(glob.glob("{0}/target/*-sources.jar".format(sdk_folder))), None),
            "language": "Java",
            "result": result,
            "changelog": {
                "content": changelog,
                "hasBreakingChange": breaking,
                "breakingChangeItems": breaking_change_items,
            },
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


def move_premium_samples(sdk_root: str, service: str, module: str):
    package_path = "com/" + module.replace("-", "/")
    source_sample_dir = os.path.join(
        sdk_root, "sdk", service, module, "src", "samples", "java", package_path, "generated"
    )
    target_sample_dir = os.path.join(
        sdk_root, "sdk", "resourcemanager", "azure-resourcemanager", "src", "samples", "java", package_path
    )
    logging.info(f"Moving samples from {source_sample_dir} to {target_sample_dir}.")
    copy_folder_recursive_sync(source_sample_dir, target_sample_dir)
    shutil.rmtree(source_sample_dir, ignore_errors=True)


def update_azure_resourcemanager_pom(sdk_root: str, module: str, current_version: str):
    """
    Updates azure-resourcemanager pom for premium package split:
    1. Add unreleased entry in eng/versioning/version_client.txt
    2. Update dependency in azure-resourcemanager/pom.xml to use unreleased dependency
    """
    # 1. Add unreleased entry to version_client.txt
    version_file = os.path.join(sdk_root, "eng/versioning/version_client.txt")
    group_id = "com.azure.resourcemanager"
    project = "{0}:{1}".format(group_id, module)

    # Check if unreleased entry already exists
    unreleased_project = "unreleased_{0}".format(project)
    unreleased_exists = False
    with open(version_file, "r", encoding="utf-8") as fin:
        content = fin.read()
        if unreleased_project in content:
            unreleased_exists = True
            logging.info("[UNRELEASED][Skip] Unreleased entry already exists for %s", module)

    if not unreleased_exists:
        # Find the unreleased section and add the entry
        with open(version_file, "r", encoding="utf-8") as fin:
            lines = fin.read().splitlines()

        # Find the unreleased section start
        unreleased_section_start = -1
        for i, line in enumerate(lines):
            if "# Unreleased dependencies:" in line:
                unreleased_section_start = i
                break

        if unreleased_section_start == -1:
            logging.error("[UNRELEASED][Skip] Cannot find unreleased section in version_client.txt")
            return

        # Determine insertion point: append to the end of the unreleased section
        # by locating the first blank line after the last 'unreleased_' entry.
        last_unreleased_idx = -1
        end_of_section_idx = -1
        seen_unreleased = False
        for i in range(unreleased_section_start + 1, len(lines)):
            line = lines[i]
            if line.startswith("unreleased_"):
                seen_unreleased = True
                last_unreleased_idx = i
                continue
            if seen_unreleased:
                # First blank line after we started seeing unreleased entries marks end of section
                if line.strip() == "":
                    end_of_section_idx = i  # insert before this blank line
                    break
                # Or a new header line also marks the end
                if line.startswith("# "):
                    end_of_section_idx = i
                    break

        if last_unreleased_idx != -1:
            insert_index = end_of_section_idx if end_of_section_idx != -1 else last_unreleased_idx + 1
        else:
            # No existing unreleased entries, insert after header comments and optional blank line
            insert_index = unreleased_section_start + 1
            while insert_index < len(lines) and lines[insert_index].startswith("#"):
                insert_index += 1
            if insert_index < len(lines) and lines[insert_index].strip() == "":
                insert_index += 1

        # Insert the unreleased entry
        unreleased_entry = "unreleased_{0};{1}".format(project, current_version)
        lines.insert(insert_index, unreleased_entry)

        with open(version_file, "w", encoding="utf-8") as fout:
            fout.write("\n".join(lines))
            fout.write("\n")

        logging.info("[UNRELEASED][Success] Added unreleased entry: %s", unreleased_entry)

    # 2. Update azure-resourcemanager pom.xml
    pom_file = os.path.join(sdk_root, "sdk/resourcemanager/azure-resourcemanager/pom.xml")
    if not os.path.exists(pom_file):
        logging.error("[POM][Skip] Cannot find azure-resourcemanager pom.xml")
        return

    with open(pom_file, "r", encoding="utf-8") as fin:
        pom_content = fin.read()

    # Pattern to find the dependency and its version comment
    dependency_pattern = r"(<groupId>{0}</groupId>\s*<artifactId>{1}</artifactId>\s*<version>)[^<]+(</version>\s*<!-- {{x-version-update;){2}(;dependency}} -->)".format(
        re.escape(group_id), re.escape(module), re.escape(project)
    )

    # Replace current with unreleased dependency
    replacement = r"\g<1>" + current_version + r"\g<2>unreleased_" + project + r"\g<3>"
    updated_pom_content = re.sub(dependency_pattern, replacement, pom_content, flags=re.DOTALL)

    if updated_pom_content != pom_content:
        with open(pom_file, "w", encoding="utf-8") as fout:
            fout.write(updated_pom_content)
        logging.info("[POM][Success] Updated azure-resourcemanager pom.xml to use unreleased dependency of %s", module)
    else:
        logging.warning("[POM][Skip] Could not find dependency for %s in azure-resourcemanager pom.xml", module)


def main():
    (parser, args) = parse_args()
    args = vars(args)

    if args.get("config"):
        return sdk_automation(args["config"][0], args["config"][1])

    base_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
    sdk_root = os.path.abspath(os.path.join(base_dir, SDK_ROOT))
    api_specs_file = os.path.join(base_dir, API_SPECS_FILE)
    premium = False

    if args.get("tsp_config"):
        tsp_config = args["tsp_config"]

        succeeded, require_sdk_integration, sdk_folder, service, module = generate_typespec_project(
            tsp_project=tsp_config, sdk_root=sdk_root, remove_before_regen=True, group_id=GROUP_ID, **args
        )

        premium = is_mgmt_premium(module)

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
            r"specification/([^/]+)/resource-manager((?:/[^/]+)*)/readme.md",
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

        suffix = args.get("suffix") or get_suffix_from_api_specs(api_specs_file, spec)
        update_parameters(suffix)
        service = get_and_update_service_from_api_specs(api_specs_file, spec, args["service"], suffix)
        args["service"] = service
        module = ARTIFACT_FORMAT.format(service)
        premium = is_mgmt_premium(module)
        stable_version, current_version = set_or_increase_version(sdk_root, GROUP_ID, module, **args)
        args["version"] = current_version
        output_folder = OUTPUT_FOLDER_FORMAT.format(service)
        namespace = NAMESPACE_FORMAT.format(service)
        succeeded = generate(
            sdk_root, module=module, output_folder=output_folder, namespace=namespace, premium=premium, **args
        )

    if succeeded:
        succeeded = compile_arm_package(sdk_root, module)
        if succeeded:
            if premium:
                move_premium_samples(sdk_root, service, module)
                update_azure_resourcemanager_pom(sdk_root, module, current_version)
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
