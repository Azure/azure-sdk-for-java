#!/usr/bin/env python3
import os
import sys
import shutil
import logging
import argparse
import re
import glob
import subprocess
import yaml
import requests
from utils import get_latest_ga_version
from generate_utils import compare_with_maven_package
from typing import List, Tuple, Optional

from parameters import *
from utils import set_or_default_version
from utils import update_service_files_for_new_lib
from utils import update_root_pom
from utils import ListIndentDumper

from generate_utils import generate_typespec_project, clean_sdk_folder_if_swagger

GROUP_ID = "com.azure"
DPG_ARGUMENTS = "--sdk-integration --generate-samples --generate-tests"
YAML_BLOCK_REGEX = r"```\s?(?:yaml|YAML).*?\n(.*?)```"


def sdk_automation_typespec_project(tsp_project: str, config: dict) -> dict:
    base_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
    sdk_root = os.path.abspath(os.path.join(base_dir, SDK_ROOT))
    spec_root = os.path.abspath(config["specFolder"])
    head_sha: str = config["headSha"]
    repo_url: str = config["repoHttpsUrl"]
    breaking: bool = False
    changelog: str = ""

    succeeded, require_sdk_integration, sdk_folder, service, module = generate_typespec_project(
        tsp_project, sdk_root, spec_root, head_sha, repo_url
    )

    if not succeeded:
        # check whether this is migration from Swagger
        clean_sdk_folder_succeeded = clean_sdk_folder_if_swagger(sdk_root, sdk_folder)
        if clean_sdk_folder_succeeded:
            # re-generate
            succeeded, require_sdk_integration, sdk_folder, service, module = generate_typespec_project(
                tsp_project, sdk_root, spec_root, head_sha, repo_url
            )

    if succeeded:
        # TODO (weidxu): move to typespec-java
        stable_version, current_version = set_or_default_version(sdk_root, GROUP_ID, module)
        if require_sdk_integration:
            update_service_files_for_new_lib(sdk_root, service, GROUP_ID, module)
            update_root_pom(sdk_root, service)

        # compile
        succeeded = compile_package(sdk_root, GROUP_ID, module)
        if succeeded:
            breaking, changelog = compare_with_maven_package(
                sdk_root,
                GROUP_ID,
                service,
                get_latest_ga_version(GROUP_ID, module, stable_version),
                current_version,
                module,
            )
        else:
            # check whether this is migration from Swagger
            clean_sdk_folder_succeeded = clean_sdk_folder_if_swagger(sdk_root, sdk_folder)
            if clean_sdk_folder_succeeded:
                # re-generate
                succeeded, require_sdk_integration, sdk_folder, service, module = generate_typespec_project(
                    tsp_project, sdk_root, spec_root, head_sha, repo_url
                )
                stable_version, current_version = set_or_default_version(sdk_root, GROUP_ID, module)
                if require_sdk_integration:
                    update_service_files_for_new_lib(sdk_root, service, GROUP_ID, module)
                    update_root_pom(sdk_root, service)
                # compile
                succeeded = compile_package(sdk_root, GROUP_ID, module)
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


def get_or_update_sdk_readme(config: dict, readme_file_path: str) -> Optional[str]:
    base_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
    sdk_root = os.path.abspath(os.path.join(base_dir, SDK_ROOT))
    sdk_readme_abspath = None

    if "autorestConfig" in config:
        # autorestConfig

        autorest_config: str = config["autorestConfig"]

        # find 'output-folder', and write swagger/README.md
        autorest_config = autorest_config.replace(r"\r\n", r"\n")
        yaml_blocks = re.findall(YAML_BLOCK_REGEX, autorest_config, re.DOTALL)
        for yaml_str in yaml_blocks:
            yaml_json = yaml.safe_load(yaml_str)
            if "output-folder" in yaml_json:
                output_folder: str = yaml_json["output-folder"]
                if output_folder.startswith("sdk/"):
                    sdk_readme_abspath = os.path.join(sdk_root, output_folder, "swagger", "README.md")
                    os.makedirs(os.path.dirname(sdk_readme_abspath), exist_ok=True)
                    with open(sdk_readme_abspath, "w", encoding="utf-8") as f_out:
                        f_out.write(autorest_config)
                    logging.info("[RESOLVE] Create README from autorestConfig")
                break

    if not sdk_readme_abspath:
        # swagger/README.md in sdk repository

        # find all swagger/README.md in sdk repo
        candidate_sdk_readmes = glob.glob(os.path.join(sdk_root, "sdk/*/*/swagger/README.md"))
        # find the README.md that matches
        sdk_readme_abspath = find_sdk_readme(readme_file_path, candidate_sdk_readmes)

    return sdk_readme_abspath


def sdk_automation(config: dict) -> List[dict]:
    # priority:
    # 1. autorestConfig from input
    # 2. swagger/README.md in sdk repository that matches readme from input

    base_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
    sdk_root = os.path.abspath(os.path.join(base_dir, SDK_ROOT))
    spec_root = os.path.abspath(config["specFolder"])

    packages = []

    # find readme.md in spec repository
    readme_file_paths = []
    for file_path in config["relatedReadmeMdFiles"]:
        match = re.search(
            r"specification/([^/]+)/data-plane(/.*)*/readme.md",
            file_path,
            re.IGNORECASE,
        )
        if match:
            readme_file_paths.append(file_path)

    # readme.md required
    if not readme_file_paths:
        return packages
    # we only take first readme.md
    readme_file_path = readme_file_paths[0]
    logging.info("[RESOLVE] README from specification %s", readme_file_path)

    sdk_readme_abspath = get_or_update_sdk_readme(config, readme_file_path)

    if sdk_readme_abspath:
        spec_readme_abspath = os.path.join(spec_root, readme_file_path)
        update_readme(sdk_readme_abspath, spec_readme_abspath)
        sdk_automation_readme(sdk_readme_abspath, packages, sdk_root)

    return packages


def find_sdk_readme(spec_readme: str, candidate_sdk_readmes: List[str]) -> Optional[str]:
    segments = spec_readme.split("/")
    if "data-plane" in segments:
        index = segments.index("data-plane")
        # include service name, exclude readme.md
        search_target = "/" + "/".join(segments[index - 1 :])

        for sdk_readme_path in candidate_sdk_readmes:
            spec_reference = find_sdk_spec_reference(sdk_readme_path)
            if spec_reference and search_target in spec_reference:
                return sdk_readme_path
    return None


def find_sdk_spec_reference(sdk_readme_path: str) -> Optional[str]:
    with open(sdk_readme_path, "r", encoding="utf-8") as f_in:
        content = f_in.read()
    if content:
        yaml_blocks = re.findall(YAML_BLOCK_REGEX, content, re.DOTALL)
        for yaml_str in yaml_blocks:
            try:
                yaml_json = yaml.safe_load(yaml_str)
                if "data-plane" in yaml_json and yaml_json["data-plane"]:
                    # take 'require'
                    if "require" in yaml_json:
                        require = yaml_json["require"]
                        if isinstance(require, List):
                            require = require[0]
                        return require
                    # take 'input-file', if 'require' not found
                    if "input-file" in yaml_json:
                        input_file = yaml_json["input-file"]
                        if isinstance(input_file, List):
                            input_file = input_file[0]
                        return input_file
            except yaml.YAMLError:
                continue
    return None


def sdk_automation_readme(readme_file_abspath: str, packages: List[dict], sdk_root: str):
    service, module = get_generate_parameters(readme_file_abspath)

    if module:
        succeeded = generate(
            sdk_root, service, module, readme=readme_file_abspath, autorest=AUTOREST_CORE_VERSION, use=AUTOREST_JAVA
        )

        generated_folder = "sdk/{0}/{1}".format(service, module)

        breaking = False
        changelog = ""

        if succeeded:
            stable_version, current_version = set_or_default_version(sdk_root, GROUP_ID, module)
            succeeded = compile_package(sdk_root, GROUP_ID, module)
            if succeeded:
                breaking, changelog = compare_with_maven_package(
                    sdk_root,
                    GROUP_ID,
                    service,
                    get_latest_ga_version(GROUP_ID, module, stable_version),
                    current_version,
                    module,
                )

        artifacts = ["{0}/pom.xml".format(generated_folder)]
        artifacts += [jar for jar in glob.glob("{0}/target/*.jar".format(generated_folder))]
        result = "succeeded" if succeeded else "failed"

        packages.append(
            {
                "packageName": module,
                "path": [
                    generated_folder,
                    CI_FILE_FORMAT.format(service),
                    POM_FILE_FORMAT.format(service),
                    "eng/versioning",
                    "pom.xml",
                ],
                "artifacts": artifacts,
                "apiViewArtifact": next(iter(glob.glob("{0}/target/*-sources.jar".format(generated_folder))), None),
                "language": "Java",
                "result": result,
                "changelog": {"content": changelog, "hasBreakingChange": breaking},
            }
        )


def generate(
    sdk_root: str,
    service: str,
    module: str,
    *,
    input_file: str = None,
    spec_readme: str = None,
    security: str = None,
    security_scopes: str = None,
    title: str = None,
    autorest: str,
    use: str,
    autorest_options: str = "",
    readme: str = None,
) -> bool:
    namespace = "com.{0}".format(module.replace("-", "."))
    output_dir = os.path.join(sdk_root, "sdk", service, module)
    # shutil.rmtree(os.path.join(output_dir, 'src/main'), ignore_errors=True)
    shutil.rmtree(
        os.path.join(output_dir, "src/samples/java", namespace.replace(".", "/"), "generated"), ignore_errors=True
    )
    shutil.rmtree(
        os.path.join(output_dir, "src/test/java", namespace.replace(".", "/"), "generated"), ignore_errors=True
    )

    if readme:
        # use readme from spec repo
        readme_file_path = readme

        require_sdk_integration = not os.path.exists(os.path.join(output_dir, "src"))

        logging.info("[GENERATE] Autorest from README {}".format(readme_file_path))

        command = (
            "autorest --version={0} --use={1} --java --data-plane "
            "--java.java-sdks-folder={2} --java.output-folder={3} {4} {5}".format(
                autorest,
                use,
                os.path.abspath(sdk_root),
                os.path.abspath(output_dir),
                readme_file_path,
                autorest_options,
            )
        )
        if require_sdk_integration:
            command += " --java.namespace={0} ".format(namespace) + DPG_ARGUMENTS
        logging.info(command)
        try:
            subprocess.run(command, shell=True, check=True)
        except subprocess.CalledProcessError:
            logging.error("[GENERATE] Autorest fail")
            return False

        if require_sdk_integration:
            set_or_default_version(sdk_root, GROUP_ID, module)
            update_service_files_for_new_lib(sdk_root, service, GROUP_ID, module)
            update_root_pom(sdk_root, service)
    else:
        # no readme

        security_arguments = ""
        if security:
            security_arguments += "--security={0}".format(security)
        if security_scopes:
            security_arguments += " --security-scopes={0}".format(security_scopes)

        if spec_readme:
            logging.info("[GENERATE] Autorest from README {}".format(spec_readme))
            input_arguments = "--require={0}".format(spec_readme)
        else:
            logging.info("[GENERATE] Autorest from JSON {}".format(input_file))
            input_arguments = "--input-file={0}".format(input_file)

        artifact_arguments = "--artifact-id={0}".format(module)
        if title:
            artifact_arguments += " --title={0}".format(title)

        command = (
            "autorest --version={0} --use={1} --java --data-plane "
            "--java.java-sdks-folder={2} --java.output-folder={3} "
            "--java.namespace={4} {5}".format(
                autorest,
                use,
                os.path.abspath(sdk_root),
                os.path.abspath(output_dir),
                namespace,
                " ".join((DPG_ARGUMENTS, input_arguments, security_arguments, artifact_arguments, autorest_options)),
            )
        )
        logging.info(command)
        if os.system(command) != 0:
            logging.error("[GENERATE] Autorest fail")
            return False

        set_or_default_version(sdk_root, GROUP_ID, module)
        update_service_files_for_new_lib(sdk_root, service, GROUP_ID, module)
        update_root_pom(sdk_root, service)
        # update_version(sdk_root, output_dir)

    return True


def compile_package(sdk_root: str, group_id: str, module: str) -> bool:
    command = "mvn --no-transfer-progress clean package -f {0}/pom.xml -Dmaven.javadoc.skip -Dgpg.skip -DskipTestCompile -Djacoco.skip -Drevapi.skip -pl {1}:{2} -am".format(
        sdk_root, group_id, module
    )
    logging.info(command)
    if os.system(command) != 0:
        error_message = (
            "[COMPILE] Maven build fail.\n"
            "One reason of the compilation failure is that the existing code customization in SDK repository being incompatible with the class generated from updated TypeSpec source. In such case, you can ignore the failure, and fix the customization in SDK repository.\n"
            'You can inquire in "Language - Java" Teams channel. Please include the link of this Pull Request in the query.'
        )
        logging.error(error_message)
        print(error_message, file=sys.stderr)
        return False
    return True


def get_generate_parameters(readme_file_abspath: str) -> Tuple[str, str]:
    # get parameters from swagger/README.md from sdk repository

    service = None
    module = None
    if readme_file_abspath:
        # try swagger/readme.md for service and module
        if not module and os.path.basename(readme_file_abspath).lower() == "readme.md":
            dir_name = os.path.dirname(readme_file_abspath).lower()
            if os.path.basename(dir_name) == "swagger":
                dir_name = os.path.dirname(dir_name)
                module = os.path.basename(dir_name)
                dir_name = os.path.dirname(dir_name)
                service = os.path.basename(dir_name)
                dir_name = os.path.dirname(dir_name)
                if not os.path.basename(dir_name) == "sdk":
                    module = None
                    service = None

    return service, module


def uri_file_exists(file_path: str) -> bool:
    if file_path.startswith("http://") or file_path.startswith("https://"):
        return requests.head(file_path).status_code / 100 == 2
    else:
        return os.path.exists(file_path)


def uri_file_read(file_path: str) -> str:
    if file_path.startswith("http://") or file_path.startswith("https://"):
        return requests.get(file_path).text
    else:
        with open(file_path, "r", encoding="utf-8") as f_in:
            return f_in.read()


def find_readme(json_file_path: str, readme_file_paths: List[str], spec_root: str) -> Optional[str]:
    if not readme_file_paths:
        return None

    # ideally we'd like to match readme in more specific path
    readme_file_paths = sorted(readme_file_paths, key=len, reverse=True)

    json_dir_name = os.path.dirname(json_file_path)
    for readme_file_path in readme_file_paths:
        readme_dir_name = os.path.dirname(readme_file_path)
        if json_dir_name.startswith(readme_dir_name):
            java_readme_path = os.path.join(spec_root, readme_file_path).replace(".md", ".java.md")
            if os.path.exists(java_readme_path):
                return readme_file_path

    return None


def update_readme(sdk_readme_abspath: str, spec_readme: str = None):
    # update README_SPEC.md in SDK repo

    with open(sdk_readme_abspath, "r", encoding="utf-8") as f_in:
        content = f_in.read()
    if content:
        yaml_blocks = re.findall(YAML_BLOCK_REGEX, content, re.DOTALL)
        for yaml_str in yaml_blocks:
            yaml_json = yaml.safe_load(yaml_str)
            yaml_json.pop("require", None)
            yaml_json.pop("input-file", None)
            if spec_readme:
                yaml_json["require"] = spec_readme

            # write updated yaml
            updated_yaml_str = yaml.dump(yaml_json, width=sys.maxsize, sort_keys=False, Dumper=ListIndentDumper)

            if not yaml_str == updated_yaml_str:
                # update readme
                updated_content = content.replace(yaml_str, updated_yaml_str, 1)
                with open(sdk_readme_abspath, "w", encoding="utf-8") as f_out:
                    f_out.write(updated_content)

                logging.info(
                    "[GENERATE] YAML block in README updated from\n{0}\nto\n{1}".format(yaml_str, updated_yaml_str)
                )
            break


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--input-file",
        required=False,
        help='URL to OpenAPI 2.0 specification JSON as input file. "service" and "module" is required.',
    )
    parser.add_argument(
        "--spec-readme",
        required=False,
        help='URL to readme.md from specification repository as input file. "service" and "module" is required.',
    )
    parser.add_argument(
        "-r",
        "--readme",
        required=False,
        help='URL to "readme.md" as configuration file.',
    )
    parser.add_argument(
        "--service",
        required=False,
        help="Service name under sdk/. Sample: storage",
    )
    parser.add_argument(
        "--module",
        required=False,
        help="Module name under sdk/<service>/. Sample: azure-storage-blob",
    )
    parser.add_argument(
        "--security",
        required=False,
        help="Security schemes for authentication. "
        'Sample: "AADToken" for AAD credential for OAuth 2.0 authentication; '
        '"AzureKey" for Azure key credential',
    )
    parser.add_argument(
        "--security-scopes",
        required=False,
        help='OAuth 2.0 scopes when "security" includes "AADToken". ' "Sample: https://storage.azure.com/.default",
    )
    parser.add_argument(
        "--title",
        required=False,
        help='The name of the client. The name should always ends with "Client". '
        "Sample: BlobClient, which makes BlobClientBuilder as builder class",
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

    return parser.parse_args()


def main():
    args = vars(parse_args())

    base_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
    sdk_root = os.path.abspath(os.path.join(base_dir, SDK_ROOT))

    if args["readme"]:
        readme_file_abspath = os.path.abspath(args["readme"])
        service, module = get_generate_parameters(readme_file_abspath)
        if not module:
            raise ValueError("readme.md not found or not well-formed")
        args["service"] = service
        args["module"] = module
    else:
        if not args["input_file"] and not args["spec_readme"]:
            raise ValueError('Either "readme", or "spec-readme", or "input-file" argument is required')
        if not args["service"] or not args["module"]:
            raise ValueError('"service" and "module" argument is required')

    succeeded = generate(sdk_root, **args)
    if succeeded:
        succeeded = compile_package(sdk_root, GROUP_ID, args["module"])

    if not succeeded:
        raise RuntimeError("Failed to generate code or compile the package")


if __name__ == "__main__":
    logging.basicConfig(
        stream=sys.stdout,
        level=logging.INFO,
        format="%(asctime)s %(levelname)s %(message)s",
        datefmt="%Y-%m-%d %X",
    )
    main()
