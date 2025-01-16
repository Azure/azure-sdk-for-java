# Copyright (c) Microsoft Corporation.
# Licensed under the MIT License.
"""
This script updates external dependencies in the `external_dependencies.txt` file based on the provided JSON input.
It also runs another script `update_versions.py` to update upgraded dependencies across all SDKs.

Usage:
    python dependabot_update_external_dependencies.py --json '<json_string>'
"""

import argparse
import json
import subprocess

def main():
    """
    Main function to parse arguments, update dependencies, and run the update_versions.py script.
    """
    file_path = "eng/versioning/external_dependencies.txt"
    print(f"Updating dependencies in {file_path}")

    def __update_dependency(search_text, pre_version, new_version):
        """
        Update the dependency version in the external_dependencies.txt file.

        Args:
            search_text (str): The dependency name to search for.
            pre_version (str): The previous version of the dependency.
            new_version (str): The new version of the dependency.
        """
        with open(file_path, 'r') as file:
            lines = file.readlines()

        updated = False
        with open(file_path, 'w') as file:
            for line in lines:
                if search_text in line:
                    parts = line.strip().split(';')
                    if len(parts) == 2:
                        parts[1] = new_version
                        line = ';'.join(parts) + '\n'
                        updated = True
                file.write(line)

        if updated:
            print(f"Updated {search_text} from version {pre_version} to version {new_version}")
        else:
            print(f"{search_text} not found in the file.")

    # Parse command-line arguments
    parser = argparse.ArgumentParser(description="Update external dependencies based on JSON input.")
    parser.add_argument("--json", type=str, help="JSON string containing dependency update information.")
    args = parser.parse_args()

    # Load JSON data
    data = json.loads(args.json)
    dependency_metadata = data[0]
    dependency_name = dependency_metadata['dependencyName']
    pre_version = dependency_metadata['prevVersion']
    new_version = dependency_metadata['newVersion']

    # Update the dependency
    __update_dependency(dependency_name, pre_version, new_version)

    # Run another script to update versions
    print("Running update_versions.py --ut external_dependency --sr")
    script_path = "eng/versioning/update_versions.py"
    subprocess.run(["python", script_path, "--ut", "external_dependency", "--sr"])

if __name__ == "__main__":
    main()
