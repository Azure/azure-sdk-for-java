#!/usr/bin/env python3

#  Copyright (c) Microsoft Corporation. All rights reserved.
#  Licensed under the MIT License.

import os
import sys
import logging
import argparse
import subprocess
import glob
import shutil
import json
from typing import List

sdk_root: str
# script is in "eng/pipelines/scripts/"
script_root: str = os.path.dirname(os.path.realpath(__file__))

skip_artifacts: List[str] = [
    "azure-ai-anomalydetector",  # deprecated
    # expect failure on below
    # "azure-developer-devcenter",  # 2 breaks introduced into stable api-version
    # "azure-ai-vision-face",  # SDK in development
]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--sdk-root",
        type=str,
        required=True,
        help="azure-sdk-for-java repository root.",
    )
    parser.add_argument(
        "--package-json-path",
        type=str,
        required=False,
        default="",
        help="path to package.json of typespec-java. Required when --emitter-version is empty "
        "(the dev build route).",
    )
    parser.add_argument(
        "--emitter-version",
        type=str,
        required=False,
        default="",
        help="published @azure-tools/typespec-java version to regenerate with. When empty, the "
        "emitter is built from source (dev route) instead.",
    )
    return parser.parse_args()


EMITTER_PACKAGE_NAME = "@azure-tools/typespec-java"


def set_emitter_dependency(emitter_ref: str) -> None:
    # Only update the typespec-java dependency in the existing emitter-package.json. The other
    # (peer/dev) dependency versions are managed elsewhere and updated before this pipeline runs,
    # so we intentionally do not regenerate them from the emitter tarball (which may pin stale
    # versions, e.g. an older typespec-client-generator-core). emitter_ref is either a published
    # version string or a local path to the dev package tarball.
    emitter_package_path = os.path.join(sdk_root, "eng", "emitter-package.json")
    with open(emitter_package_path, "r") as json_file:
        package_json = json.load(json_file)
    package_json["dependencies"][EMITTER_PACKAGE_NAME] = emitter_ref
    with open(emitter_package_path, "w") as json_file:
        logging.info(f"Update emitter-package.json to use typespec-java {emitter_ref}")
        json.dump(package_json, json_file, indent=2)


def update_emitter(package_json_path: str, emitter_version: str):
    # 'none' is the pipeline sentinel for "not specified" (Azure DevOps string parameters
    # cannot be left truly empty in the run UI), so normalize it to empty here.
    if emitter_version.lower() == "none":
        emitter_version = ""

    if emitter_version:
        # Published route (post-publish): point emitter-package.json at a published version
        # (released or unreleased/dev), then regenerate the lock file.
        set_emitter_dependency(emitter_version)
    else:
        # Dev route: build the emitter from source, then point emitter-package.json at the local
        # dev package tarball.
        if not package_json_path:
            raise ValueError("--package-json-path is required when --emitter-version is empty.")

        # Locate the dev package tarball built by the "Build typespec-java dev package" step of
        # the post-publish-emitter pipeline (pnpm pack), next to the emitter's package.json.
        dev_package_path = None
        typespec_extension_path = os.path.dirname(package_json_path)
        for file in os.listdir(typespec_extension_path):
            if file.endswith(".tgz"):
                dev_package_path = os.path.abspath(os.path.join(typespec_extension_path, file))
                logging.info(f'Found dev package at "{dev_package_path}"')
                break
        if not dev_package_path:
            logging.error("Failed to locate the dev package.")
            return

        set_emitter_dependency(dev_package_path)

    logging.info("Update emitter-package-lock.json")
    generate_lock_file()


def update_latest_dev():
    subprocess.check_call(
        ["npx", "-y", "@azure-tools/typespec-bump-deps", "eng/emitter-package.json", "--add-npm-overrides"],
        cwd=sdk_root,
    )


def generate_lock_file():
    subprocess.check_call(["tsp-client", "generate-lock-file"], cwd=sdk_root)


def get_generated_folder_from_artifact(module_path: str, artifact: str, type: str) -> str:
    path = os.path.join(module_path, "src", type, "java", "com")
    for seg in artifact.split("-"):
        path = os.path.join(path, seg)
    path = os.path.join(path, "generated")
    return path


def update_sdks():
    failed_modules = []
    for tsp_location_file in glob.glob(os.path.join(sdk_root, "sdk/*/*/tsp-location.yaml")):
        module_path = os.path.dirname(tsp_location_file)
        artifact = os.path.basename(module_path)

        arm_module = "-resourcemanager-" in artifact

        if artifact in skip_artifacts:
            continue

        # # update commit ID for ARM module
        # commit_id = "3c15c2f8c50fb3130b34887d29442da75f07fefb"
        # if commit_id and arm_module:
        #     with open(tsp_location_file, "r", encoding="utf-8") as f_in:
        #         lines = f_in.readlines()
        #     lines_out = []
        #     for line in lines:
        #         if line.startswith("commit:"):
        #             line = f"commit: {commit_id}\n"
        #         lines_out.append(line)
        #     with open(tsp_location_file, "w", encoding="utf-8") as f_out:
        #         f_out.writelines(lines_out)

        #     logging.info("Updated tsp-location file content:\n%s", "".join(lines_out))

        if os.path.dirname(module_path).endswith("-v2"):
            # skip modules on azure-core-v2
            logging.info(f"Skip azure-core-v2 module on path {module_path}")
            continue

        generated_samples_path = os.path.join(
            module_path, get_generated_folder_from_artifact(module_path, artifact, "samples")
        )
        generated_test_path = os.path.join(
            module_path, get_generated_folder_from_artifact(module_path, artifact, "test")
        )
        generated_samples_exists = os.path.isdir(generated_samples_path)
        generated_test_exists = os.path.isdir(generated_test_path)

        if arm_module:
            logging.info("Delete generated source code of resourcemanager module %s", artifact)
            shutil.rmtree(os.path.join(module_path, "src", "main", "resources"), ignore_errors=True)
            delete_generated_source_code(os.path.join(module_path, "src", "main", "java"))

        logging.info(f"Generate for module {artifact}")
        try:
            subprocess.check_call(["tsp-client", "update"], cwd=module_path)
        except subprocess.CalledProcessError:
            # one retry
            # sometimes customization have intermittent failure
            logging.warning(f"Retry generate for module {artifact}")
            try:
                subprocess.check_call(["tsp-client", "update", "--debug"], cwd=module_path)
            except subprocess.CalledProcessError:
                logging.error(f"Failed to generate for module {artifact}")
                failed_modules.append(artifact)

        if not arm_module:
            # run mvn package, as this is what's done in "TypeSpec-Compare-CurrentToCodegeneration.ps1" script
            try:
                subprocess.check_call(
                    ["mvn", "--no-transfer-progress", "codesnippet:update-codesnippet"], cwd=module_path
                )
            except subprocess.CalledProcessError:
                logging.error(f"Failed to update code snippet for module {artifact}")
                failed_modules.append(artifact)

        if arm_module:
            # revert mock test code
            cmd = ["git", "checkout", "src/test"]
            subprocess.check_call(cmd, cwd=module_path)

        # For ARM module, we want to keep the generated samples code.
        # For data-plane, if the generated samples/test code is not there before generation, we will delete the generated code after generation, to avoid unnecessary code check-in.
        if not generated_samples_exists and not arm_module:
            shutil.rmtree(generated_samples_path, ignore_errors=True)
        if not generated_test_exists:
            shutil.rmtree(generated_test_path, ignore_errors=True)

    # revert change on pom.xml, readme.md, changelog.md, etc.
    cmd = ["git", "checkout", "**/pom.xml"]
    subprocess.check_call(cmd, cwd=sdk_root)
    cmd = ["git", "checkout", "**/*.md"]
    subprocess.check_call(cmd, cwd=sdk_root)

    # temporary, revert change on metadata.json
    cmd = ["git", "checkout", "**/*_metadata.json"]
    subprocess.check_call(cmd, cwd=sdk_root)

    cmd = ["git", "add", "."]
    subprocess.check_call(cmd, cwd=sdk_root)

    if failed_modules:
        logging.error(f"Failed modules {failed_modules}")


def apply_patches() -> None:
    failed_patches = []
    for patch_file in glob.glob(os.path.join(script_root, "patches/*.patch")):
        try:
            subprocess.check_call(["git", "apply", patch_file, "--ignore-whitespace"], cwd=sdk_root)
        except subprocess.CalledProcessError:
            logging.error(f"Failed to apply patch {patch_file}")
            failed_patches.append(patch_file)

    if failed_patches:
        logging.error(f"Failed patches {failed_patches}")


def delete_generated_source_code(path: str) -> None:
    autorest_generated_header = "Code generated by Microsoft (R) AutoRest Code Generator"
    typespec_generated_header = "Code generated by Microsoft (R) TypeSpec Code Generator"
    if os.path.exists(path):
        for file in os.listdir(path):
            cur_path = os.path.join(path, file)
            if os.path.isdir(cur_path):
                # Recurse into subdirectory
                delete_generated_source_code(cur_path)
            else:
                try:
                    # Read file content and check for header
                    with open(cur_path, "r", encoding="utf-8") as f:
                        content = f.read()
                    if autorest_generated_header in content or typespec_generated_header in content:
                        os.remove(cur_path)  # Delete the file
                except Exception as e:
                    # Skip files that can't be read (binary files, permission issues)
                    print(f"Warning: Could not process file {cur_path}: {e}")
                    continue


def main():
    global sdk_root

    args = vars(parse_args())
    sdk_root = args["sdk_root"]

    update_emitter(
        args["package_json_path"],
        args["emitter_version"],
    )

    update_sdks()

    apply_patches()


if __name__ == "__main__":
    logging.basicConfig(
        stream=sys.stdout,
        level=logging.INFO,
        format="%(asctime)s %(levelname)s %(message)s",
        datefmt="%Y-%m-%d %X",
    )
    main()
