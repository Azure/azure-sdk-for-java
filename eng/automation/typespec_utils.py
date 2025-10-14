import os
import re
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

    namespace_pattern = r"com\.azure(\.\w+)+"

    # SDK automation would make sure these properties exists
    if "namespace" not in yaml_json["options"]["@azure-tools/typespec-java"]:
        logging.error(
            "[VALIDATE][tspconfig.yaml] "
            "options.@azure-tools/typespec-java.namespace is REQUIRED for Java SDK. "
            'E.g. "com.azure.ai.contentsafety" for data-plane SDK, "com.azure.resourcemanager.fabric" for management-plane SDK.'
        )
        return False
    namespace: str = yaml_json["options"]["@azure-tools/typespec-java"]["namespace"]

    # validate namespace
    if not re.fullmatch(namespace_pattern, namespace):
        valid = False
        logging.error(
            "[VALIDATE][tspconfig.yaml] "
            'options.@azure-tools/typespec-java.namespace SHOULD start with "com.azure.". '
            'E.g. "com.azure.ai.contentsafety" for data-plane SDK, "com.azure.resourcemanager.fabric" for management-plane SDK. '
            f"Current value: {namespace}"
        )

    # TODO: Ensure that 'emitter-output-dir' matches the Java package structure defined by 'namespace'.
    # For example, if namespace is 'com.azure.ai.contentsafety', emitter-output-dir should reflect this structure in its path.
    # See issue tracker: https://github.com/Azure/azure-sdk-for-java/issues/XXXX for details.

    return valid
