import os
import re
import sys
import yaml
import logging


def validate_tspconfig(tsp_dir: str) -> bool:
    """
    Validate that the "tspconfig.yaml" file is correctly configured for Java Azure lib.
    Side effect, the function would log/print the error messages, if validation fails.

    :param tsp_dir: the path to the "tspconfig.yaml" file, or the directory contains the file.
    :return: whether this "tspconfig.yaml" file is valid.
    """

    valid = True

    if not tsp_dir.endswith("tspconfig.yaml"):
        tsp_dir = os.path.join(tsp_dir, "tspconfig.yaml")
    with open(tsp_dir, "r") as tspconfig:
        yaml_json = yaml.safe_load(tspconfig)

    service_dir_pattern = r"sdk/\w+"
    package_dir_pattern = r"azure(-\w+)+"
    namespace_pattern = r"com\.azure(\.\w+)+"

    # SDK automation would make sure these properties exists
    service_dir: str = yaml_json["parameters"]["service-dir"]["default"]
    package_dir: str = yaml_json["options"]["@azure-tools/typespec-java"]["package-dir"]
    if "namespace" not in yaml_json["options"]["@azure-tools/typespec-java"]:
        log_and_print_error(
            "[VALIDATE][tspconfig.yaml] "
            "options.@azure-tools/typespec-java.namespace is REQUIRED for Java SDK. "
            'E.g. "com.azure.ai.openai".'
        )
        return False
    namespace: str = yaml_json["options"]["@azure-tools/typespec-java"]["namespace"]

    # validate service-dir
    service_dir_segments = service_dir.split("/")
    if not re.fullmatch(service_dir_pattern, service_dir):
        valid = False
        log_and_print_error(
            "[VALIDATE][tspconfig.yaml] "
            'parameters.service-dir.default SHOULD be "sdk/<service>". '
            "See https://azure.github.io/azure-sdk/policies_repostructure.html. "
            f"Current value: {service_dir}"
        )

    # validate package_dir
    if not re.fullmatch(package_dir_pattern, package_dir):
        valid = False
        log_and_print_error(
            "[VALIDATE][tspconfig.yaml] "
            'options.@azure-tools/typespec-java.package-dir SHOULD start with "azure-". '
            'E.g. "azure-ai-openai". '
            f"Current value: {package_dir}"
        )

    # validate namespace
    if not re.fullmatch(namespace_pattern, namespace):
        valid = False
        log_and_print_error(
            "[VALIDATE][tspconfig.yaml] "
            'options.@azure-tools/typespec-java.namespace SHOULD start with "com.azure.". '
            'E.g. "com.azure.ai.openai". '
            f"Current value: {namespace}"
        )

    # validate package_dir matches namespace
    if valid:
        expected_package_dir = namespace[4:].replace(".", "-")
        if expected_package_dir != package_dir:
            valid = False
            log_and_print_error(
                "[VALIDATE][tspconfig.yaml] package_dir does not match namespace. "
                f'Expected package_dir from namespace "{namespace}" is: {expected_package_dir}'
            )

    return valid


def log_and_print_error(error_message: str):
    logging.error(error_message)
    print(error_message, file=sys.stderr)
