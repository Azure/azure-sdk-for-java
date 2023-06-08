# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# This script is used to generate Azure client library for Java from TypeSpec.
# It combines two steps:
# 1. Clone the remote repository that has the TypeSpec service definition using the details specified in tsp-location.yaml
# 2. Generate the Java client library

# For the first step, the TypeSpec-Project-Sync.ps1 script is used and for the second, TypeSpec-Project-Generate.ps1 is used.

# Python version 3.4 or higher is required.
# This script must be run from the root directory of the azure-sdk-for-java repository. The script takes the relative
# path of the directory that has the tsp-location.yaml file. The code will also be generated in the same directory.

# For example, the following command will generate the OpenAI client library in sdk/openai/azure-ai-openai directory.
# python eng/scripts/typespec-sync-generate.py --o sdk/openai/azure-ai-openai

# For more detail, please refer to this wiki - https://github.com/Azure/azure-sdk-for-java/wiki/TypeSpec-Java-Quickstart#use-typespec-defined-in-rest-api-specifications

import subprocess
import sys
import time
import argparse
from datetime import timedelta


def sync_from_remote(path: str):
    # A python wrapper to call the PowerShell sync and generate scripts to generate code from remote TypeSpec service definition
    sync_process = subprocess.Popen(["pwsh", ".\eng\common\scripts\TypeSpec-Project-Sync.ps1", path], stdout=sys.stdout)
    sync_process.communicate()
    if sync_process.returncode != 0:
        print("Error syncing from remote repository. Exiting")
        exit(1)
    print("########################################")
    print("Sync from remote completed successfully")
    print("########################################")


def generate_client_library(path: str):
    generate_process = subprocess.Popen(["pwsh", ".\eng\common\scripts\TypeSpec-Project-Generate.ps1", path], stdout=sys.stdout)
    generate_process.communicate()
    if generate_process.returncode != 0:
        print("Error generating client library. Exiting")
        exit(1)
    print("########################################")
    print("Code generation completed successfully")
    print("########################################")


def main():
    parser = argparse.ArgumentParser(description='Generates client library from TypeSpec service definition.')
    parser.add_argument('--output-path', '--o', required=True, type=str, help='Specify the directory containing the tsp-location.yaml which will also be the output directory for the generated code.')
    args = parser.parse_args()

    print("Generating code in " + args.output_path)
    start_time = time.time()

    # sparse checkout TypeSpec files from remote repository
    sync_from_remote(args.output_path)

    # generate code from TypeSpec
    generate_client_library(args.output_path)

    elapsed_time = time.time() - start_time

    print('Total time to generate code: {} seconds'.format(str(timedelta(seconds=elapsed_time))))


if __name__ == '__main__':
    main()
