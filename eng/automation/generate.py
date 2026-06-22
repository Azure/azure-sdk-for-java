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
    is_first_release,
)
from generate_data import (
    sdk_automation_typespec_project as sdk_automation_typespec_project_data,
)
from generate_utils import (
    compare_with_maven_package,
    compile_arm_package,
    generate_typespec_project,
)

os.chdir(pwd)


def parse_args() -> (argparse.ArgumentParser, argparse.Namespace):
    parser = argparse.ArgumentParser()
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


def infer_sdk_release_type(sdk_root: str, sdk_folder: str, module: str) -> str:
    """Infer SDK release type from the generated metadata JSON.

    Reads {sdk_root}/{sdk_folder}/src/main/resources/META-INF/{module}_metadata.json
    and inspects the apiVersions values:
    - All GA (no 'preview' substring) -> 'stable'
    - Any preview or mixed -> 'beta'
    - Fallback on error -> 'beta' (safe default)
    """
    metadata_path = os.path.join(sdk_root, sdk_folder, "src", "main", "resources", "META-INF", f"{module}_metadata.json")
    try:
        with open(metadata_path, "r") as f:
            metadata = json.load(f)
        api_versions = metadata.get("apiVersions", {})
        if not api_versions:
            logging.warning(f"[SelfServe] No apiVersions found in {metadata_path}, defaulting to beta.")
            return "beta"

        has_preview = any("preview" in v.lower() for v in api_versions.values())
        inferred = "beta" if has_preview else "stable"
        logging.info(f"[SelfServe] Inferred sdkReleaseType={inferred} from apiVersions: {api_versions}")
        return inferred
    except FileNotFoundError:
        logging.warning(f"[SelfServe] Metadata file not found: {metadata_path}, defaulting to beta.")
        return "beta"
    except Exception as e:
        logging.warning(f"[SelfServe] Failed to read metadata file {metadata_path}: {e}, defaulting to beta.")
        return "beta"


def update_revapi_skip(pom_path: str, beta: bool):
    """Update revapi.skip property in pom.xml based on release type.

    beta=True:  ensure <revapi.skip>true</revapi.skip> (add if missing, flip if false)
    beta=False: flip <revapi.skip>true</revapi.skip> to false if present (skip if absent, as false is default)
    """
    try:
        with open(pom_path, "r") as f:
            content = f.read()
        if beta:
            if "<revapi.skip>true</revapi.skip>" in content:
                return
            if "<revapi.skip>false</revapi.skip>" in content:
                new_content = content.replace("<revapi.skip>false</revapi.skip>", "<revapi.skip>true</revapi.skip>")
                logging.info(f"[SelfServe] Changed revapi.skip to true in {pom_path}")
            else:
                new_content = re.sub(
                    r'([ \t]*)</properties>',
                    r'\1  <revapi.skip>true</revapi.skip>\n\1</properties>',
                    content,
                    count=1,
                )
                logging.info(f"[SelfServe] Added revapi.skip=true to {pom_path}")
        else:
            if "<revapi.skip>true</revapi.skip>" not in content:
                return
            new_content = content.replace("<revapi.skip>true</revapi.skip>", "<revapi.skip>false</revapi.skip>")
            logging.info(f"[SelfServe] Changed revapi.skip to false in {pom_path}")
        with open(pom_path, "w") as f:
            f.write(new_content)
    except Exception as e:
        logging.warning(f"[SelfServe] Failed to update revapi.skip in {pom_path}: {e}")


def sdk_automation_typespec_project(tsp_project: str, config: dict) -> dict:

    base_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
    sdk_root = os.path.abspath(os.path.join(base_dir, SDK_ROOT))
    spec_root = os.path.abspath(config["specFolder"])
    head_sha: str = config["headSha"]
    repo_url: str = config["repoHttpsUrl"]
    sdk_release_type: str = config["sdkReleaseType"] if "sdkReleaseType" in config else None
    api_version = config["apiVersion"] if "apiVersion" in config else None
    # Generate with beta by default; will be corrected after inference if needed
    release_beta_sdk: bool = not sdk_release_type or sdk_release_type == "beta"
    breaking: bool = False
    changelog = ""
    breaking_change_items = []

    succeeded, require_sdk_integration, sdk_folder, service, module = generate_typespec_project(
        tsp_project,
        sdk_root,
        spec_root,
        head_sha,
        repo_url,
        remove_before_regen=True,
        group_id=GROUP_ID,
        api_version=api_version,
    )

    if succeeded:
        # Infer sdk release type from generated metadata when not explicitly provided.
        # However, if the package has never been released, always force a beta first release
        # (1.0.0-beta.1) regardless of whether the API version is GA or preview.
        if not sdk_release_type and sdk_folder and module:
            if is_first_release(sdk_root, GROUP_ID, module):
                logging.info(
                    f"[SelfServe] Package {GROUP_ID}:{module} has never been released; "
                    f"forcing first release to {DEFAULT_VERSION} (sdkReleaseType=beta)."
                )
                release_beta_sdk = True
            else:
                inferred_type = infer_sdk_release_type(sdk_root, sdk_folder, module)
                release_beta_sdk = inferred_type == "beta"

        # TODO (weidxu): move to typespec-java
        if require_sdk_integration:
            update_service_files_for_new_lib(sdk_root, service, GROUP_ID, module)
            update_root_pom(sdk_root, service)

        # get the stable version and current version from version_client.txt, current version in version_client will be updated if the release type is GA. 
        # e.g. If current version is 1.2.0-beta.1 and the release type is GA, then current version will be updated to 1.2.0
        stable_version, current_version = set_or_increase_version(sdk_root, GROUP_ID, module, preview=release_beta_sdk)
        output_folder = sdk_folder
        update_version(sdk_root, output_folder)

        # Update revapi.skip based on release type
        update_revapi_skip(os.path.join(sdk_root, output_folder, "pom.xml"), beta=release_beta_sdk)

        # compile
        succeeded = compile_arm_package(sdk_root, module)
        if succeeded:
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


def main():
    (parser, args) = parse_args()
    args = vars(args)

    config = args.get("config")
    if config:
        if len(config) != 2:
            parser.error("config requires exactly two arguments: generationInput and generationOutput")
        return sdk_automation(config[0], config[1])

    parser.print_help()
    sys.exit(0)


if __name__ == "__main__":
    logging.basicConfig(
        stream=sys.stdout,
        level=logging.INFO,
        format="%(asctime)s [%(levelname)s] %(message)s",
        datefmt="%Y-%m-%d %X",
    )
    main()
