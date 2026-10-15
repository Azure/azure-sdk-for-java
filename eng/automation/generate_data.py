#!/usr/bin/env python3
import os
import sys
import glob
import logging

from utils import get_latest_ga_version
from generate_utils import compare_with_maven_package

from parameters import *
from utils import set_or_default_version
from utils import update_service_files_for_new_lib
from utils import update_root_pom

from generate_utils import generate_typespec_project, clean_sdk_folder

GROUP_ID = "com.azure"


def sdk_automation_typespec_project(tsp_project: str, config: dict) -> dict:
    base_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
    sdk_root = os.path.abspath(os.path.join(base_dir, SDK_ROOT))
    spec_root = os.path.abspath(config["specFolder"])
    head_sha: str = config["headSha"]
    repo_url: str = config["repoHttpsUrl"]
    breaking: bool = False
    changelog: str = ""
    breaking_change_items = []

    # the fallback logic is only enabled when this automation is run for specs PR validation
    # we do not want to delete code from user for SDK generation
    fallback_generate_from_clean_folder_enabled = "runMode" in config and (
        config["runMode"] == "spec-pull-request" or config["runMode"] == "batch"
    )
    clean_sdk_folder_succeeded = False

    succeeded, require_sdk_integration, sdk_folder, service, module = generate_typespec_project(
        tsp_project, sdk_root, spec_root, head_sha, repo_url
    )

    if not succeeded and fallback_generate_from_clean_folder_enabled:
        # error in emitter
        # fallback to generate from a clean folder
        clean_sdk_folder_succeeded = clean_sdk_folder(sdk_root, sdk_folder)
        if clean_sdk_folder_succeeded:
            # re-generate
            succeeded, require_sdk_integration, sdk_folder, service, module = generate_typespec_project(
                tsp_project, sdk_root, spec_root, head_sha, repo_url, disable_customization=True
            )

    if succeeded:
        stable_version, current_version = set_or_default_version(sdk_root, GROUP_ID, module)
        if require_sdk_integration:
            update_service_files_for_new_lib(sdk_root, service, GROUP_ID, module)
            update_root_pom(sdk_root, service)
        if clean_sdk_folder_succeeded:
            current_version = DEFAULT_VERSION

        # compile
        succeeded = compile_package(sdk_root, GROUP_ID, module)
        if succeeded:
            breaking, changelog, breaking_change_items = compare_with_maven_package(
                sdk_root,
                GROUP_ID,
                service,
                get_latest_ga_version(GROUP_ID, module, stable_version),
                current_version,
                module,
            )
        elif fallback_generate_from_clean_folder_enabled:
            # error in compile
            # fallback to generate from a clean folder
            clean_sdk_folder_succeeded = clean_sdk_folder(sdk_root, sdk_folder)
            if clean_sdk_folder_succeeded:
                # re-generate
                succeeded, require_sdk_integration, sdk_folder, service, module = generate_typespec_project(
                    tsp_project, sdk_root, spec_root, head_sha, repo_url, disable_customization=True
                )
                stable_version, _ = set_or_default_version(sdk_root, GROUP_ID, module)
                current_version = DEFAULT_VERSION
                if require_sdk_integration:
                    update_service_files_for_new_lib(sdk_root, service, GROUP_ID, module)
                    update_root_pom(sdk_root, service)
                # compile
                succeeded = compile_package(sdk_root, GROUP_ID, module)
                if succeeded:
                    breaking, changelog, breaking_change_items = compare_with_maven_package(
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


def compile_package(sdk_root: str, group_id: str, module: str) -> bool:
    command = "mvn --no-transfer-progress clean package -f {0}/pom.xml -Dmaven.javadoc.skip -Dgpg.skip -DskipTestCompile -Djacoco.skip -Drevapi.skip -pl {1}:{2} -am".format(
        sdk_root, group_id, module
    )
    logging.info(command)
    if os.system(command) != 0:
        logging.error("[COMPILE] Maven build fail.")
        logging.error(
            'One reason of the compilation failure is that the existing code customization in SDK repository being incompatible with the class generated from updated TypeSpec source. In such case, you can ignore the failure, and fix the customization in SDK repository. You can inquire in "Language - Java" Teams channel. Please include the link of this Pull Request in the query.'
        )
        return False
    return True
