# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.4 or higher is required to run this script.

# Use case: Invokes embedme README codesnippet generation and validation regardless of OS.
#
# Flags
#   --readme/-r: Path to the README.
#   --verify/-v: Flag indicating to only perform a dry-run validation.
#   --debug/-d: Flag indicating to perform debug level logging.
#
# For example: Generating README codesnippets for Azure Storage Blobs.
#    python eng/scripts/invoke_embedme.py -r sdk/storage/azure-storage-blob/README.md
#
# For example: Valdate README codesnippets for Azure Core.
#    python eng/scripts/invoke_embedme.py -r sdk/core/azure-core/README.md -v
#
# The script must be run at the root of azure-sdk-for-java.

import argparse
import os
import sys

# NPX command.
npx_command = 'npx embedme'

# Invoke embedme.
def invoke_embedme(readme: str, verify: bool, debug: bool):
    command = npx_command

    # If the passed README path was relative
    command += ' ' + os.path.abspath(readme)

    if verify:
        command += ' --verify'

    if debug:
        print('Running embedme command: {}'.format(command))

    sys.exit(os.system(command))

def main():
    parser = argparse.ArgumentParser(description='Invokes embedme README codesnippet generation and validation regardless of OS.')
    parser.add_argument('--readme', '-r', type=str, required=True, help='Path to the README')
    parser.add_argument('--verify', '-v', action='store_true', help='Flag indicating to only perform a dry-run validation')
    parser.add_argument('--debug', '-d', action='store_true', help='Flag indicating to perform debug level logging')
    args = parser.parse_args()

    invoke_embedme(args.readme, args.verify, args.debug)

if __name__ == '__main__':
    main()
