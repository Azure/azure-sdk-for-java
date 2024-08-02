# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Python version 3.4 or higher is required to run this script.

# Simple helper script that adds a single-line (//) Java comment to shaded code
# explaining where the source code came from (GitHub repository if possible)
# and the license it is under.
#
# Right now this script is only used by azure-xml which contains code shaded
# from Aalto XML (https://github.com/FasterXML/aalto-xml) which uses
# an Apache-2.0 license and the shaded code exists in
# /sdk/serialization/azure-xml/src/main/java/com/azure/xml/implementation/aalto
# and
# /sdk/serialization/azure-xml/src/main/java/com/azure/xml/implementation/stax2
# where all of these values are hardcoded into this script. In the future if
# this script becomes generalized all these values will need to be
# configurable, if that happens take guidance from the /eng/versioning scripts
# on how to pass values to a Python script.

import os

def walk_and_add_comment(directory, code_comment):
    walk_directory = os.path.dirname(__file__)
    walk_directory = os.path.normpath(os.path.join(walk_directory, 'src/main/java/com/azure/xml/implementation/' + directory))

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

def main():
    walk_and_add_comment('aalto', '// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.')
    walk_and_add_comment('stax2', '// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License')

if __name__ == '__main__':
    main()
