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
import tarfile
import tempfile
import urllib.request
from typing import List

sdk_root: str
# script is in "eng/pipelines/scripts/"
script_root: str = os.path.dirname(os.path.realpath(__file__))

skip_artifacts: List[str] = [
    "azure-ai-anomalydetector",  # deprecated
    # expect failure on below
    # emitter generates inconsistent code for id-parameterized subclients (LargePersonGroup,
    # LargeFaceList): wrapper clients call the removed plural getLargePersonGroups()/getLargeFaceLists()
    # accessors and stale plural *sImpl files are left behind, so the module does not compile.
    "azure-ai-vision-face",
    # "azure-developer-devcenter",  # 2 breaks introduced into stable api-version
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

# Prefixes of the TypeSpec dependency entries in emitter-package.json whose versions we resolve to
# the latest published npm version (see resolve_dependency_versions_to_latest).
TYPESPEC_DEPENDENCY_PREFIXES = ("@azure-tools/", "@typespec/")

# TypeSpec libraries that some specs depend on but which are intentionally NOT declared as
# dependencies of the typespec-java emitter itself (declaring them there would force every
# downstream emitter/repo to carry them - see discussion on Azure/typespec-azure PR 4866). We add
# them to emitter-package.json here so that generated SDKs referencing these libraries compile.
#
# These are versioned from the azure-rest-api-specs package.json (the source of truth for the
# versions the specs actually use), falling back to the latest published npm version if the specs
# repo has not updated yet. They are released independently of the TypeSpec compiler/Azure
# libraries, so their specs-pinned version does not have to move in lockstep with the emitter's
# other dependencies.
DESIGNATED_LIBRARIES_FROM_SPECS = [
    "@azure-tools/openai-typespec",
    "@azure-tools/typespec-liftr-base",
]

# These designated libraries are versioned from the latest published npm version instead of the
# specs repo. They belong to the same release group as the TypeSpec compiler / Azure libraries that
# the emitter depends on (e.g. @typespec/*, @azure-tools/typespec-azure-*), which
# resolve_dependency_versions_to_latest already pins to the latest published version. Sourcing
# these from npm latest too keeps the whole release group on the same version, avoiding peer
# conflicts that a lagging specs-repo pin (e.g. openapi3 requiring an older @typespec/http) would
# otherwise cause.
DESIGNATED_LIBRARIES_FROM_NPM_LATEST = [
    "@azure-tools/typespec-azure-portal-core",
    "@typespec/openapi3",
]

# All designated libraries, used to exclude them from resolve_dependency_versions_to_latest (they
# are versioned separately by add_designated_libraries).
DESIGNATED_LIBRARIES = DESIGNATED_LIBRARIES_FROM_SPECS + DESIGNATED_LIBRARIES_FROM_NPM_LATEST

# package.json of the specs repo, the source of truth for the specs-versioned designated libraries.
SPECS_PACKAGE_JSON_URL = "https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/package.json"


def emitter_package_json_path() -> str:
    return os.path.join(sdk_root, "eng", "emitter-package.json")


def extract_package_json_from_tgz(tgz_path: str, dest_path: str) -> None:
    # npm/pnpm pack tarballs place all files under a top-level "package/" directory. The
    # package.json inside has the "catalog:"/"workspace:" protocols resolved to concrete
    # versions, which is what tsp-client generate-config-files needs (npm cannot resolve those
    # protocols, and generate-config-files reads the emitter's peerDependencies from this file).
    with tarfile.open(tgz_path, "r:gz") as tar:
        with tar.extractfile("package/package.json") as member:
            with open(dest_path, "wb") as f:
                f.write(member.read())


def generate_config_files(emitter_package_json: str, use_npm_pinning: bool, overrides_path: str = "") -> None:
    # tsp-client generate-config-files seeds eng/emitter-package.json from an emitter package.json
    # (its peerDependencies) and then generates the lock file (via "npm install"). Used by both
    # routes:
    #   - Published route: use_npm_pinning=True so peer versions are pinned from the published
    #     emitter's dependencies (looked up with "npm view").
    #   - Dev route: overrides_path points the emitter dependency at the locally built dev tarball,
    #     so the lock-generation "npm install" resolves the (unpublished) emitter from that tarball
    #     instead of the registry. --use-npm-pinning is not used, because it would "npm view" the
    #     unpublished dev emitter, which does not exist on the registry.
    #
    # Remove the designated libraries from any existing eng/emitter-package.json first:
    # generate-config-files MERGES into it rather than replacing it, and it only overwrites entries
    # that are emitter peers. A designated library left from a previous run (e.g. @typespec/openapi3,
    # which is NOT an emitter peer) would survive the merge with its stale version and break the
    # lock-generation "npm install" with ERESOLVE. We add the designated libraries back (from npm
    # latest or the specs repo) after seeding, so the merge starts from only the emitter's own peers.
    #
    # Alternative: delete the whole eng/emitter-package.json before generating (os.remove) so it is
    # seeded entirely from scratch. That is more robust against non-designated orphans (e.g. an
    # emitter peer that was dropped), but it regenerates the key order from the emitter manifest,
    # producing noisier diffs. We remove only the designated entries to keep the existing key order.
    remove_designated_libraries()

    command = [
        "tsp-client",
        "generate-config-files",
        "--package-json",
        emitter_package_json,
        "--emitter-package-json-path",
        emitter_package_json_path(),
    ]
    if use_npm_pinning:
        command.append("--use-npm-pinning")
    if overrides_path:
        command.extend(["--overrides", overrides_path])
    subprocess.check_call(command, cwd=sdk_root)


def npm_view_version(package_ref: str) -> str:
    # "npm view <ref> version" reads a published version from the npm registry. Preferred over
    # "ncu -u" (which may be unavailable in CFS environments) for looking up dependency versions.
    return subprocess.check_output(
        ["npm", "view", package_ref, "version"],
        cwd=sdk_root,
        text=True,
    ).strip()


def load_emitter_package_json() -> dict:
    with open(emitter_package_json_path(), "r") as json_file:
        return json.load(json_file)


def save_emitter_package_json(package_json: dict) -> None:
    with open(emitter_package_json_path(), "w") as json_file:
        json.dump(package_json, json_file, indent=2)


def remove_designated_libraries() -> None:
    # Remove the designated libraries from an existing eng/emitter-package.json before seeding.
    # generate-config-files merges into that file and only overwrites emitter-peer entries, so a
    # designated library carried over from a previous run (e.g. @typespec/openapi3) would keep its
    # stale version and conflict with the freshly pinned peers. They are added back afterward by
    # add_designated_libraries. The skipped designated libraries (still emitter peers) are re-added
    # by generate-config-files itself from the emitter's peerDependencies.
    path = emitter_package_json_path()
    if not os.path.exists(path):
        return
    package_json = load_emitter_package_json()
    dev_dependencies = package_json.get("devDependencies", {})
    for library in DESIGNATED_LIBRARIES:
        if library in dev_dependencies:
            logging.info(f"Remove designated library {library} before seeding")
            del dev_dependencies[library]
    save_emitter_package_json(package_json)


def resolve_dependency_versions_to_latest() -> None:
    # Seeding (either route) leaves the @azure-tools/* and @typespec/* devDependencies as the
    # emitter's caret ranges (e.g. "^0.70.0"), but eng/emitter-package.json must pin exact versions.
    # Resolve each of these entries to its latest published version via "npm view ...@latest".
    # Designated libraries are handled separately (from the specs repo), so skip them here.
    package_json = load_emitter_package_json()
    dev_dependencies = package_json.get("devDependencies", {})
    for name in list(dev_dependencies.keys()):
        if name in DESIGNATED_LIBRARIES:
            continue
        if name.startswith(TYPESPEC_DEPENDENCY_PREFIXES):
            version = npm_view_version(f"{name}@latest")
            logging.info(f"Resolve {name} to latest published version {version}")
            dev_dependencies[name] = version
    save_emitter_package_json(package_json)


def fetch_specs_dev_dependencies() -> dict:
    # Read the devDependencies map from the specs repo package.json, the source of truth for the
    # designated library versions.
    logging.info(f"Fetch designated library versions from {SPECS_PACKAGE_JSON_URL}")
    with urllib.request.urlopen(SPECS_PACKAGE_JSON_URL, timeout=30) as response:
        specs_package_json = json.loads(response.read().decode("utf-8"))
    return specs_package_json.get("devDependencies", {})


def add_designated_libraries() -> None:
    # Add the designated libraries (not declared by the emitter) to emitter-package.json. Two
    # groups, versioned from different sources:
    #   - DESIGNATED_LIBRARIES_FROM_SPECS: pinned to the exact version declared in the specs repo
    #     package.json (the version the specs actually use). If a library is missing there (the
    #     specs repo may not have updated yet), fall back to its latest published npm version.
    #   - DESIGNATED_LIBRARIES_FROM_NPM_LATEST: pinned to the latest published npm version.
    specs_dev_dependencies = fetch_specs_dev_dependencies()
    package_json = load_emitter_package_json()
    dev_dependencies = package_json.setdefault("devDependencies", {})

    for library in DESIGNATED_LIBRARIES_FROM_SPECS:
        version = specs_dev_dependencies.get(library)
        if version:
            logging.info(f"Add designated library {library}@{version} (from specs repo)")
        else:
            version = npm_view_version(f"{library}@latest")
            logging.info(f"Add designated library {library}@{version} (specs repo missing it, using npm latest)")
        dev_dependencies[library] = version

    for library in DESIGNATED_LIBRARIES_FROM_NPM_LATEST:
        version = npm_view_version(f"{library}@latest")
        logging.info(f"Add designated library {library}@{version} (from npm latest)")
        dev_dependencies[library] = version

    save_emitter_package_json(package_json)


def update_emitter(package_json_path: str, emitter_version: str):
    # 'none' is the pipeline sentinel for "not specified" (Azure DevOps string parameters
    # cannot be left truly empty in the run UI), so normalize it to empty here.
    if emitter_version.lower() == "none":
        emitter_version = ""

    if emitter_version:
        # Published route (post-publish): seed emitter-package.json from the published emitter.
        logging.info(f"Seed emitter-package.json from published typespec-java {emitter_version}")
        with tempfile.TemporaryDirectory() as tmp_dir:
            # "npm view --json" returns the published registry manifest (name, version, main,
            # peerDependencies). generate-config-files --use-npm-pinning reads its peerDependencies
            # to seed emitter-package.json, pinning the emitter to this published version.
            manifest = subprocess.check_output(
                ["npm", "view", f"{EMITTER_PACKAGE_NAME}@{emitter_version}", "--json"],
                cwd=sdk_root,
                text=True,
            )
            published_package_json_path = os.path.join(tmp_dir, "package.json")
            with open(published_package_json_path, "w") as f:
                f.write(manifest)

            logging.info("Update emitter-package.json")
            generate_config_files(published_package_json_path, use_npm_pinning=True)
    else:
        # Dev route: build the emitter from source, then seed emitter-package.json from the local
        # dev package. The dev emitter version is unpublished, so generate-config-files consumes
        # the emitter's (resolved) package.json extracted from the dev tarball, and an overrides
        # file points the emitter dependency at that tarball so the lock-generation "npm install"
        # can resolve the emitter locally instead of from the registry.
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

        with tempfile.TemporaryDirectory() as tmp_dir:
            # The tarball's package.json has "catalog:"/"workspace:" protocols resolved to concrete
            # versions, which generate-config-files needs (npm cannot resolve those protocols).
            resolved_package_json_path = os.path.join(tmp_dir, "package.json")
            extract_package_json_from_tgz(dev_package_path, resolved_package_json_path)

            # Point the (unpublished) emitter dependency at the local dev tarball.
            overrides_path = os.path.join(tmp_dir, "overrides.json")
            with open(overrides_path, "w") as f:
                json.dump({EMITTER_PACKAGE_NAME: dev_package_path}, f)

            logging.info("Update emitter-package.json")
            generate_config_files(resolved_package_json_path, use_npm_pinning=False, overrides_path=overrides_path)

    # Both routes: pin the emitter's TypeSpec dependencies to their latest published versions and
    # add the designated libraries from the specs repo, then (re)generate the lock file so it
    # reflects the final dependency set.
    resolve_dependency_versions_to_latest()
    add_designated_libraries()

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

        generated_samples_path = get_generated_folder_from_artifact(module_path, artifact, "samples")
        generated_test_path = get_generated_folder_from_artifact(module_path, artifact, "test")
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
