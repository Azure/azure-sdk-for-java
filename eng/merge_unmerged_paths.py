# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.4 or higher is required to run this script.

# Use case: Adds or removed unmerged paths based on their status.
#
# Parameters:
#
# '--debug'/'-d': Flag that enables debug level output.
#
# The script must be run at the root of azure-sdk-for-java.

import argparse
import os
import subprocess

# Runs the git diff command outputing all values without CLI paging.
# This returns only the names of unmerged paths.
list_unmerged_path_names = ['git', '--no-pager', 'diff', '--diff-filter=U', '--name-only']

# Base command arguments for getting the status of an unmerged file.
# --short will return a more machine readable output.
#
# The returned status of the file will be one of the following and will use the corresponding resolution.
#
# Status    |   Explanation                                 |   Resolution                                  |   Resolution Explanation
# DD        |   Both base and upstream deleted the file     |   git rm will be used to delete the file      |   File was deleted by both
# AU        |   File added by base                          |   git rm will be used to delete the file      |   Base added the file so it will be deleted as upstream is the source of truth
# UD        |   Deleted by them                             |   git rm will be used to delete the file      |   Upstream deleted the file and is the source of truth
# UA        |   Added by them                               |   git add will be used to add the file        |   Upstream added the file and is the source of truth
# DU        |   Deleted by us                               |   git add will be used to add the file        |   Base deleted the file but it was added by upstream which is the source of truth
# AA        |   Both base and upstream added the file       |   git add will be used to add the file        |   File was added by both
# UU        |   Both base and upstream modified the file    |   git add will be used to add the file        |   File was modified by both
unmerged_path_status_base_command = ['git', 'status', '--short']

# Handles adding or removing unmerged paths.
def handle_unmerged_paths(debug: bool):
    unmerged_paths = get_unmerged_paths(debug)

    for unmerged_path in unmerged_paths:
        resolve_unmerged_path(unmerged_path, debug)

# Gets the unmerged paths in the current git merge.
def get_unmerged_paths(debug: bool):
    completed_process = subprocess.run(list_unmerged_path_names, check=True, capture_output=True)

    diff_output = str(completed_process.stdout, encoding='UTF-8')

    unmerged_paths = []
    for unmerged_path in diff_output.splitlines(keepends=False):
        if unmerged_paths is None or unmerged_paths == '':
            continue

        unmerged_paths.append(unmerged_path)

    if debug:
        print('Found the following unmerged paths{}'.format(os.linesep))

        for unmerged_path in unmerged_paths:
            print(unmerged_path)

    return unmerged_paths

# Resolves the unmerged path.
def resolve_unmerged_path(unmerged_path: str, debug: bool):
    unmerged_path_status_command = unmerged_path_status_base_command.copy()
    unmerged_path_status_command.append(unmerged_path)

    completed_process = subprocess.run(unmerged_path_status_command, check=True, capture_output=True)

    status = str(completed_process.stdout, encoding='UTF-8').split(' ')[0]

    if debug:
        print('git status of {} is {}'.format(unmerged_path, status))

    resolution_command = ['git']

    if status in ['DD', 'AU', 'UD']:
        resolution_command.append('rm')
    elif status in ['UA', 'DU', 'AA', 'UU']:
        resolution_command.append('add')
    else:
        raise ValueError('status is not one of {}'.format(['DD', 'AU', 'UD', 'UA', 'DU', 'AA', 'UU']))    

    resolution_command.append(unmerged_path)

    if debug:
        print('{}Using command {} to resolve unmerged path {}{}'.format(os.linesep, str.join(' ', resolution_command), unmerged_path, os.linesep))

    subprocess.run(resolution_command, check=True, capture_output=True)

def main():
    parser = argparse.ArgumentParser(description='Runs compilation, testing, and linting for the passed artifacts.')
    parser.add_argument('--debug', '-d', action='store_true', default=False, help='Flag that enables debug level output.')
    args = parser.parse_args()

    handle_unmerged_paths(args.debug)

if __name__ == '__main__':
    main()
