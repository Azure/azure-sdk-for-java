# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.4 or higher is required to run this script.

# Simple helper script that adds a single-line (//) Java comment to shaded code
# explaining where the source code came from (GitHub repository if possible)
# and the license it is under.
#
# Right now this script is only used by azure-json which contains code shaded
# from Jackson Core (https://github.com/FasterXML/jackson-core) which uses
# an Apache-2.0 license and the shaded code exists in 
# /sdk/core/azure-json/src/main/java/com/azure/json/implementation/jackson/core
# where all of these values are hardcoded into this script. In the future if
# this script becomes generalized all these values will need to be
# configurable, if that happens take guidance from the /eng/versioning scripts
# on how to pass values to a Python script.

import os

def main():
    code_comment = '// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.'
    
    walk_directory = os.path.dirname(__file__)
    walk_directory = os.path.normpath(os.path.join(walk_directory, 'src/main/java/com/azure/json/implementation/jackson/core'))

    for root, _, files in os.walk(walk_directory):
        for file_name in files:
            file_path = root + os.sep + file_name
            file_content = ''
            with open(file_path, encoding='utf-8', mode='r') as f:
                file_content = f.read()

            if file_content.startswith(code_comment):
                # Expected code comment already exists move on to the next file
                continue

            with open(file_path, encoding='utf-8', mode='w') as f:
                f.write(code_comment + '\n' + file_content)


if __name__ == '__main__':
    main()
